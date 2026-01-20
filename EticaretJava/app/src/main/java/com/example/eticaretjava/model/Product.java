package com.example.eticaretjava.model;


import com.google.gson.annotations.SerializedName;

public class Product {

    public static class ProductListDto {
        @SerializedName("id")
        public int id;

        @SerializedName("name")
        public String name;

        @SerializedName("price")
        public double price;

        @SerializedName("discount_percent")
        public Double discountPercent;

        @SerializedName("image_url")
        public String imageUrl;

        @SerializedName("stock_qty")
        public int stockQty;

        @SerializedName("is_active")
        public int isActive;
    }

    public static class ProductDto {
        @SerializedName("id")
        public int id;

        @SerializedName("name")
        public String name;

        @SerializedName("slug")
        public String slug;

        @SerializedName("sku")
        public String sku;

        @SerializedName("price")
        public double price;

        @SerializedName("discount_percent")
        public Double discountPercent;

        @SerializedName("image_url")
        public String imageUrl;

        @SerializedName("stock_qty")
        public int stockQty;

        @SerializedName("is_active")
        public int isActive;

        @SerializedName("created_at")
        public String createdAt;

        @SerializedName("updated_at")
        public String updatedAt;
    }
}

