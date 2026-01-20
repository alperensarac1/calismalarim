package com.example.eticaretjava.model;

package com.example.eticaretkotlin.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import com.example.eticaretkotlin.model.Payment.PaymentDto;

public class Order {

    public static class OrderSummaryDto {
        @SerializedName("id")
        public int id;

        @SerializedName("status")
        public String status;

        @SerializedName("total_amount")
        public double totalAmount;

        @SerializedName("currency")
        public String currency;

        @SerializedName("created_at")
        public String createdAt;
    }

    public static class OrderItemDto {
        @SerializedName("product_id")
        public int productId;

        @SerializedName("name")
        public String name;

        @SerializedName("sku")
        public String sku;

        @SerializedName("unit_price")
        public double unitPrice;

        @SerializedName("quantity")
        public int quantity;

        @SerializedName("line_total")
        public double lineTotal;
    }

    public static class OrderDetailDto {
        @SerializedName("id")
        public int id;

        @SerializedName("status")
        public String status;

        @SerializedName("total_amount")
        public double totalAmount;

        @SerializedName("currency")
        public String currency;

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

        @SerializedName("created_at")
        public String createdAt;

        @SerializedName("items")
        public List<OrderItemDto> items;

        @SerializedName("payment")
        public PaymentDto payment;
    }
}

