package com.bvhllc.openvpnmanager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// ****************************************************************************
// HostService Class
// ****************************************************************************
public abstract class HostService {
    private static final Logger logger = LoggerFactory.getLogger(HostService.class);


    // ****************************************************************************
    // Static Properties
    // ****************************************************************************

    private Retrofit retrofit;
    private Interceptor interceptor;
    private Interceptor retryInterceptor;

    // ****************************************************************************
    // Abstract Methods
    // ****************************************************************************

    public abstract String endpointAddress();
    public abstract List<Interceptor> getInterceptors();

    // ****************************************************************************
    // Getters / Setters
    // ****************************************************************************

    public Retrofit getRetrofit() {
        if (this.retrofit == null) {
            retrofit = getRetrofit(null);
        }

        return retrofit;
    }

    public Retrofit getRetrofit(OkHttpClient client) {
        if (this.retrofit == null) {

            if (client == null) {
                OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
                okHttpClient.addInterceptor(getInterceptor());

                //iterate through interceptors and add them
                for (Interceptor interceptor : getInterceptors() ) {
                    okHttpClient.addInterceptor(interceptor);
                }

                okHttpClient.readTimeout(45, TimeUnit.SECONDS);
                okHttpClient.writeTimeout(45, TimeUnit.SECONDS);
                client = okHttpClient.build();
            }

            this.retrofit = new Retrofit.Builder()
                    .baseUrl(endpointAddress())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(client)
                    .build();
        }

        return retrofit;
    }

    public void setRetrofit(Retrofit retrofit) {
        this.retrofit = retrofit;
    }


    public Interceptor getInterceptor() {
        if (this.interceptor == null) {
            this.interceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();

                    // Customize the request
                    Request request = original.newBuilder()
                            .addHeader("Content-Type", "application/json")
                            .method(original.method(), original.body())
                            .build();

                    Response response = chain.proceed(request);

                    // Customize or return the response
                    return response;
                }
            };
        }

        return this.interceptor;
    }

    public Interceptor getRetryInterceptor() {
        if (this.retryInterceptor == null) {
            this.retryInterceptor = new Interceptor() {
                @SuppressWarnings("resource")
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();

                    // try the request
                    Response response = chain.proceed(request);

                    int tryCount = 0;
                    while (!response.isSuccessful() && tryCount < 2) {
                        logger.info("Request is not successful - " + tryCount);
                        tryCount++;
                        // retry the request
                        response = chain.proceed(request);
                    }

                    // otherwise just pass the original response on
                    return response;
                }
            };
        }

        return this.retryInterceptor;
    }

    public void setInterceptor(Interceptor requestInterceptor) {
        this.interceptor = requestInterceptor;

    }
}

