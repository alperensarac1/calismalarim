package com.example.kargopaylasimjava.service;


import com.example.kargopaylasimjava.dto.ApiResp;
import com.example.kargopaylasimjava.dto.AddressDtos;
import com.example.kargopaylasimjava.dto.AuthDtos;
import com.example.kargopaylasimjava.dto.ReceiverDtos;
import com.example.kargopaylasimjava.dto.ShipmentDtos;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CargoApi {

    // Auth
    @POST("user_register.php")
    Call<ApiResp<AuthDtos.RegisterResp>> register(@Body AuthDtos.RegisterReq req);

    @POST("user_login.php")
    Call<ApiResp<AuthDtos.LoginResp>> login(@Body AuthDtos.LoginReq req);

    @GET("user_me.php")
    Call<ApiResp<AuthDtos.UserMeResp>> me();

    // Address
    @POST("address_create.php")
    Call<ApiResp<AddressDtos.AddressIdResp>> addressCreate(@Body AddressDtos.AddressCreateReq req);

    @POST("address_update.php")
    Call<ApiResp<Object>> addressUpdate(@Body AddressDtos.AddressUpdateReq req);

    @POST("address_detail_post.php")
    Call<ApiResp<AddressDtos.AddressDto>> addressDetailPost(@Body AddressDtos.AddressDetailReq req);

    @GET("address_list.php")
    Call<ApiResp<AddressDtos.AddressListResp>> addressList();

    @POST("address_delete.php")
    Call<ApiResp<Object>> addressDelete(@Body AddressDtos.AddressDeleteReq req);

    @GET("address_detail.php")
    Call<ApiResp<AddressDtos.AddressDto>> addressDetail(@Query("id") int id);

    @POST("address_set_default.php")
    Call<ApiResp<Object>> addressSetDefault(@Body AddressDtos.AddressSetDefaultReq req);

    // Receiver
    @POST("receiver_lookup.php")
    Call<ApiResp<ReceiverDtos.ReceiverLookupResp>> receiverLookup(@Body ReceiverDtos.ReceiverLookupReq req);

    // Shipment
    @POST("shipment_delete.php")
    Call<ApiResp<ShipmentDtos.ShipmentDeleteResp>> shipmentDelete(@Body ShipmentDtos.ShipmentDeleteReq req);

    @POST("shipment_create.php")
    Call<ApiResp<ShipmentDtos.ShipmentCreateResp>> shipmentCreate(@Body ShipmentDtos.ShipmentCreateReq req);

    @GET("shipment_list.php")
    Call<ApiResp<ShipmentDtos.ShipmentListResp>> shipmentList(@Query("type") String type);

    @GET("shipment_detail.php")
    Call<ApiResp<ShipmentDtos.ShipmentDetailResp>> shipmentDetail(@Query("id") int id);

    @POST("shipment_cancel.php")
    Call<ApiResp<Object>> shipmentCancel(@Body ShipmentDtos.ShipmentCancelReq req);

    @POST("shipment_regenerate_code.php")
    Call<ApiResp<ShipmentDtos.ShipmentRegenerateResp>> shipmentRegenerate(@Body ShipmentDtos.ShipmentRegenerateReq req);
}

