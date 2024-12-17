package com.example.aap;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OpenAITextService {

    @Headers({
            "Content-Type: application/json",
            "Authorization: Bearer YOUR_OPENAI_API_KEY" // Replace with your actual key handling
    })
    @POST("v1/chat/completions")
    Call<Map<String, Object>> getChatCompletion(@Body Map<String, Object> requestBody);
}
