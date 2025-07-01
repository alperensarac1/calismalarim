package com.example.adisyonuygulamajava.services.retrofit;

public class ApiUtils {

    private static final String BASE_URL = "https://alperensaracdeneme.com/adisyon/";

    // private constructor to prevent instantiation
    private ApiUtils() {}

    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static AdisyonServiceInterface getAdisyonServisDaoInterface() {
        return RetrofitClient.getRetrofitClient(BASE_URL).create(AdisyonServiceInterface.class);
    }
}

