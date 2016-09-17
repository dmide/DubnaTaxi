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
    private final ModelFragment modelFragment;

    private final Interceptor REWRITE_RESPONSE_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());
            String cacheControl = originalResponse.header("Cache-Control");

            if (cacheControl == null || cacheControl.contains("no-store") || cacheControl.contains("no-cache") ||
                    cacheControl.contains("must-revalidate") || cacheControl.contains("max-age=0")) {
                return originalResponse.newBuilder()
                        .header("Cache-Control", "public, max-age=" + 10)
                        .build();
            } else {
                return originalResponse;
            }
        }
    };

    private final Interceptor OFFLINE_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            if (!modelFragment.isOnline()) {
                Log.d(TAG, "rewriting request");

                int maxStale = 60 * 60 * 24 * 560; // tolerate 80-weeks stale
                request = request.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                        .build();
            }

            return chain.proceed(request);
        }
    };

    public NetworkClient(ModelFragment modelFragment, String contentUrl) {
        this.modelFragment = modelFragment;

        //setup cache
        File httpCacheDirectory = new File(modelFragment.getApplicationContext().getCacheDir(), "responses");
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(httpCacheDirectory, cacheSize);

        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(REWRITE_RESPONSE_INTERCEPTOR)
                .addInterceptor(OFFLINE_INTERCEPTOR)
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
