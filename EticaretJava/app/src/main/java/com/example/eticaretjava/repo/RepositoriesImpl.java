package com.example.eticaretjava.repo;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.eticaretjava.data.TokenStore;
import com.example.eticaretjava.model.Cart;
import com.example.eticaretjava.model.Util;
import com.example.eticaretjava.model.Util.ApiResponse;

import com.example.eticaretjava.model.Category.CategoryDto;
import com.example.eticaretjava.model.Product.ProductDto;
import com.example.eticaretjava.model.ProductListPage;

import com.example.eticaretkotlin.model.Cart.CartDto;
import com.example.eticaretkotlin.model.Cart.AddToCartRequest;
import com.example.eticaretkotlin.model.Cart.AddToCartResponse;
import com.example.eticaretkotlin.model.Cart.UpdateCartItemRequest;

import com.example.eticaretjava.model.User.LoginRequest;
import com.example.eticaretjava.model.User.LoginResponse;
import com.example.eticaretjava.model.User.RegisterRequest;
import com.example.eticaretjava.model.User.RegisterResponse;
import com.example.eticaretjava.model.User.UserDto;

import com.example.eticaretjava.model.Checkout.CheckoutRequest;
import com.example.eticaretjava.model.Checkout.CheckoutResponse;

import com.example.eticaretjava.model.Order.OrderSummaryDto;
import com.example.eticaretjava.model.Order.OrderDetailDto;

import com.example.eticaretjava.service.AuthApi;
import com.example.eticaretjava.service.ApiService;

public class RepositoriesImpl {

    public static class AuthRepositoryImpl implements Repositories.AuthRepository {

        private final AuthApi api;
        private final TokenStore tokenStore;

        public AuthRepositoryImpl(AuthApi api, TokenStore tokenStore) {
            this.api = api;
            this.tokenStore = tokenStore;
        }

        @Override
        public void login(String email, String password, ResultCallback<Void> cb) {
            api.login(new LoginRequest(email, password)).enqueue(new Callback<ApiResponse<LoginResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                    ApiResponse<LoginResponse> body = response.body();
                    if (body != null && body.ok && body.data != null) {
                        tokenStore.setToken(body.data.token);
                        cb.onSuccess(null);
                    } else {
                        cb.onError(body != null ? (body.error != null ? body.error : "Login failed") : "Login failed");
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                    cb.onError(t.getMessage() != null ? t.getMessage() : "Login failed");
                }
            });
        }

