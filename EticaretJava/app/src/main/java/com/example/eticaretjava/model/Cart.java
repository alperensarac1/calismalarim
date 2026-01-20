package com.example.eticaretjava.model;


import java.util.List;

public class Cart {

    public static class CartDto {
        public Integer cart_id;
        public List<CartItemDto> items;
        public double total;
        public int total_items;
    }

    public static class CartItemDto {
        public int item_id;
        public int quantity;
        public int product_id;
        public String name;
        public String sku;
        public String image_url;
        public int stock_qty;
        public double price;
        public Double discount_percent;
        public double sale_price;
    }

    public static class AddToCartRequest {
        public int product_id;
        public int quantity;

        public AddToCartRequest(int productId, int quantity) {
            this.product_id = productId;
            this.quantity = quantity;
        }
    }

    public static class AddToCartResponse {
        public int cart_id;
        public int item_id;
        public int quantity;
    }

    public static class UpdateCartItemRequest {
        public int quantity;

        public UpdateCartItemRequest(int quantity) {
            this.quantity = quantity;
        }
    }
}
