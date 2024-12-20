package com.example.aap;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * This code is based on ChatGPT prompt, regarding using Google API
 */
public interface GoogleCustomSearchService {
    @GET("customsearch/v1")
    Call<SearchResponse> searchImages(
            @Query("key") String apiKey,
            @Query("cx") String cx,
            @Query("q") String query,
            @Query("searchType") String searchType,
            @Query("num") int num
    );
}