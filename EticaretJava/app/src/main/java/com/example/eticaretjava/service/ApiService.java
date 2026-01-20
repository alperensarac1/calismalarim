package com.example.eticaretjava.service;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

import com.example.eticaretjava.model.Util.ApiResponse;
import com.example.eticaretjava.model.Util.BasicOk;

import com.example.eticaretjava.model.Category.CategoryDto;
import com.example.eticaretjava.model.Product.ProductDto;
import com.example.eticaretjava.model.Product.ProductListDto;
import com.example.eticaretjava.model.ProductListPage;

import com.example.eticaretjava.model.Cart.CartDto;
import com.example.eticaretjava.model.Cart.AddToCartRequest;
import com.example.eticaretjava.model.Cart.AddToCartResponse;
import com.example.eticaretjava.model.Cart.UpdateCartItemRequest;

import com.example.eticaretjava.model.Checkout.CheckoutRequest;
import com.example.eticaretjava.model.Checkout.CheckoutResponse;

import com.example.eticaretjava.model.Order.OrderSummaryDto;
import com.example.eticaretjava.model.Order.OrderDetailDto;

public class ApiService {

    public interface ProductApi {
        @GET("categories")
        Call<ApiResponse<List<CategoryDto>>> getCategories();

        @GET("products")
        Call<ApiResponse<ProductListPage>> getProducts(
                @Query("cat") Integer cat,
                @Query("q") String q,
                @Query("min") Double min,
                @Query("max") Double max,
                @Query("discount") Integer discount,
                @Query("sort") String sort,
                @Query("page") Integer page,
                @Query("per") Integer per
        );

        @GET("products/{id}")
        Call<ApiResponse<ProductDto>> getProduct(@Path("id") int id);
    }

    public interface CartApi {
        @GET("cart.php")
        Call<ApiResponse<CartDto>> getCart(@Header("Authorization") String bearer);

        @POST("cart_add.php")
        Call<ApiResponse<AddToCartResponse>> addToCart(
                @Header("Authorization") String bearer,
                @Body AddToCartRequest body
        );

        @POST("cart_item.php")
        Call<ApiResponse<BasicOk>> updateItem(
                @Header("Authorization") String bearer,
                @Query("id") int itemId,
                @Body UpdateCartItemRequest body
        );

        @DELETE("cart_item.php")
        Call<ApiResponse<BasicOk>> deleteItem(
                @Header("Authorization") String bearer,
                @Query("id") int itemId
        );
    }

    public interface OrderApi {
        @POST("checkout.php")
        Call<ApiResponse<CheckoutResponse>> checkout(
                @Header("Authorization") String bearer,
                @Body CheckoutRequest body
        );

        @GET("orders.php")
        Call<ApiResponse<List<OrderSummaryDto>>> getOrders(@Header("Authorization") String bearer);

        @GET("order.php")
        Call<ApiResponse<OrderDetailDto>> getOrderDetail(
                @Header("Authorization") String bearer,
                @Query("id") int orderId
        );
    }
}

