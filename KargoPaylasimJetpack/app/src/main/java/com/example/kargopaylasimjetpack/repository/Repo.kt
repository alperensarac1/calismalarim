package com.example.kargopaylasimjetpack.repository

import com.example.kargopaylasimjetpack.model.AddressCreateReq
import com.example.kargopaylasimjetpack.model.AddressIdReq
import com.example.kargopaylasimjetpack.model.CreateShipmentReq
import com.example.kargopaylasimjetpack.model.LoginReq
import com.example.kargopaylasimjetpack.model.LookupReceiverReq
import com.example.kargopaylasimjetpack.model.RegisterReq
import com.example.kargopaylasimjetpack.service.ApiService


class Repo(private val api: ApiService) {

    suspend fun login(phone: String, password: String) =
        api.login(LoginReq(phone, password))

    suspend fun register(req: RegisterReq) = api.register(req)

    suspend fun shipmentListAll() = api.shipmentList("all")

    suspend fun addressList() = api.addressList()

    suspend fun addressCreate(req: AddressCreateReq) = api.addressCreate(req)

    suspend fun addressDelete(id: Int) = api.addressDelete(AddressIdReq(id))

    suspend fun addressSetDefault(id: Int) = api.addressSetDefault(AddressIdReq(id))

    suspend fun receiverLookup(phone: String) = api.receiverLookup(LookupReceiverReq(phone))

    suspend fun shipmentCreate(receiverPhone: String) =
        api.shipmentCreate(CreateShipmentReq(receiver_phone = receiverPhone, sender_address_id = null))
}
