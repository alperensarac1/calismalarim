package com.example.eticaretjava.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eticaretjava.model.Category.CategoryDto;
import com.example.eticaretjava.model.Product;
import com.example.eticaretjava.model.ProductListPage;
import com.example.eticaretjava.model.Product.ProductDto;
import com.example.eticaretjava.repo.Repositories;
import com.example.eticaretjava.repo.RepositoriesImpl;
import com.example.eticaretjava.repo.ResultCallback;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    // ----------------------------
    // FILTER MODEL
    // ----------------------------
    public static class ProductFilters {
        public Integer cat = null;
        public String q = null;
        public Double min = null;
        public Double max = null;
        public boolean discount = false;
        public String sort = "newest";
    }

    // ----------------------------
    // UI STATE
    // ----------------------------
    public static class HomeState {
        public boolean loading = false;
        public String error = null;
        public List<CategoryDto> categories = new ArrayList<>();
        public List<Product.ProductListDto> items = new ArrayList<>();
        public int page = 1;
        public int total = 0;
        public int per = 12;
        public ProductFilters filters = new ProductFilters();
    }

    private final MutableLiveData<HomeState> state =
            new MutableLiveData<>(new HomeState());

    private final Repositories.ProductRepository productRepo;
    private boolean pagingBusy = false;

    public HomeViewModel(Repositories.ProductRepository repo) {
        this.productRepo = repo;
    }

    public LiveData<HomeState> getState() {
        return state;
    }

    // ----------------------------
    // STATE COPY
    // ----------------------------
    private HomeState copyState() {
        HomeState cur = state.getValue() != null ? state.getValue() : new HomeState();
        HomeState n = new HomeState();

        n.loading = cur.loading;
        n.error = cur.error;
        n.categories = new ArrayList<>(cur.categories);
        n.items = new ArrayList<>(cur.items);
        n.page = cur.page;
        n.total = cur.total;
        n.per = cur.per;
        n.filters = cur.filters;

        return n;
    }

    // ----------------------------
    // LOAD CATEGORIES
    // ----------------------------
    public void loadCategories() {
        HomeState s = copyState();
        s.error = null;
        state.setValue(s);

        productRepo.getCategories(new ResultCallback<List<CategoryDto>>() {
            @Override
            public void onSuccess(List<CategoryDto> list) {
                HomeState ns = copyState();
                ns.categories = list;
                state.postValue(ns);
            }

            @Override
            public void onError(String message) {
                HomeState ns = copyState();
                ns.error = message != null ? message : "Kategori hatası";
                state.postValue(ns);
            }
        });
    }

    // ----------------------------
    // LOAD PRODUCTS (PAGING)
    // ----------------------------
    public void loadProducts(int page) {
        if (pagingBusy) return;
        pagingBusy = true;

        HomeState s = copyState();
        s.loading = true;
        s.error = null;
        s.page = page;
        state.setValue(s);

        HomeState cur = state.getValue();
        if (cur == null) return;

        productRepo.getProducts(
                cur.filters.cat,
                cur.filters.q,
                cur.filters.min,
                cur.filters.max,
                cur.filters.discount ? 1 : null,
                cur.filters.sort,
                page,
                cur.per,
                new ResultCallback<ProductListPage>() {

                    @Override
                    public void onSuccess(ProductListPage resp) {
                        HomeState ns = copyState();
                        ns.loading = false;
                        ns.items = resp.items;
                        ns.total = resp.total;
                        ns.page = resp.page;
                        ns.per = resp.per;
                        pagingBusy = false;
                        state.postValue(ns);
                    }

                    @Override
                    public void onError(String message) {
                        HomeState ns = copyState();
                        ns.loading = false;
                        ns.error = message != null ? message : "Ürün hatası";
                        pagingBusy = false;
                        state.postValue(ns);
                    }
                }
        );
    }

    // ----------------------------
    // FILTER UPDATE
    // ----------------------------
    public void setFilters(ProductFilters newFilters) {
        HomeState s = copyState();
        s.filters = newFilters;
        s.page = 1;
        state.setValue(s);
        loadProducts(1);
    }
}
