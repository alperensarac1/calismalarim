package com.example.eticaretjava.model;


import com.google.gson.annotations.SerializedName;

public class Util {

    public static class BasicOk {
        @SerializedName("ok")
        public boolean ok;
    }

    public static class ApiResponse<T> {
        @SerializedName("ok")
        public boolean ok;

        @SerializedName("data")
        public T data;

        @SerializedName("error")
        public String error;
    }
}

