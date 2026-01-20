package com.example.eticaretjava.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eticaretjava.model.Checkout.CheckoutRequest;
import com.example.eticaretjava.model.Checkout.CheckoutResponse;
import com.example.eticaretjava.model.Order.OrderDetailDto;
import com.example.eticaretjava.model.Order.OrderSummaryDto;
import com.example.eticaretjava.repo.Repositories;
import com.example.eticaretjava.repo.ResultCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrdersViewModel extends ViewModel {


    public static class OrdersState {
        public boolean loading = false;
        public String error = null;
        public List<OrderSummaryDto> orders = new ArrayList<>();
        public CheckoutResponse lastOrder = null;
        public OrderDetailDto orderDetail = null;
    }

    private final MutableLiveData<OrdersState> state =
            new MutableLiveData<>(new OrdersState());

    private final Repositories.OrderRepository orderRepo;

    public OrdersViewModel(Repositories.OrderRepository repo) {
        this.orderRepo = repo;
    }

    public LiveData<OrdersState> getState() {
        return state;
    }

    // ----------------------------
    // STATE COPY
    // ----------------------------
    private OrdersState copyState() {
        OrdersState cur = state.getValue() != null ? state.getValue() : new OrdersState();
        OrdersState n = new OrdersState();

        n.loading = cur.loading;
        n.error = cur.error;
        n.orders = new ArrayList<>(cur.orders);
        n.lastOrder = cur.lastOrder;
        n.orderDetail = cur.orderDetail;

        return n;
    }


    public void checkout(CheckoutRequest addr) {
        OrdersState s = copyState();
        s.loading = true;
        s.error = null;
        s.lastOrder = null;
        state.setValue(s);

        if (addr.idempotencyKey == null || addr.idempotencyKey.trim().isEmpty()) {
            addr.idempotencyKey = UUID.randomUUID().toString();
        }

        orderRepo.checkout(addr, new ResultCallback<CheckoutResponse>() {
            @Override
            public void onSuccess(CheckoutResponse data) {
                OrdersState ns = copyState();
                ns.loading = false;
                ns.lastOrder = data;
                ns.error = (data == null) ? "Checkout sonucu boş döndü" : null;
                state.postValue(ns);
            }

            @Override
            public void onError(String message) {
                OrdersState ns = copyState();
                ns.loading = false;
                ns.error = message != null ? message : "Checkout hatası";
                state.postValue(ns);
            }
        });
    }


    public void loadOrders() {
        OrdersState s = copyState();
        s.loading = true;
        s.error = null;
        state.setValue(s);

        orderRepo.getOrders(new ResultCallback<List<OrderSummaryDto>>() {
            @Override
            public void onSuccess(List<OrderSummaryDto> list) {
                OrdersState ns = copyState();
                ns.loading = false;
                ns.orders = list != null ? list : new ArrayList<>();
                ns.error = (list == null) ?
                        "Siparişler alınamadı (boş yanıt / bağlantı sorunu)" : null;
                state.postValue(ns);
            }

            @Override
            public void onError(String message) {
                OrdersState ns = copyState();
                ns.loading = false;
                ns.orders = new ArrayList<>();
                ns.error = message != null ? message : "Sipariş liste hatası";
                state.postValue(ns);
            }
        });
    }

    public void loadOrderDetail(int id) {
        OrdersState s = copyState();
        s.loading = true;
        s.error = null;
        s.orderDetail = null;
        state.setValue(s);

        orderRepo.getOrderDetail(id, new ResultCallback<OrderDetailDto>() {
            @Override
            public void onSuccess(OrderDetailDto detail) {
                OrdersState ns = copyState();
                ns.loading = false;
                ns.orderDetail = detail;
                ns.error = (detail == null) ? "Sipariş detayı boş döndü" : null;
                state.postValue(ns);
            }

            @Override
            public void onError(String message) {
                OrdersState ns = copyState();
                ns.loading = false;
                ns.error = message != null ? message : "Sipariş detay hatası";
                state.postValue(ns);
            }
        });
    }

    public void clearError() {
        OrdersState s = copyState();
        s.error = null;
        state.setValue(s);
    }
}
