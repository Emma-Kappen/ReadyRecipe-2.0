package com.readyrecipe.android.network;

import android.content.Context;
import com.readyrecipe.android.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .addInterceptor(interceptor);

            if (context != null) {
                SessionManager sessionManager = new SessionManager(context.getApplicationContext());
                clientBuilder.addInterceptor(new AuthInterceptor(sessionManager));
            }

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(clientBuilder.build())
                    .build();
        }
        return retrofit;
    }

    // Backwards-compatible access for legacy callers
    public static Retrofit getClient() {
        return getClient(null);
    }
}
