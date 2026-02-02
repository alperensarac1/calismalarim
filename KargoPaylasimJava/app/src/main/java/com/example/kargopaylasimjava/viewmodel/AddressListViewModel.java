package com.example.kargopaylasimjava.viewmodel;


import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.kargopaylasimjava.dto.AddressDtos;
import com.example.kargopaylasimjava.dto.ApiResp;
import com.example.kargopaylasimjava.model.UiState;
import com.example.kargopaylasimjava.repo.CargoRepository;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressListViewModel extends ViewModel {

    private final CargoRepository repo;

    public MutableLiveData<UiState<List<AddressDtos.AddressDto>>> listState = new MutableLiveData<>(UiState.idle());
    public MutableLiveData<UiState<Void>> deleteState = new MutableLiveData<>(UiState.idle());
    public MutableLiveData<UiState<Void>> setDefaultState = new MutableLiveData<>(UiState.idle());

    public AddressListViewModel(CargoRepository repo) {
        this.repo = repo;
    }

    public void load() {
        listState.setValue(UiState.loading());

        repo.addressList().enqueue(new Callback<ApiResp<AddressDtos.AddressListResp>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResp<AddressDtos.AddressListResp>> call,
                                   @NonNull Response<ApiResp<AddressDtos.AddressListResp>> response) {
                ApiResp<AddressDtos.AddressListResp> body = response.body();
                if (response.isSuccessful() && body != null && body.ok && body.data != null) {
                    List<AddressDtos.AddressDto> items = body.data.items != null ? body.data.items : Collections.emptyList();
                    listState.setValue(UiState.success(items));
                } else {
                    String msg = (body != null && body.error != null) ? body.error : "Load failed";
                    listState.setValue(UiState.error(msg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResp<AddressDtos.AddressListResp>> call, @NonNull Throwable t) {
                listState.setValue(UiState.error(t.getMessage() != null ? t.getMessage() : "Network error"));
            }
        });
    }

    public void setDefault(int id) {
        setDefaultState.setValue(UiState.loading());

        repo.addressSetDefault(id).enqueue(new Callback<ApiResp<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResp<Object>> call, @NonNull Response<ApiResp<Object>> response) {
                ApiResp<Object> body = response.body();
                if (response.isSuccessful() && body != null && body.ok) {
                    setDefaultState.setValue(UiState.success(null));
                } else {
                    String msg = (body != null && body.error != null) ? body.error : "Default failed";
                    setDefaultState.setValue(UiState.error(msg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResp<Object>> call, @NonNull Throwable t) {
                setDefaultState.setValue(UiState.error(t.getMessage() != null ? t.getMessage() : "Network error"));
            }
        });
    }

    public void delete(int id) {
        deleteState.setValue(UiState.loading());

        repo.addressDelete(id).enqueue(new Callback<ApiResp<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResp<Object>> call, @NonNull Response<ApiResp<Object>> response) {
                ApiResp<Object> body = response.body();
                if (response.isSuccessful() && body != null && body.ok) {
                    deleteState.setValue(UiState.success(null));
                } else {
                    String msg = (body != null && body.error != null) ? body.error : "Delete failed";
                    deleteState.setValue(UiState.error(msg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResp<Object>> call, @NonNull Throwable t) {
                deleteState.setValue(UiState.error(t.getMessage() != null ? t.getMessage() : "Network error"));
            }
        });
    }
}

