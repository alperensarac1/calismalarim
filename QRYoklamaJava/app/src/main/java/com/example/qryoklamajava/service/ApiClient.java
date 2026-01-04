package com.example.qryoklamajava.service;

// ApiClient.java
import okhttp3.*;

public class ApiClient {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();

    public static void postJson(String url, String jsonBody, Callback cb) {
        RequestBody body = RequestBody.create(JSON, jsonBody);
        Request req = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Platform", "android")
                .build();
        client.newCall(req).enqueue(cb);
    }
}
