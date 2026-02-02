package com.example.kargopaylasimkotlin.repo

import com.example.kargopaylasimkotlin.dto.*
import com.example.kargopaylasimkotlin.service.CargoApi
import com.example.kargopaylasimkotlin.service.TokenStore

class CargoRepository(
    private val api: CargoApi,
    private val tokenStore: TokenStore
) {

    suspend fun login(phone: String, password: String) =
        api.login(LoginReq(phone, password))

    suspend fun register(req: RegisterReq) = api.register(req)
    suspend fun me() = api.me()

    suspend fun receiverLookup(phone: String) =
        api.receiverLookup(ReceiverLookupReq(phone))

    suspend fun shipmentCreate(receiverPhone: String, senderAddressId: Int?) =
        api.shipmentCreate(ShipmentCreateReq(receiverPhone, senderAddressId))

    suspend fun shipmentList(type: String = "all") = api.shipmentList(type)

    suspend fun addressDetail(id: Int): ApiResp<AddressDto> {
        val token = tokenStore.getToken().orEmpty()
        return api.addressDetailPost(AddressDetailReq(id = id, token = token))
    }


    suspend fun shipmentDelete(id: Int): ApiResp<ShipmentDeleteResp> {
        return api.shipmentDelete(ShipmentDeleteReq(id))
    }

    suspend fun shipmentDetail(id: Int) = api.shipmentDetail(id)
    suspend fun shipmentCancel(id: Int) = api.shipmentCancel(ShipmentCancelReq(id))
    suspend fun shipmentRegenerate(id: Int) = api.shipmentRegenerate(ShipmentRegenerateReq(id))

    suspend fun addressList() = api.addressList()
    suspend fun addressCreate(req: AddressCreateReq) = api.addressCreate(req)
    suspend fun addressUpdate(req: AddressUpdateReq) = api.addressUpdate(req)

    suspend fun addressSetDefault(id: Int) = api.addressSetDefault(AddressSetDefaultReq(id))
    suspend fun addressDelete(id: Int) = api.addressDelete(AddressDeleteReq(id))
}
