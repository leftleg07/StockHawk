package com.sam_chordas.android.stockhawk.network;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by heim on 8/20/16.
 */

public interface YahooApiService {
    String BASE_URL = "https://query.yahooapis.com";

    @GET("/v1/public/yql")
    Observable<String> getYQL(@Query("q") String query);

}
