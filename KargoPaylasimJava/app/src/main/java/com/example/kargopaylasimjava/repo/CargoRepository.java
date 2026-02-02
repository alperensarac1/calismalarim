package com.example.kargopaylasimjava.repo;
import com.example.kargopaylasimjava.dto.AddressDtos;
import com.example.kargopaylasimjava.dto.ApiResp;
import com.example.kargopaylasimjava.dto.AuthDtos;
import com.example.kargopaylasimjava.dto.ReceiverDtos;
import com.example.kargopaylasimjava.dto.ShipmentDtos;
import com.example.kargopaylasimjava.service.CargoApi;
import com.example.kargopaylasimjava.service.TokenStore;

import retrofit2.Call;

public class CargoRepository {

    private final CargoApi api;
    private final TokenStore tokenStore;

    public CargoRepository(CargoApi api, TokenStore tokenStore) {
        this.api = api;
        this.tokenStore = tokenStore;
    }

    public Call<ApiResp<AuthDtos.LoginResp>> login(String phone, String password) {
        return api.login(new AuthDtos.LoginReq(phone, password));
    }

    public Call<ApiResp<AuthDtos.RegisterResp>> register(AuthDtos.RegisterReq req) {
        return api.register(req);
    }

    public Call<ApiResp<AuthDtos.UserMeResp>> me() {
        return api.me();
    }

    public Call<ApiResp<ReceiverDtos.ReceiverLookupResp>> receiverLookup(String phone) {
        return api.receiverLookup(new ReceiverDtos.ReceiverLookupReq(phone));
    }

    public Call<ApiResp<ShipmentDtos.ShipmentCreateResp>> shipmentCreate(String receiverPhone, Integer senderAddressId) {
        return api.shipmentCreate(new ShipmentDtos.ShipmentCreateReq(receiverPhone, senderAddressId));
    }

    public Call<ApiResp<ShipmentDtos.ShipmentListResp>> shipmentList(String type) {
        return api.shipmentList(type);
    }

    public Call<ApiResp<AddressDtos.AddressDto>> addressDetailPost(int id) {
        String token = tokenStore.getToken();
        if (token == null) token = "";
        return api.addressDetailPost(new AddressDtos.AddressDetailReq(id, token));
    }

    public Call<ApiResp<ShipmentDtos.ShipmentDeleteResp>> shipmentDelete(int id) {
        return api.shipmentDelete(new ShipmentDtos.ShipmentDeleteReq(id));
    }

    public Call<ApiResp<ShipmentDtos.ShipmentDetailResp>> shipmentDetail(int id) {
        return api.shipmentDetail(id);
    }

    public Call<ApiResp<Object>> shipmentCancel(int shipmentId) {
        return api.shipmentCancel(new ShipmentDtos.ShipmentCancelReq(shipmentId));
    }

    public Call<ApiResp<ShipmentDtos.ShipmentRegenerateResp>> shipmentRegenerate(int shipmentId) {
        return api.shipmentRegenerate(new ShipmentDtos.ShipmentRegenerateReq(shipmentId));
    }

    public Call<ApiResp<AddressDtos.AddressListResp>> addressList() {
        return api.addressList();
    }

    public Call<ApiResp<AddressDtos.AddressIdResp>> addressCreate(AddressDtos.AddressCreateReq req) {
        return api.addressCreate(req);
    }

    public Call<ApiResp<Object>> addressUpdate(AddressDtos.AddressUpdateReq req) {
        return api.addressUpdate(req);
    }

    public Call<ApiResp<Object>> addressSetDefault(int id) {
        return api.addressSetDefault(new AddressDtos.AddressSetDefaultReq(id));
    }

    public Call<ApiResp<Object>> addressDelete(int id) {
        return api.addressDelete(new AddressDtos.AddressDeleteReq(id));
    }
}

