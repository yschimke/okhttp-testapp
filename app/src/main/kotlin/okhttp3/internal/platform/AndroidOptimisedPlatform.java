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

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.NetworkInfo;
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
import com.google.android.gms.common.GooglePlayServicesUtil;
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
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.net.SocketFactory;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.internal.Util;
import okhttp3.internal.tls.CertificateChainCleaner;
import okhttp3.network.Network;
import okhttp3.network.NetworkAccess;
import org.jetbrains.annotations.NotNull;

/**
 * Network loss detection
 * Network selection
 * GMS Security Provider support
 * Session reuse
 */
public class AndroidOptimisedPlatform extends Platform {
  private final Context context;
  private final X509TrustManager trustManager;
  private SSLCertificateSocketFactory socketFactory;

  private final AndroidPlatform.CloseGuard closeGuard = AndroidPlatform.CloseGuard.get();
  private final OptionalMethod getAlpnSelectedProtocol;
  private final OptionalMethod setAlpnProtocols;
  private final NetworkAccess networkAccess;

  public AndroidOptimisedPlatform(Context context) {
    this.context = context;
    this.trustManager = systemDefaultTrustManager();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      ConnectivityManager connectivityManager = context.getSystemService(ConnectivityManager.class);
      networkAccess = new AndroidNetworkAccess(connectivityManager);
    } else {
      networkAccess = NetworkAccess.DEFAULT;
    }

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
      SSLCertificateSocketFactory sslSocketFactory = getSslSocketFactory();
      sslSocketFactory.setUseSessionTickets(sslSocket, true);
      sslSocketFactory.setHostname(sslSocket, hostname);
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

  public static X509TrustManager systemDefaultTrustManager() {
    try {
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
          TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init((KeyStore) null);
      TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
      if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
        throw new IllegalStateException("Unexpected default trust managers:"
            + Arrays.toString(trustManagers));
      }
      return (X509TrustManager) trustManagers[0];
    } catch (GeneralSecurityException e) {
      throw new AssertionError("No System TLS", e); // The system has no TLS. Just give up.
    }
  }

  @NotNull public void configureClient(
      @NotNull OkHttpClient.Builder testBuilder) {

    synchronized (this) {
      testBuilder.sslSocketFactory(getSslSocketFactory(), trustManager);
    }
  }

  public SSLCertificateSocketFactory getSslSocketFactory() {
    if (socketFactory == null) {
      SSLSessionCache cache = new SSLSessionCache(context);
      socketFactory =
          (SSLCertificateSocketFactory) SSLCertificateSocketFactory.getDefault(5000, cache);
    }
    return socketFactory;
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

  @Override public NetworkAccess getNetworkAccess() {
    return networkAccess;
  }

  @TargetApi(Build.VERSION_CODES.M)
  class AndroidNetworkAccess implements NetworkAccess {
    private final ConnectivityManager connectivityManager;

    public AndroidNetworkAccess(ConnectivityManager connectivityManager) {
      this.connectivityManager = connectivityManager;
    }

    @Override public List<Network> available() throws IOException {
      List<Network> connected = new ArrayList<>();
      List<Network> possible = new ArrayList<>();

      for (android.net.Network n : connectivityManager.getAllNetworks()) {
        Network network = toNetwork(n);

        if (connectivityManager.getNetworkInfo(n).isConnected()) {
          connected.add(network);
        } else {
          possible.add(network);
        }
      }

      connected.addAll(possible);

      Log.i("AndroidNetworkAccess", "networks: " + connected);

      return connected;
    }

    @NonNull public Network toNetwork(android.net.Network n) {
      NetworkInfo info = connectivityManager.getNetworkInfo(n);
      LinkProperties properties = connectivityManager.getLinkProperties(n);
      String name = properties.getInterfaceName();

      Set<String> tags = new LinkedHashSet<>();
      if (info.isRoaming()) {
        tags.add("roaming");
      }

      Dns dns = new NetworkDns(n);
      ProxySelector proxySelector = new AndroidProxySelector(properties);
      SocketFactory socketFactory = n.getSocketFactory();

      Log.i("AndroidNetworkAccess", "addresses: " + properties.getLinkAddresses());

      return new Network(name, name, null, null, null, tags, dns, socketFactory, proxySelector);
    }
  }

  class NetworkDns implements Dns {
    private final android.net.Network network;

    public NetworkDns(android.net.Network network) {
      this.network = network;
    }

    @Override public List<InetAddress> lookup(String s) throws UnknownHostException {
      Log.i("NetworkDns", "lookup: " + s);
      return Arrays.asList(network.getAllByName(s));
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      NetworkDns that = (NetworkDns) o;

      return network.equals(that.network);
    }

    @Override public int hashCode() {
      return network.hashCode();
    }
  }

  class AndroidProxySelector extends ProxySelector {
    private final LinkProperties properties;

    public AndroidProxySelector(LinkProperties properties) {
      this.properties = properties;
    }

    @Override public List<Proxy> select(URI uri) {
      // TODO implement
      Log.i("AndroidProxySelector", "select proxy: " + uri);
      return Collections.singletonList(Proxy.NO_PROXY);
    }

    @Override public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
      ProxySelector.getDefault().connectFailed(uri, sa, ioe);
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      AndroidProxySelector that = (AndroidProxySelector) o;

      return properties.equals(that.properties);
    }

    @Override public int hashCode() {
      return properties.hashCode();
    }
  }
}
