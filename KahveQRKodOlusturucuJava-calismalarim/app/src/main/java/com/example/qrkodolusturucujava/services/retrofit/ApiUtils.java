package com.example.qrkodolusturucujava.services.retrofit;

public class ApiUtils {
    public static final String BASE_URL = "https://alperensaracdeneme.com/kahveservis/";

    public static KahveHTTPServisDaoInterface getKahveHTTPServisDaoInterface() {
        return RetrofitClient.getRetrofitClient(BASE_URL).create(KahveHTTPServisDaoInterface.class);
    }
}

