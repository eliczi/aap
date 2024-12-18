package com.example.aap;

public class ImageGenerationRequest {
    private String prompt;
    private int n;
    private String size;

    public ImageGenerationRequest(String prompt) {
        this.prompt = prompt;
        this.n = 1;
        this.size = "512x512";
    }
}
