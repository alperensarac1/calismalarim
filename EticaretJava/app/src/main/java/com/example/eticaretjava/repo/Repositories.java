package com.example.eticaretjava.repo;

import java.util.List;

import com.example.eticaretjava.model.Category.CategoryDto;
import com.example.eticaretjava.model.Product.ProductDto;
import com.example.eticaretjava.model.ProductListPage;
import com.example.eticaretjava.model.Cart.CartDto;
import com.example.eticaretjava.model.Cart.AddToCartResponse;
import com.example.eticaretjava.model.Checkout.CheckoutRequest;
import com.example.eticaretjava.model.Checkout.CheckoutResponse;
import com.example.eticaretjava.model.Order.OrderDetailDto;
import com.example.eticaretjava.model.Order.OrderSummaryDto;
import com.example.eticaretjava.model.User.UserDto;

public class Repositories {

    public interface AuthRepository {
        void login(String email, String password, ResultCallback<Void> cb);
        void register(String name, String email, String password, ResultCallback<Void> cb);
        void me(ResultCallback<UserDto> cb);
    }

    public interface ProductRepository {
        void getCategories(ResultCallback<List<CategoryDto>> cb);

        void getProducts(
                Integer cat, String q, Double min, Double max,
                Integer discount, String sort, Integer page, Integer per,
                ResultCallback<ProductListPage> cb
        );

        void getProduct(int id, ResultCallback<ProductDto> cb);
    }

    public interface CartRepository {
        void getCart(ResultCallback<CartDto> cb);
        void addToCart(int productId, int quantity, ResultCallback<AddToCartResponse> cb);
        void updateItem(int itemId, int quantity, ResultCallback<Void> cb);
        void deleteItem(int itemId, ResultCallback<Void> cb);
    }

    public interface OrderRepository {
        void checkout(CheckoutRequest body, ResultCallback<CheckoutResponse> cb);
        void getOrders(ResultCallback<java.util.List<OrderSummaryDto>> cb);
        void getOrderDetail(int id, ResultCallback<OrderDetailDto> cb);
    }
}

