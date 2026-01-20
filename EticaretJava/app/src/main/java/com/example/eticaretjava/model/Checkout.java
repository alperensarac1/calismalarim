package com.example.eticaretjava.model;

import com.google.gson.annotations.SerializedName;

public class Checkout {

    public static class CheckoutRequest {
        @SerializedName("idempotency_key")
        public String idempotencyKey;

        @SerializedName("address_name")
        public String addressName;

        @SerializedName("address_line1")
        public String addressLine1;

        @SerializedName("address_line2")
        public String addressLine2;

        @SerializedName("city")
        public String city;

        @SerializedName("district")
        public String district;

        @SerializedName("postal_code")
        public String postalCode;
    }

    public static class CheckoutResponse {
        @SerializedName("order_id")
        public int orderId;

        @SerializedName("total")
        public double total;

        @SerializedName("currency")
        public String currency;
    }
}

