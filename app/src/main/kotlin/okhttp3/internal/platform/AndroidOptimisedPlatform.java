/*
 * Copyright (C) 2016 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package okhttp3.internal.platform;

import android.content.Context;
import android.net.SSLCertificateSocketFactory;
import android.net.SSLSessionCache;
import android.net.http.X509TrustManagerExtensions;
import android.os.Build;
import android.security.NetworkSecurityPolicy;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.SocketFactory;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.internal.Util;
import okhttp3.internal.tls.CertificateChainCleaner;
import org.jetbrains.annotations.NotNull;

/**
 * Android 5 or better.
 */
public class AndroidOptimisedPlatform extends Platform {
  private final SSLCertificateSocketFactory socketFactory;

  private final AndroidPlatform.CloseGuard closeGuard = AndroidPlatform.CloseGuard.get();
  private final OptionalMethod getAlpnSelectedProtocol;
  private final OptionalMethod setAlpnProtocols;

  public AndroidOptimisedPlatform(Context context) {
    SSLSessionCache cache = new SSLSessionCache(context);
    this.socketFactory =
        (SSLCertificateSocketFactory) SSLCertificateSocketFactory.getDefault(5000, cache);

    getAlpnSelectedProtocol
        = new OptionalMethod<>(byte[].class, "getAlpnSelectedProtocol");
    setAlpnProtocols
        = new OptionalMethod<>(null, "setAlpnProtocols", byte[].class);
  }

