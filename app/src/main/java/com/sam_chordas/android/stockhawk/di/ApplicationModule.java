package com.sam_chordas.android.stockhawk.di;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.sam_chordas.android.stockhawk.network.YahooApiService;

import java.io.IOException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by heim on 8/20/16.
 */
@Module
public class ApplicationModule {
    private final Context mApplicationContext;

    public ApplicationModule(Context mApplicationContext) {
        this.mApplicationContext = mApplicationContext;
    }

    @Provides
    @Singleton
    public Context proviceApplicationContext() {
        return mApplicationContext;
    }

    @Provides
    @Singleton // Application reference must come from AppModule.class
    public SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
    }

    @Provides
    @Singleton
    public YahooApiService provideTheMovieDBApiService() {

        OkHttpClient client = buildOkHttpClient();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(YahooApiService.BASE_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(client)
                .build();


        return retrofit.create(YahooApiService.class);
    }

    private OkHttpClient buildOkHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        // add your other interceptors …

        // add logging as last interceptor
        httpClient.addInterceptor(logging);  // <-- this is the important line!
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                // &format=json&diagnostics=true&env=store://datatables.org/alltableswithkeys&callback=
                HttpUrl newUrl = request.url().newBuilder()
                        .addQueryParameter("format", "json")
                        .addQueryParameter("diagnostics","true")
                        .addQueryParameter("env", "store://datatables.org/alltableswithkeys")
                        .addQueryParameter("callback", "")
                        .build();
                Request newRequest = request.newBuilder().url(newUrl).build();
                return chain.proceed(newRequest);
            }
        });

        return httpClient.build();

    }
}