        @Override
        public void register(String name, String email, String password, ResultCallback<Void> cb) {
            api.register(new RegisterRequest(name, email, password)).enqueue(new Callback<ApiResponse<RegisterResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<RegisterResponse>> call, Response<ApiResponse<RegisterResponse>> response) {
                    ApiResponse<RegisterResponse> body = response.body();
                    if (body != null && body.ok && body.data != null) {
                        tokenStore.setToken(body.data.token);
                        cb.onSuccess(null);
                    } else {
                        cb.onError(body != null ? (body.error != null ? body.error : "Register failed") : "Register failed");
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<RegisterResponse>> call, Throwable t) {
                    cb.onError(t.getMessage() != null ? t.getMessage() : "Register failed");
                }
            });
        }

        @Override
        public void me(ResultCallback<UserDto> cb) {
            String token = tokenStore.getToken();
            if (token == null || token.trim().isEmpty()) {
                cb.onError("Token yok");
                return;
            }
            api.me("Bearer " + token).enqueue(new Callback<ApiResponse<UserDto>>() {
                @Override
                public void onResponse(Call<ApiResponse<UserDto>> call, Response<ApiResponse<UserDto>> response) {
                    ApiResponse<UserDto> body = response.body();
                    if (body != null && body.ok) {
                        cb.onSuccess(body.data);
                    } else {
                        cb.onError(body != null ? (body.error != null ? body.error : "Me error") : "Me error");
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<UserDto>> call, Throwable t) {
                    cb.onError(t.getMessage() != null ? t.getMessage() : "Me error");
                }
            });
        }
    }

    public static class ProductRepositoryImpl implements Repositories.ProductRepository {

        private final ApiService.ProductApi api;

        public ProductRepositoryImpl(ApiService.ProductApi api) {
            this.api = api;
        }

        @Override
        public void getCategories(ResultCallback<List<CategoryDto>> cb) {
            api.getCategories().enqueue(new Callback<ApiResponse<List<CategoryDto>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<CategoryDto>>> call, Response<ApiResponse<List<CategoryDto>>> response) {
                    ApiResponse<List<CategoryDto>> body = response.body();
                    if (body != null && body.ok && body.data != null) cb.onSuccess(body.data);
                    else cb.onError(body != null ? (body.error != null ? body.error : "Kategori hatası") : "Kategori hatası");
                }

                @Override
                public void onFailure(Call<ApiResponse<List<CategoryDto>>> call, Throwable t) {
                    cb.onError(t.getMessage() != null ? t.getMessage() : "Kategori hatası");
                }
            });
        }

        @Override
        public void getProducts(Integer cat, String q, Double min, Double max, Integer discount,
                                String sort, Integer page, Integer per, ResultCallback<ProductListPage> cb) {

            api.getProducts(cat, q, min, max, discount, sort, page, per)
                    .enqueue(new Callback<ApiResponse<ProductListPage>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<ProductListPage>> call, Response<ApiResponse<ProductListPage>> response) {
                            ApiResponse<ProductListPage> body = response.body();
                            if (body != null && body.ok && body.data != null) cb.onSuccess(body.data);
                            else cb.onError(body != null ? (body.error != null ? body.error : "Ürün hatası") : "Ürün hatası");
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<ProductListPage>> call, Throwable t) {
                            cb.onError(t.getMessage() != null ? t.getMessage() : "Ürün hatası");
                        }
                    });
        }

        @Override
        public void getProduct(int id, ResultCallback<ProductDto> cb) {
            api.getProduct(id).enqueue(new Callback<ApiResponse<ProductDto>>() {
                @Override
                public void onResponse(Call<ApiResponse<ProductDto>> call, Response<ApiResponse<ProductDto>> response) {
                    ApiResponse<ProductDto> body = response.body();
                    if (body != null && body.ok && body.data != null) cb.onSuccess(body.data);
                    else cb.onError(body != null ? (body.error != null ? body.error : "Detay hatası") : "Detay hatası");
                }

                @Override
                public void onFailure(Call<ApiResponse<ProductDto>> call, Throwable t) {
                    cb.onError(t.getMessage() != null ? t.getMessage() : "Detay hatası");
                }
            });
        }
    }

    public static class CartRepositoryImpl implements Repositories.CartRepository {

        private final ApiService.CartApi api;
        private final TokenStore tokenStore;

        public CartRepositoryImpl(ApiService.CartApi api, TokenStore tokenStore) {
            this.api = api;
            this.tokenStore = tokenStore;
        }

        private String bearerOrThrow(ResultCallback<?> cb) {
            String t = tokenStore.getToken();
            if (t == null || t.trim().isEmpty()) {
                cb.onError("Token yok");
                return null;
            }
            return "Bearer " + t;
        }

        @Override
        public void getCart(ResultCallback<Cart.CartDto> cb) {
            String bearer = bearerOrThrow(cb);
            if (bearer == null) return;

            api.getCart(bearer).enqueue(new Callback<ApiResponse<Cart.CartDto>>() {
                @Override
                public void onResponse(Call<ApiResponse<Cart.CartDto>> call, Response<ApiResponse<Cart.CartDto>> response) {
                    ApiResponse<Cart.CartDto> body = response.body();
                    if (body != null && body.ok && body.data != null) cb.onSuccess(body.data);
                    else cb.onError(body != null ? (body.error != null ? body.error : "Sepet yüklenemedi") : "Sepet yüklenemedi");
                }

                @Override
                public void onFailure(Call<ApiResponse<Cart.CartDto>> call, Throwable t) {
                    cb.onError(t.getMessage() != null ? t.getMessage() : "Sepet yüklenemedi");
                }
            });
        }

        @Override
        public void addToCart(int productId, int quantity, ResultCallback<Cart.AddToCartResponse> cb) {
            String bearer = bearerOrThrow(cb);
            if (bearer == null) return;

            api.addToCart(bearer, new Cart.AddToCartRequest(productId, quantity))
                    .enqueue(new Callback<ApiResponse<Cart.AddToCartResponse>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Cart.AddToCartResponse>> call, Response<ApiResponse<Cart.AddToCartResponse>> response) {
                            ApiResponse<Cart.AddToCartResponse> body = response.body();
                            if (body != null && body.ok && body.data != null) cb.onSuccess(body.data);
                            else cb.onError(body != null ? (body.error != null ? body.error : "Sepete eklenemedi") : "Sepete eklenemedi");
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Cart.AddToCartResponse>> call, Throwable t) {
                            cb.onError(t.getMessage() != null ? t.getMessage() : "Sepete eklenemedi");
                        }
                    });
        }



        @Override
        public void updateItem(int itemId, int quantity, ResultCallback<Void> cb) {
            String bearer = bearerOrThrow(cb);
            if (bearer == null) return;

            api.updateItem(bearer, itemId, new Cart.UpdateCartItemRequest(quantity))
                    .enqueue(new Callback<Util.BasicOk>() {
                        @Override
                        public void onResponse(Call<Util.ApiResponse<Util.BasicOk>> call,
                                               Response<Util.ApiResponse<Util.BasicOk>> response) {
                            ApiResponse<Util.BasicOk> body = response.body();
                            if (body != null && body.ok) cb.onSuccess(null);
                            else cb.onError(body != null ? (body.error != null ? body.error : "Sepet güncellenemedi") : "Sepet güncellenemedi");
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Util.BasicOk>> call, Throwable t) {
                            cb.onError(t.getMessage() != null ? t.getMessage() : "Sepet güncellenemedi");
                        }
                    });
        }

        @Override
        public void deleteItem(int itemId, ResultCallback<Void> cb) {
            String bearer = bearerOrThrow(cb);
            if (bearer == null) return;

            api.deleteItem(bearer, itemId).enqueue(new Callback<Util.BasicOk>>() {
                @Override
                public void onResponse(Call<ApiResponse<Util.BasicOk>> call,
                                       Response<ApiResponse<Util.BasicOk>> response) {
                    ApiResponse<Util.BasicOk> body = response.body();
                    if (body != null && body.ok) cb.onSuccess(null);
                    else cb.onError(body != null ? (body.error != null ? body.error : "Ürün silinemedi") : "Ürün silinemedi");
                }

                @Override
                public void onFailure(Call<ApiResponse<Util.BasicOk>> call, Throwable t) {
                    cb.onError(t.getMessage() != null ? t.getMessage() : "Ürün silinemedi");
                }
            });
        }
    }

    public static class OrderRepositoryImpl implements Repositories.OrderRepository {

        private final ApiService.OrderApi api;
        private final TokenStore tokenStore;

        public OrderRepositoryImpl(ApiService.OrderApi api, TokenStore tokenStore) {
            this.api = api;
            this.tokenStore = tokenStore;
        }

        private String bearerOrThrow(ResultCallback<?> cb) {
            String t = tokenStore.getToken();
            if (t == null || t.trim().isEmpty()) {
                cb.onError("Token yok");
                return null;
            }
            return "Bearer " + t;
        }

        @Override
        public void checkout(CheckoutRequest body, ResultCallback<CheckoutResponse> cb) {
            String bearer = bearerOrThrow(cb);
            if (bearer == null) return;

            api.checkout(bearer, body).enqueue(new Callback<ApiResponse<CheckoutResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<CheckoutResponse>> call, Response<ApiResponse<CheckoutResponse>> response) {
                    ApiResponse<CheckoutResponse> r = response.body();
                    if (r != null && r.ok && r.data != null) cb.onSuccess(r.data);
                    else cb.onError(r != null ? (r.error != null ? r.error : "Checkout hatası") : "Checkout hatası");
                }

                @Override
                public void onFailure(Call<ApiResponse<CheckoutResponse>> call, Throwable t) {
                    cb.onError(t.getMessage() != null ? t.getMessage() : "Checkout hatası");
                }
            });
        }

        @Override
        public void getOrders(ResultCallback<List<OrderSummaryDto>> cb) {
            String bearer = bearerOrThrow(cb);
            if (bearer == null) return;

            api.getOrders(bearer).enqueue(new Callback<ApiResponse<List<OrderSummaryDto>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<OrderSummaryDto>>> call, Response<ApiResponse<List<OrderSummaryDto>>> response) {
                    ApiResponse<List<OrderSummaryDto>> r = response.body();
                    if (r != null && r.ok && r.data != null) cb.onSuccess(r.data);
                    else cb.onError(r != null ? (r.error != null ? r.error : "Sipariş liste hatası") : "Sipariş liste hatası");
                }

                @Override
                public void onFailure(Call<ApiResponse<List<OrderSummaryDto>>> call, Throwable t) {
                    cb.onError(t.getMessage() != null ? t.getMessage() : "Sipariş liste hatası");
                }
            });
        }

        @Override
        public void getOrderDetail(int id, ResultCallback<OrderDetailDto> cb) {
            String bearer = bearerOrThrow(cb);
            if (bearer == null) return;

            api.getOrderDetail(bearer, id).enqueue(new Callback<ApiResponse<OrderDetailDto>>() {
                @Override
                public void onResponse(Call<ApiResponse<OrderDetailDto>> call, Response<ApiResponse<OrderDetailDto>> response) {
                    ApiResponse<OrderDetailDto> r = response.body();
                    if (r != null && r.ok && r.data != null) cb.onSuccess(r.data);
                    else cb.onError(r != null ? (r.error != null ? r.error : "Sipariş detay hatası") : "Sipariş detay hatası");
                }

                @Override
                public void onFailure(Call<ApiResponse<OrderDetailDto>> call, Throwable t) {
                    cb.onError(t.getMessage() != null ? t.getMessage() : "Sipariş detay hatası");
                }
            });
        }
    }
}

