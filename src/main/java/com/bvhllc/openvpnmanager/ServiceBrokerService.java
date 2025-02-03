package com.bvhllc.openvpnmanager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;

import com.bvhllc.openvpnmanager.utility.Constants;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

// ************************************************************************
// ServiceBrokerService Class
// ************************************************************************
public class ServiceBrokerService extends HostService {

    // ************************************************************************
    // Instance Properties
    // ************************************************************************

    private String endpointAddress = null;
    private ServiceBrokerApi serviceBrokerService = null;
    private List<Interceptor> serviceBrokerInterceptors = null;

    // ************************************************************************
    // Constructors
    // ************************************************************************

    public ServiceBrokerService() {
    }

    // ************************************************************************
    // Abstract HostService Methods
    // ************************************************************************

    @Override
    public String endpointAddress() {
        if (endpointAddress == null) {

            endpointAddress = (Constants.ServiceBrokerIsSecure) ? "https://" : "http://";

            int slashIndex = Constants.ServiceBrokerAddress.indexOf("/");
            if (slashIndex > 0) {
                String addressHalf = Constants.ServiceBrokerAddress.substring(0, slashIndex);
                String remainingPath = Constants.ServiceBrokerAddress.substring(slashIndex);

                endpointAddress += addressHalf + ":" + Constants.ServiceBrokerPort + remainingPath;
            }
            else {
                endpointAddress += Constants.ServiceBrokerAddress + ":" + Constants.ServiceBrokerPort;
            }
        }

        return endpointAddress;
    }

    @Override
    public List<Interceptor> getInterceptors() {
        if (serviceBrokerInterceptors == null) {
            serviceBrokerInterceptors = new ArrayList<Interceptor>();

            if (Constants.RetrofitLoggingEnabled) {
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                serviceBrokerInterceptors.add(loggingInterceptor);
            }

        }

        return serviceBrokerInterceptors;
    }


    // ************************************************************************
    // Public Methods
    // ************************************************************************

    public void setCaCertHttpClient(String caCert) {
        setRetrofit(null);
        setService(null);

        //set up new rest adapter to use new cert
        ServiceBrokerOKClient serviceBrokerClient = new ServiceBrokerOKClient(caCert);

        OkHttpClient.Builder okHttpClient = serviceBrokerClient.newBuilder()
                .addInterceptor(getRetryInterceptor())
                .sslSocketFactory(serviceBrokerClient.sslSocketFactory, serviceBrokerClient.trustManager);

        OkHttpClient client = okHttpClient.build();

        getRetrofit( client);
    }

    public ServiceBrokerApi getService() {
        if (serviceBrokerService == null) {
            serviceBrokerService = getRetrofit().create(ServiceBrokerApi.class);
        }

        return serviceBrokerService;
    }

    public void setService(ServiceBrokerApi serviceApi) {
        serviceBrokerService = serviceApi;
    }

    // ************************************************************************
    // Private Class
    // ************************************************************************

    private class ServiceBrokerOKClient extends OkHttpClient {

        public SSLSocketFactory sslSocketFactory;
        public X509TrustManager trustManager;

        public ServiceBrokerOKClient(String caInput) {
            super();

            try {
                if (caInput != null) {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");

                    Certificate ca;
                    ca = cf.generateCertificate(new ByteArrayInputStream(Base64.decodeBase64(caInput)));
                    System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());

                    // Create a KeyStore containing our trusted CAs
                    String keyStoreType = KeyStore.getDefaultType();
                    KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                    keyStore.load(null, null);
                    keyStore.setCertificateEntry("ca", ca);

                    // Create a TrustManager that trusts the CAs in our KeyStore and system CA
                    //BvhTrustManager trustManager = new BvhTrustManager(keyStore);
                    trustManager = new CustomTrustManager(keyStore);
                }
                else { //trust everyone if null
                    trustManager = new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    };
                }

                // Create an SSLContext that uses our TrustManager
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, new TrustManager[]{trustManager}, null);
                sslSocketFactory = context.getSocketFactory();
                //setSslSocketFactory(context.getSocketFactory());
            }
            catch (java.security.cert.CertificateException e) {
                e.printStackTrace();
            }
            catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            catch (KeyManagementException e) {
                e.printStackTrace();
            }
            catch (KeyStoreException e) {
                e.printStackTrace();
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}

