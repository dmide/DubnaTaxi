package ru.dmide.dubnataxi.web;

import android.util.Log;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import ru.dmide.dubnataxi.ModelFragment;

public class NetworkClient {
    private static final String TAG = "NetworkClient";

    private final WebAPI api;

    public NetworkClient(ModelFragment modelFragment, String contentUrl) {

        //setup cache
        File httpCacheDirectory = new File(modelFragment.getApplicationContext().getCacheDir(), "responses");
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(httpCacheDirectory, cacheSize);

        OkHttpClient client = new OkHttpClient.Builder()
                .cache(cache)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(contentUrl)
                .client(client)
                .build();

        api = retrofit.create(WebAPI.class);
    }

    public WebAPI getApi() {
        return api;
    }
}
