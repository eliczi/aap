package com.example.aap;

import android.util.Log;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;

public class RetrofitClient {
    private static final String BASE_URL = "https://api.openai.com/";

    private static Retrofit retrofit = null;

    public static OpenAIApiService getOpenAIImageClient() {
        if (retrofit == null) {

            // Create an interceptor to add the Authorization header
            Interceptor authInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Log.d("OPENAI", Constants.OPENAI_API_KEY);
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "Bearer " + Constants.OPENAI_API_KEY)
                            .header("Content-Type", "application/json");
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit.create(OpenAIApiService.class);
    }

    public static OpenAITextService getOpenAITextClient() {
        if (retrofit == null) {
            // Same interceptor and OkHttpClient setup as in getClient(), or a different one if needed
            Interceptor authInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "Bearer " + Constants.OPENAI_API_KEY)
                            .header("Content-Type", "application/json");
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit.create(OpenAITextService.class);
    }

}
