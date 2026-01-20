package com.example.eticaretjava.repo;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.eticaretjava.data.TokenStore;
import com.example.eticaretjava.viewmodel.AuthViewModel;
import com.example.eticaretjava.viewmodel.CartViewModel;
import com.example.eticaretjava.viewmodel.HomeViewModel;
import com.example.eticaretjava.viewmodel.OrdersViewModel;
import com.example.eticaretjava.viewmodel.ProductDetailViewModel;

public class Factories {

    public static class AuthVMFactory implements ViewModelProvider.Factory {
        private final TokenStore tokenStore;
        private final RepositoriesImpl.AuthRepositoryImpl repo;

        public AuthVMFactory(Context ctx) {
            tokenStore = new TokenStore(ctx.getApplicationContext());
            repo = new RepositoriesImpl.AuthRepositoryImpl(RetrofitProvider.authApi, tokenStore);
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new AuthViewModel(repo);
        }
    }

    public static class HomeVMFactory implements ViewModelProvider.Factory {
        private final RepositoriesImpl.ProductRepositoryImpl repo =
                new RepositoriesImpl.ProductRepositoryImpl(RetrofitProvider.productApi);

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new HomeViewModel(repo);
        }
    }

    public static class ProductDetailVMFactory implements ViewModelProvider.Factory {
        private final RepositoriesImpl.ProductRepositoryImpl prodRepo =
                new RepositoriesImpl.ProductRepositoryImpl(RetrofitProvider.productApi);

        private final RepositoriesImpl.CartRepositoryImpl cartRepo;

        public ProductDetailVMFactory(Context ctx) {
            cartRepo = new RepositoriesImpl.CartRepositoryImpl(
                    RetrofitProvider.cartApi,
                    new TokenStore(ctx.getApplicationContext())
            );
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ProductDetailViewModel(prodRepo, cartRepo);
        }
    }

    public static class CartVMFactory implements ViewModelProvider.Factory {
        private final RepositoriesImpl.CartRepositoryImpl repo;

        public CartVMFactory(Context ctx) {
            repo = new RepositoriesImpl.CartRepositoryImpl(
                    RetrofitProvider.cartApi,
                    new TokenStore(ctx.getApplicationContext())
            );
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CartViewModel(repo);
        }
    }

    public static class OrdersVMFactory implements ViewModelProvider.Factory {
        private final RepositoriesImpl.OrderRepositoryImpl repo;

        public OrdersVMFactory(Context ctx) {
            repo = new RepositoriesImpl.OrderRepositoryImpl(
                    RetrofitProvider.orderApi,
                    new TokenStore(ctx.getApplicationContext())
            );
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new OrdersViewModel(repo);
        }
    }
}

