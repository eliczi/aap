package com.example.aap;

import java.util.List;

/**
 * This code is based on ChatGPT prompt, regarding using Google API
 */
public class SearchResponse {
    List<Item> items;

    public static class Item {
        public String link;
        // You may add more fields if needed
    }

    public List<Item> getItems() {
        return items;
    }
}
