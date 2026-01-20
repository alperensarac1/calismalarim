package com.example.eticaretjava.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eticaretjava.model.Cart;
import com.example.eticaretjava.repo.Repositories;
import com.example.eticaretjava.repo.ResultCallback;

public class CartViewModel extends ViewModel {

    public static class CartState {
        public boolean loading = false;
        public String error = null;
        public Cart.CartDto cart = null;
        public Integer busyItemId = null;
        public String lastAction = null;
    }

    private final MutableLiveData<CartState> state = new MutableLiveData<>(new CartState());
    private final Repositories.CartRepository repo;

    public CartViewModel(Repositories.CartRepository repo) {
        this.repo = repo;
    }

    public LiveData<CartState> getState() {
        return state;
    }

    private CartState copyState() {
        CartState cur = state.getValue() != null ? state.getValue() : new CartState();
        CartState n = new CartState();
        n.loading = cur.loading;
        n.error = cur.error;
        n.cart = cur.cart;
        n.busyItemId = cur.busyItemId;
        n.lastAction = cur.lastAction;
        return n;
    }

    // ----------------------------
    // LOAD CART
    // ----------------------------
    public void loadCart() {
        CartState s = copyState();
        s.loading = true;
        s.error = null;
        s.lastAction = null;
        state.setValue(s);

        repo.getCart(new ResultCallback<Cart.CartDto>() {
            @Override
            public void onSuccess(Cart.CartDto data) {
                CartState ns = copyState();
                ns.loading = false;
                ns.cart = data;
                ns.busyItemId = null;
                ns.lastAction = "load";
                state.postValue(ns);
            }

            @Override
            public void onError(String message) {
                CartState ns = copyState();
                ns.loading = false;
                ns.error = message != null ? message : "Sepet yüklenemedi";
                ns.busyItemId = null;
                ns.lastAction = "error";
                state.postValue(ns);
            }
        });
    }

    // ----------------------------
    // ADD ITEM (qty = 1)
    // ----------------------------
    public void addItem(int productId) {
        CartState s = copyState();
        s.busyItemId = productId;
        s.lastAction = "add";
        s.error = null;
        state.setValue(s);

        repo.addToCart(productId, 1, new ResultCallback<Cart.AddToCartResponse>() {
            @Override
            public void onSuccess(Cart.AddToCartResponse data) {
                // Sepeti güncelle
                loadCart();
            }

            @Override
            public void onError(String message) {
                CartState ns = copyState();
                ns.busyItemId = null;
                ns.error = message != null ? message : "Ürün sepete eklenemedi";
                ns.lastAction = "add_error";
                state.postValue(ns);
            }
        });
    }

    // ----------------------------
    // REMOVE ITEM (delete by itemId)
    // ----------------------------
    public void removeItem(int itemId) {
        CartState s = copyState();
        s.busyItemId = itemId;
        s.lastAction = "remove";
        s.error = null;
        state.setValue(s);

        repo.deleteItem(itemId, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                // Sepeti güncelle
                loadCart();
            }

            @Override
            public void onError(String message) {
                CartState ns = copyState();
                ns.busyItemId = null;
                ns.error = message != null ? message : "Ürün sepetten silinemedi";
                ns.lastAction = "remove_error";
                state.postValue(ns);
            }
        });
    }

    // ----------------------------
    // UPDATE ITEM QUANTITY
    // ----------------------------
    public void updateQuantity(int itemId, int quantity) {
        CartState s = copyState();
        s.busyItemId = itemId;
        s.lastAction = "update";
        s.error = null;
        state.setValue(s);

        repo.updateItem(itemId, quantity, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                // Sepeti güncelle
                loadCart();
            }

            @Override
            public void onError(String message) {
                CartState ns = copyState();
                ns.busyItemId = null;
                ns.error = message != null ? message : "Miktar güncellenemedi";
                ns.lastAction = "update_error";
                state.postValue(ns);
            }
        });
    }

    // ----------------------------
    // REFRESH
    // ----------------------------
    public void refresh() {
        loadCart();
    }
}
