package com.example.kargopaylasimjava.viewmodel;


import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.kargopaylasimjava.dto.AddressDtos;
import com.example.kargopaylasimjava.dto.ApiResp;
import com.example.kargopaylasimjava.model.UiState;
import com.example.kargopaylasimjava.repo.CargoRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressViewModel extends ViewModel {

    private final CargoRepository repo;

    public MutableLiveData<UiState<AddressDtos.AddressDto>> defaultState = new MutableLiveData<>(UiState.idle());
    public MutableLiveData<UiState<Void>> saveState = new MutableLiveData<>(UiState.idle());

    public AddressViewModel(CargoRepository repo) {
        this.repo = repo;
    }

    public void loadDefault() {
        defaultState.setValue(UiState.loading());

        repo.addressList().enqueue(new Callback<ApiResp<AddressDtos.AddressListResp>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResp<AddressDtos.AddressListResp>> call,
                                   @NonNull Response<ApiResp<AddressDtos.AddressListResp>> response) {
                ApiResp<AddressDtos.AddressListResp> body = response.body();
                if (!(response.isSuccessful() && body != null && body.ok && body.data != null)) {
                    String msg = (body != null && body.error != null) ? body.error : "Load failed";
                    defaultState.setValue(UiState.error(msg));
                    return;
                }

                List<AddressDtos.AddressDto> list = body.data.items;
                if (list == null || list.isEmpty()) {
                    defaultState.setValue(UiState.error("Adres bulunamadı"));
                    return;
                }

                AddressDtos.AddressDto def = null;
                for (AddressDtos.AddressDto a : list) {
                    if (a != null && a.is_default == 1) { def = a; break; }
                }
                if (def == null) def = list.get(0);
                defaultState.setValue(UiState.success(def));
            }

            @Override
            public void onFailure(@NonNull Call<ApiResp<AddressDtos.AddressListResp>> call, @NonNull Throwable t) {
                defaultState.setValue(UiState.error(t.getMessage() != null ? t.getMessage() : "Network error"));
            }
        });
    }

    public void saveOrCreate(int addressId,
                             String title, String city, String district,
                             String neighborhood, String line, String postalCode) {
        saveState.setValue(UiState.loading());

        String n = (neighborhood != null && neighborhood.trim().isEmpty()) ? null : neighborhood;
        String p = (postalCode != null && postalCode.trim().isEmpty()) ? null : postalCode;

        if (addressId > 0) {
            AddressDtos.AddressUpdateReq req = new AddressDtos.AddressUpdateReq(addressId, title, city, district, n, line, p);
            repo.addressUpdate(req).enqueue(new Callback<ApiResp<Object>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResp<Object>> call, @NonNull Response<ApiResp<Object>> response) {
                    ApiResp<Object> body = response.body();
                    if (response.isSuccessful() && body != null && body.ok) {
                        saveState.setValue(UiState.success(null));
                    } else {
                        String msg = (body != null && body.error != null) ? body.error : "Save failed";
                        saveState.setValue(UiState.error(msg));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResp<Object>> call, @NonNull Throwable t) {
                    saveState.setValue(UiState.error(t.getMessage() != null ? t.getMessage() : "Network error"));
                }
            });
        } else {
            AddressDtos.AddressCreateReq req = new AddressDtos.AddressCreateReq(title, city, district, n, line, p);
            repo.addressCreate(req).enqueue(new Callback<ApiResp<AddressDtos.AddressIdResp>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResp<AddressDtos.AddressIdResp>> call,
                                       @NonNull Response<ApiResp<AddressDtos.AddressIdResp>> response) {
                    ApiResp<AddressDtos.AddressIdResp> body = response.body();
                    if (response.isSuccessful() && body != null && body.ok) {
                        saveState.setValue(UiState.success(null));
                    } else {
                        String msg = (body != null && body.error != null) ? body.error : "Create failed";
                        saveState.setValue(UiState.error(msg));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResp<AddressDtos.AddressIdResp>> call, @NonNull Throwable t) {
                    saveState.setValue(UiState.error(t.getMessage() != null ? t.getMessage() : "Network error"));
                }
            });
        }
    }

    public void loadById(int id) {
        defaultState.setValue(UiState.loading());

        repo.addressDetailPost(id).enqueue(new Callback<ApiResp<AddressDtos.AddressDto>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResp<AddressDtos.AddressDto>> call,
                                   @NonNull Response<ApiResp<AddressDtos.AddressDto>> response) {
                ApiResp<AddressDtos.AddressDto> body = response.body();
                if (response.isSuccessful() && body != null && body.ok && body.data != null) {
                    defaultState.setValue(UiState.success(body.data));
                } else {
                    String msg = (body != null && body.error != null) ? body.error : "Load failed";
                    defaultState.setValue(UiState.error(msg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResp<AddressDtos.AddressDto>> call, @NonNull Throwable t) {
                defaultState.setValue(UiState.error(t.getMessage() != null ? t.getMessage() : "Network error"));
            }
        });
    }
}
