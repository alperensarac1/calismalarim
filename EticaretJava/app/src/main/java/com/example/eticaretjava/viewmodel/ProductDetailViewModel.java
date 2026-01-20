package com.example.eticaretjava.viewmodel;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eticaretjava.model.Product.ProductDto;
import com.example.eticaretjava.repo.Repositories;
import com.example.eticaretjava.repo.ResultCallback;

public class ProductDetailViewModel extends ViewModel {

    public static class DetailState {
        public boolean loading = false;
        public String error = null;
        public ProductDto product = null;
        public boolean addSuccess = false;
    }

    private final MutableLiveData<DetailState> state = new MutableLiveData<>(new DetailState());

    private final Repositories.ProductRepository productRepo;
    private final Repositories.CartRepository cartRepo;

    public ProductDetailViewModel(Repositories.ProductRepository productRepo, Repositories.CartRepository cartRepo) {
        this.productRepo = productRepo;
        this.cartRepo = cartRepo;
    }

    public LiveData<DetailState> getState() {
        return state;
    }

    private DetailState copyState() {
        DetailState cur = state.getValue() != null ? state.getValue() : new DetailState();
        DetailState n = new DetailState();
        n.loading = cur.loading;
        n.error = cur.error;
        n.product = cur.product;
        n.addSuccess = cur.addSuccess;
        return n;
    }

    public void load(int id) {
        DetailState s = copyState();
        s.loading = true;
        s.error = null;
        s.addSuccess = false;
        state.setValue(s);

        productRepo.getProduct(id, new ResultCallback<ProductDto>() {
            @Override
            public void onSuccess(ProductDto data) {
                DetailState ns = copyState();
                ns.loading = false;
                ns.product = data;
                state.postValue(ns);
            }

            @Override
            public void onError(String message) {
                DetailState ns = copyState();
                ns.loading = false;
                ns.error = message != null ? message : "Detay hatası";
                state.postValue(ns);
            }
        });
    }

    public void addToCart(int productId, int qty) {
        DetailState s = copyState();
        s.loading = true;
        s.error = null;
        s.addSuccess = false;
        state.setValue(s);

        cartRepo.addToCart(productId, qty, new ResultCallback<com.example.eticaretkotlin.model.Cart.AddToCartResponse>() {
            @Override
            public void onSuccess(com.example.eticaretkotlin.model.Cart.AddToCartResponse data) {
                DetailState ns = copyState();
                ns.loading = false;
                ns.addSuccess = true;
                state.postValue(ns);
            }

            @Override
            public void onError(String message) {
                DetailState ns = copyState();
                ns.loading = false;
                ns.error = message != null ? message : "Sepete ekleme hatası";
                state.postValue(ns);
            }
        });
    }

    public void clearFlags() {
        DetailState ns = copyState();
        ns.addSuccess = false;
        ns.error = null;
        state.setValue(ns);
    }
}
