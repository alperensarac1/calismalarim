package com.example.eticaretjava.model;

import com.google.gson.annotations.SerializedName;

public class Payment {

    public static class PaymentDto {
        @SerializedName("provider")
        public String provider;

        @SerializedName("status")
        public String status;

        @SerializedName("amount")
        public double amount;

        @SerializedName("created_at")
        public String createdAt;
    }
}

