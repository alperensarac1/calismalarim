package com.example.kargopaylasimkotlin.service

import com.example.kargopaylasimkotlin.dto.AddressCreateReq
import com.example.kargopaylasimkotlin.dto.AddressDeleteReq
import com.example.kargopaylasimkotlin.dto.AddressDetailReq
import com.example.kargopaylasimkotlin.dto.AddressDto
import com.example.kargopaylasimkotlin.dto.AddressIdResp
import com.example.kargopaylasimkotlin.dto.AddressListResp
import com.example.kargopaylasimkotlin.dto.AddressSetDefaultReq
import com.example.kargopaylasimkotlin.dto.AddressUpdateReq
import com.example.kargopaylasimkotlin.dto.ApiResp
import com.example.kargopaylasimkotlin.dto.LoginReq
import com.example.kargopaylasimkotlin.dto.LoginResp
import com.example.kargopaylasimkotlin.dto.ReceiverLookupReq
import com.example.kargopaylasimkotlin.dto.ReceiverLookupResp
import com.example.kargopaylasimkotlin.dto.RegisterReq
import com.example.kargopaylasimkotlin.dto.RegisterResp
import com.example.kargopaylasimkotlin.dto.ShipmentCancelReq
import com.example.kargopaylasimkotlin.dto.ShipmentCreateReq
import com.example.kargopaylasimkotlin.dto.ShipmentCreateResp
import com.example.kargopaylasimkotlin.dto.ShipmentDeleteReq
import com.example.kargopaylasimkotlin.dto.ShipmentDeleteResp
import com.example.kargopaylasimkotlin.dto.ShipmentDetailResp
import com.example.kargopaylasimkotlin.dto.ShipmentListResp
import com.example.kargopaylasimkotlin.dto.ShipmentRegenerateReq
import com.example.kargopaylasimkotlin.dto.ShipmentRegenerateResp
import com.example.kargopaylasimkotlin.dto.UserMeResp
import okhttp3.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface CargoApi {

    // Auth
    @POST("user_register.php")
    suspend fun register(@Body req: RegisterReq): ApiResp<RegisterResp>

    @POST("user_login.php")
    suspend fun login(@Body req: LoginReq): ApiResp<LoginResp>

    @GET("user_me.php")
    suspend fun me(): ApiResp<UserMeResp>

    @POST("address_create.php")
    suspend fun addressCreate(@Body req: AddressCreateReq): ApiResp<AddressIdResp>

    @POST("address_update.php")
    suspend fun addressUpdate(@Body req: AddressUpdateReq): ApiResp<Any>

    @POST("address_detail_post.php")
    suspend fun addressDetailPost(@Body req: AddressDetailReq): ApiResp<AddressDto>

    @GET("address_list.php")
    suspend fun addressList(): ApiResp<AddressListResp>


    @POST("address_delete.php")
    suspend fun addressDelete(@Body req: AddressDeleteReq): ApiResp<Any>

    @GET("address_detail.php")
    suspend fun addressDetail(@Query("id") id: Int): ApiResp<AddressDto>

    @POST("address_set_default.php")
    suspend fun addressSetDefault(@Body req: AddressSetDefaultReq): ApiResp<Any>

    @POST("receiver_lookup.php")
    suspend fun receiverLookup(@Body req: ReceiverLookupReq): ApiResp<ReceiverLookupResp>

    @POST("shipment_delete.php")
    suspend fun shipmentDelete(
        @Body req: ShipmentDeleteReq
    ): ApiResp<ShipmentDeleteResp>

    @POST("shipment_create.php")
    suspend fun shipmentCreate(@Body req: ShipmentCreateReq): ApiResp<ShipmentCreateResp>

    @GET("shipment_list.php")
    suspend fun shipmentList(@Query("type") type: String = "all"): ApiResp<ShipmentListResp>

    @GET("shipment_detail.php")
    suspend fun shipmentDetail(@Query("id") id: Int): ApiResp<ShipmentDetailResp>

    @POST("shipment_cancel.php")
    suspend fun shipmentCancel(@Body req: ShipmentCancelReq): ApiResp<Any>

    @POST("shipment_regenerate_code.php")
    suspend fun shipmentRegenerate(@Body req: ShipmentRegenerateReq): ApiResp<ShipmentRegenerateResp>

}

