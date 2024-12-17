package com.example.aap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import java.util.List;

public interface OpenAIApiService {

    @Headers("Content-Type: application/json")
    @POST("v1/images/generations")
    Call<ImageGenerationResponse> generateImage(@Body ImageGenerationRequest request);
}
