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
import android.support.annotation.RequiresApi;
import android.util.Log;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.Protocol;
import okhttp3.internal.Util;
import okhttp3.internal.tls.CertificateChainCleaner;

/**
 * Android 5 or better.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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
      if (Build.VERSION.SDK_INT == 26) {
        IOException ioException = new IOException("Exception in connect");
        ioException.initCause(e);
        throw ioException;
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

  public CertificateChainCleaner buildCertificateChainCleaner(X509TrustManager trustManager) {
    return new AndroidCertificateChainCleaner(new X509TrustManagerExtensions(trustManager));
  }

  public static AndroidOptimisedPlatform install(Context context) {
    AndroidOptimisedPlatform platform = new AndroidOptimisedPlatform(context);

    try {
      Field platformField = Platform.class.getDeclaredField("PLATFORM");
      platformField.setAccessible(true);
      platformField.set(null, platform);
    } catch (Exception e) {
      throw new AssertionError(e);
    }

    return platform;
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
}
