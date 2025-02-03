package com.bvhllc.openvpnmanager;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class CustomTrustManager implements X509TrustManager {

    private X509TrustManager defaultTrustManager;
    private X509TrustManager localTrustManager;
    public CustomTrustManager(KeyStore localKeyStore) throws KeyStoreException {
        try {
            this.defaultTrustManager = createTrustManager(null);
            this.localTrustManager = createTrustManager(localKeyStore);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private X509TrustManager createTrustManager(KeyStore store) throws NoSuchAlgorithmException, KeyStoreException {
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init((KeyStore) store);
        TrustManager[] trustManagers = tmf.getTrustManagers();
        return (X509TrustManager) trustManagers[0];
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        try {
            defaultTrustManager.checkServerTrusted(chain, authType);
        } catch (CertificateException ce) {
            localTrustManager.checkServerTrusted(chain, authType);
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        try {
            defaultTrustManager.checkClientTrusted(chain, authType);
        } catch (CertificateException ce) {
            localTrustManager.checkClientTrusted(chain, authType);
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        X509Certificate[] first = defaultTrustManager.getAcceptedIssuers();
        X509Certificate[] second = localTrustManager.getAcceptedIssuers();
        X509Certificate[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
