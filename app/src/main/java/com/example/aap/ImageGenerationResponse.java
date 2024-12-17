package com.example.aap;

import java.util.List;

public class ImageGenerationResponse {
    private int created;
    private List<ImageData> data;

    public int getCreated() {
        return created;
    }

    public List<ImageData> getData() {
        return data;
    }
}
