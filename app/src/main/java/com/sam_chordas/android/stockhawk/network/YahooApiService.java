package com.sam_chordas.android.stockhawk.network;


import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Yahoo query api
 */

public interface YahooApiService {
    String BASE_URL = "https://query.yahooapis.com";

    @GET("/v1/public/yql")
    Observable<String> getYQL(@Query("q") String query);

}