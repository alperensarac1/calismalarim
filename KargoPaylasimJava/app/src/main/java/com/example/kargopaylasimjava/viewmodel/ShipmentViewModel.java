package com.example.kargopaylasimjava.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.kargopaylasimjava.dto.ApiResp;
import com.example.kargopaylasimjava.dto.ReceiverDtos;
import com.example.kargopaylasimjava.dto.ShipmentDtos;
import com.example.kargopaylasimjava.model.UiState;
import com.example.kargopaylasimjava.repo.CargoRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShipmentViewModel extends ViewModel {

    private final CargoRepository repo;

    public MutableLiveData<UiState<List<ShipmentDtos.ShipmentDto>>> listState = new MutableLiveData<>(UiState.idle());
    public MutableLiveData<UiState<ShipmentDtos.ShipmentCreateResp>> createState = new MutableLiveData<>(UiState.idle());
    public MutableLiveData<UiState<ShipmentDtos.ShipmentDetailDto>> detailState = new MutableLiveData<>(UiState.idle());
    public MutableLiveData<UiState<ReceiverDtos.ReceiverLookupResp>> lookupState = new MutableLiveData<>(UiState.idle());
    public MutableLiveData<UiState<ShipmentDtos.ShipmentRegenerateResp>> regenerateState = new MutableLiveData<>(UiState.idle());
    public MutableLiveData<UiState<ShipmentDtos.ShipmentDeleteResp>> deleteState = new MutableLiveData<>(UiState.idle());

    public ShipmentViewModel(CargoRepository repo) {
        this.repo = repo;
    }

    public void loadShipments() {
        listState.setValue(UiState.loading());

        repo.shipmentList("all").enqueue(new Callback<ApiResp<ShipmentDtos.ShipmentListResp>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResp<ShipmentDtos.ShipmentListResp>> call,
                                   @NonNull Response<ApiResp<ShipmentDtos.ShipmentListResp>> response) {
                ApiResp<ShipmentDtos.ShipmentListResp> body = response.body();
                if (response.isSuccessful() && body != null && body.ok && body.data != null) {
                    listState.setValue(UiState.success(body.data.shipments));
                } else {
                    String msg = (body != null && body.error != null) ? body.error : "Load failed";
                    listState.setValue(UiState.error(msg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResp<ShipmentDtos.ShipmentListResp>> call, @NonNull Throwable t) {
                listState.setValue(UiState.error(t.getMessage() != null ? t.getMessage() : "Network error"));
            }
        });
    }

    public void lookupReceiver(String phone) {
        lookupState.setValue(UiState.loading());

        repo.receiverLookup(phone).enqueue(new Callback<ApiResp<ReceiverDtos.ReceiverLookupResp>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResp<ReceiverDtos.ReceiverLookupResp>> call,
                                   @NonNull Response<ApiResp<ReceiverDtos.ReceiverLookupResp>> response) {
                ApiResp<ReceiverDtos.ReceiverLookupResp> body = response.body();
                if (response.isSuccessful() && body != null && body.ok && body.data != null) {
                    lookupState.setValue(UiState.success(body.data));
                } else {
                    String msg = (body != null && body.error != null) ? body.error : "Lookup failed";
                    lookupState.setValue(UiState.error(msg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResp<ReceiverDtos.ReceiverLookupResp>> call, @NonNull Throwable t) {
                lookupState.setValue(UiState.error(t.getMessage() != null ? t.getMessage() : "Network error"));
            }
        });
    }

    public void createShipment(String receiverPhone, Integer senderAddressId) {
        createState.setValue(UiState.loading());

        repo.shipmentCreate(receiverPhone, senderAddressId).enqueue(new Callback<ApiResp<ShipmentDtos.ShipmentCreateResp>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResp<ShipmentDtos.ShipmentCreateResp>> call,
                                   @NonNull Response<ApiResp<ShipmentDtos.ShipmentCreateResp>> response) {
                ApiResp<ShipmentDtos.ShipmentCreateResp> body = response.body();
                if (response.isSuccessful() && body != null && body.ok && body.data != null) {
                    createState.setValue(UiState.success(body.data));
                } else {
                    String msg = (body != null && body.error != null) ? body.error : "Create failed";
                    createState.setValue(UiState.error(msg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResp<ShipmentDtos.ShipmentCreateResp>> call, @NonNull Throwable t) {
                createState.setValue(UiState.error(t.getMessage() != null ? t.getMessage() : "Network error"));
            }
        });
    }

    public void loadDetail(int id) {
        detailState.setValue(UiState.loading());

        repo.shipmentDetail(id).enqueue(new Callback<ApiResp<ShipmentDtos.ShipmentDetailResp>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResp<ShipmentDtos.ShipmentDetailResp>> call,
                                   @NonNull Response<ApiResp<ShipmentDtos.ShipmentDetailResp>> response) {
                ApiResp<ShipmentDtos.ShipmentDetailResp> body = response.body();
                if (response.isSuccessful() && body != null && body.ok && body.data != null && body.data.shipment != null) {
                    detailState.setValue(UiState.success(body.data.shipment));
                } else {
                    String msg = (body != null && body.error != null) ? body.error : "Detail failed";
                    detailState.setValue(UiState.error(msg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResp<ShipmentDtos.ShipmentDetailResp>> call, @NonNull Throwable t) {
                detailState.setValue(UiState.error(t.getMessage() != null ? t.getMessage() : "Network error"));
            }
        });
    }

    public void regenerateCode(int shipmentId) {
        regenerateState.setValue(UiState.loading());

        repo.shipmentRegenerate(shipmentId).enqueue(new Callback<ApiResp<ShipmentDtos.ShipmentRegenerateResp>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResp<ShipmentDtos.ShipmentRegenerateResp>> call,
                                   @NonNull Response<ApiResp<ShipmentDtos.ShipmentRegenerateResp>> response) {
                ApiResp<ShipmentDtos.ShipmentRegenerateResp> body = response.body();
                if (response.isSuccessful() && body != null && body.ok && body.data != null) {
                    regenerateState.setValue(UiState.success(body.data));
                } else {
                    String msg = (body != null && body.error != null) ? body.error : "Regenerate failed";
                    regenerateState.setValue(UiState.error(msg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResp<ShipmentDtos.ShipmentRegenerateResp>> call, @NonNull Throwable t) {
                regenerateState.setValue(UiState.error(t.getMessage() != null ? t.getMessage() : "Network error"));
            }
        });
    }

    public void deleteShipment(int shipmentId) {
        deleteState.setValue(UiState.loading());

        repo.shipmentDelete(shipmentId).enqueue(new Callback<ApiResp<ShipmentDtos.ShipmentDeleteResp>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResp<ShipmentDtos.ShipmentDeleteResp>> call,
                                   @NonNull Response<ApiResp<ShipmentDtos.ShipmentDeleteResp>> response) {
                ApiResp<ShipmentDtos.ShipmentDeleteResp> body = response.body();
                if (response.isSuccessful() && body != null && body.ok && body.data != null) {
                    deleteState.setValue(UiState.success(body.data));
                } else {
                    String msg = (body != null && body.error != null) ? body.error : "Delete failed";
                    deleteState.setValue(UiState.error(msg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResp<ShipmentDtos.ShipmentDeleteResp>> call, @NonNull Throwable t) {
                deleteState.setValue(UiState.error(t.getMessage() != null ? t.getMessage() : "Network error"));
            }
        });
    }
}