  @Override
  public void connectSocket(Socket socket, InetSocketAddress address,
      int connectTimeout) throws IOException {
    try {
      socket.connect(address, connectTimeout);
    } catch (AssertionError e) {
      if (Util.isAndroidGetsocknameError(e)) throw new IOException(e);
      throw e;
    } catch (ClassCastException e) {
      // On android 8.0, socket.connect throws a ClassCastException due to a bug
      // see https://issuetracker.google.com/issues/63649622
      if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
        throw new IOException("Exception in connect", e);
      } else {
        throw e;
      }
    }
  }

  @Override
  protected X509TrustManager trustManager(SSLSocketFactory sslSocketFactory) {
    throw new UnsupportedOperationException();
  }

  // TODO socket.relaxsslcheck
  @Override
  @RequiresApi(Build.VERSION_CODES.M)
  public void configureTlsExtensions(
      SSLSocket sslSocket, String hostname, List<Protocol> protocols) {
    // Enable SNI and session tickets.
    if (hostname != null) {
      socketFactory.setUseSessionTickets(sslSocket, true);
      socketFactory.setHostname(sslSocket, hostname);
    }

    // Enable ALPN.
    Object[] parameters = {concatLengthPrefixed(protocols)};
    setAlpnProtocols.invokeWithoutCheckedException(sslSocket, parameters);
  }

  @Override
  public String getSelectedProtocol(SSLSocket socket) {
    byte[] alpnResult = (byte[]) getAlpnSelectedProtocol.invokeWithoutCheckedException(socket);
    return alpnResult != null ? new String(alpnResult, Util.UTF_8) : null;
  }

  @Override
  public void log(int level, String message, Throwable t) {
    if (level == WARN) {
      Log.w("OkHttp", message, t);
    } else {
      Log.d("OkHttp", message, t);
    }
  }

  @Override
  public boolean isCleartextTrafficPermitted(String hostname) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      return api24IsCleartextTrafficPermitted(hostname);
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return api23IsCleartextTrafficPermitted();
    }

    return true;
  }

  @RequiresApi(Build.VERSION_CODES.N)
  private boolean api24IsCleartextTrafficPermitted(String hostname) {
    return NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted(hostname);
  }

  @RequiresApi(Build.VERSION_CODES.M)
  private boolean api23IsCleartextTrafficPermitted() {
    return NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted();
  }

  @RequiresApi(Build.VERSION_CODES.M)
  public CertificateChainCleaner buildCertificateChainCleaner(X509TrustManager trustManager) {
    return new AndroidCertificateChainCleaner(new X509TrustManagerExtensions(trustManager));
  }

  @NotNull public void configureBuilder(@NotNull OkHttpClient.Builder clientBuilder) {
    clientBuilder.proxySelector(new AndroidProxySelector());
    clientBuilder.socketFactory(new AndroidSocketFactory());
    //clientBuilder.connectionPool();
  }

  /**
   * X509TrustManagerExtensions was added to Android in API 17 (Android 4.2, released in late 2012).
   * This is the best way to get a clean chain on Android because it uses the same code as the TLS
   * handshake.
   */
  static final class AndroidCertificateChainCleaner extends CertificateChainCleaner {
    private final X509TrustManagerExtensions x509TrustManagerExtensions;

    AndroidCertificateChainCleaner(X509TrustManagerExtensions x509TrustManagerExtensions) {
      this.x509TrustManagerExtensions = x509TrustManagerExtensions;
    }

    @Override
    @RequiresApi(Build.VERSION_CODES.M)
    public List<Certificate> clean(List<Certificate> chain, String hostname)
        throws SSLPeerUnverifiedException {
      try {
        X509Certificate[] certificateArray = chain.toArray(new X509Certificate[0]);
        List sorted =
            x509TrustManagerExtensions.checkServerTrusted(certificateArray, "RSA", hostname);
        return sorted;
      } catch (CertificateException e) {
        SSLPeerUnverifiedException exception = new SSLPeerUnverifiedException(e.getMessage());
        exception.initCause(e);
        throw exception;
      }
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof AndroidCertificateChainCleaner; // All instances are equivalent.
    }

    @Override
    public int hashCode() {
      return 0;
    }
  }

  // TODO do this as part of a build method, log unavailability as a developer warning
  public static void loadGmsProvider(Context context) {
    try {
      ProviderInstaller.installIfNeeded(context);
    } catch (GooglePlayServicesRepairableException e) {
      GoogleApiAvailability.getInstance()
          .showErrorNotification(context, e.getConnectionStatusCode());
    } catch (GooglePlayServicesNotAvailableException e) {
      // ignore
    }
  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  public static @NonNull AndroidOptimisedPlatform install(Context context) {
    AndroidOptimisedPlatform platform = build(context);

    installPlatform(platform);

    return platform;
  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  public static @NonNull AndroidOptimisedPlatform build(Context context) {
    return new AndroidOptimisedPlatform(context);
  }

  public static void installPlatform(Platform platform) {
    try {
      Field platformField = Platform.class.getDeclaredField("PLATFORM");
      platformField.setAccessible(true);
      platformField.set(null, platform);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  public static Platform buildAndroidPlatform() {
    return AndroidPlatform.buildIfSupported();
  }

  class AndroidProxySelector extends ProxySelector {
    @Override public List<Proxy> select(URI uri) {
      List<Proxy> proxies = ProxySelector.getDefault().select(uri);
      Log.i("AndroidProxySelector", "" + uri + " " + proxies);
      return proxies;
    }

    @Override public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
      ProxySelector.getDefault().connectFailed(uri, sa, ioe);
    }
  }

  class AndroidSocketFactory extends SocketFactory {
    private SocketFactory socketFactory = SocketFactory.getDefault();

    @Override public Socket createSocket() throws IOException {
      return socketFactory.createSocket();
    }

    @Override public Socket createSocket(String host, int port)
        throws IOException, UnknownHostException {
      return socketFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
        throws IOException, UnknownHostException {
      return socketFactory.createSocket(host, port, localHost, localPort);
    }

    @Override public Socket createSocket(InetAddress host, int port) throws IOException {
      return socketFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress,
        int localPort)
        throws IOException {
      return socketFactory.createSocket(address, port, localAddress, localPort);
    }
  }
}
