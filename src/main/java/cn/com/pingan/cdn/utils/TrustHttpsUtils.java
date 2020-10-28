package cn.com.pingan.cdn.utils;

import javax.net.ssl.*;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @Classname TrustHttpsUtils
 * @Description TODO
 * @Date 2020/10/27 11:08
 * @Created by Luj
 */
public class TrustHttpsUtils {

    private static SSLSocketFactory sslSocketFactory;

    final static X509TrustManager trustManager = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };

    public static SSLSocketFactory getSslSocketFactory(){
        try {
            SSLContext sslContext;
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null,new X509TrustManager[]{trustManager},null);
            sslSocketFactory = sslContext.getSocketFactory();
        } catch ( GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

        return sslSocketFactory;
    }



    static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
}
