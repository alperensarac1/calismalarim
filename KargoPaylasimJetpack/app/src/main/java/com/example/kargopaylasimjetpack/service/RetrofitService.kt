package com.example.kargopaylasimjetpack.service


import com.example.kargopaylasimjetpack.model.AddressCreateData
import com.example.kargopaylasimjetpack.model.AddressCreateReq
import com.example.kargopaylasimjetpack.model.AddressIdReq
import com.example.kargopaylasimjetpack.model.AddressListData
import com.example.kargopaylasimjetpack.model.ApiResp
import com.example.kargopaylasimjetpack.model.CreateShipmentData
import com.example.kargopaylasimjetpack.model.CreateShipmentReq
import com.example.kargopaylasimjetpack.model.LoginData
import com.example.kargopaylasimjetpack.model.LoginReq
import com.example.kargopaylasimjetpack.model.LookupReceiverData
import com.example.kargopaylasimjetpack.model.LookupReceiverReq
import com.example.kargopaylasimjetpack.model.RegisterData
import com.example.kargopaylasimjetpack.model.RegisterReq
import com.example.kargopaylasimjetpack.model.ShipmentListData
import retrofit2.http.*

interface ApiService {

    @POST("user_login.php")
    suspend fun login(@Body req: LoginReq): ApiResp<LoginData>

    @POST("user_register.php")
    suspend fun register(@Body req: RegisterReq): ApiResp<RegisterData>

    @GET("address_list.php")
    suspend fun addressList(): ApiResp<AddressListData>

    @POST("address_create.php")
    suspend fun addressCreate(@Body req: AddressCreateReq): ApiResp<AddressCreateData>

    @POST("address_delete.php")
    suspend fun addressDelete(@Body req: AddressIdReq): ApiResp<Boolean>

    @POST("address_set_default.php")
    suspend fun addressSetDefault(@Body req: AddressIdReq): ApiResp<Boolean>

    @POST("receiver_lookup.php")
    suspend fun receiverLookup(@Body req: LookupReceiverReq): ApiResp<LookupReceiverData>

    @GET("shipment_list.php")
    suspend fun shipmentList(@Query("type") type: String = "all"): ApiResp<ShipmentListData>

    @POST("shipment_create.php")
    suspend fun shipmentCreate(@Body req: CreateShipmentReq): ApiResp<CreateShipmentData>
}
