package com.android.swaddle.networkManager;


import com.android.swaddle.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Rabia on 27/09/2016.
 */
public class RetrofitClass {

    private static RetrofitClass instance = null;
    private static webRequests service;

    private RetrofitClass() {
        // Exists only to defeat instantiation.
    }

    public static RetrofitClass getInstance() {
        if (instance == null) {
            instance = new RetrofitClass();
            OkHttpClient.Builder client = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS).build().newBuilder();

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            client.addInterceptor(loggingInterceptor);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.SERVER_ADDRESS_NEW)
                    .client(client.build())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            service = retrofit.create(webRequests.class);
        }
        return instance;
    }

    public webRequests getWebRequestsInstance() {

        return service;
    }

}
