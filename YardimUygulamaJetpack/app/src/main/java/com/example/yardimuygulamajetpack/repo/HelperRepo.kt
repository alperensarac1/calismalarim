package com.example.yardimuygulamajetpack.repo

import com.example.yardimuygulamajetpack.model.HelpAcceptBody
import com.example.yardimuygulamajetpack.service.ApiClient

class HelperRepo {
    private val api = ApiClient.api

    suspend fun listOpen(helperId: Long) = api.listOpen(helperId)

    suspend fun accept(reqId: Long, helperId: Long) =
        api.accept(HelpAcceptBody(request_id = reqId, helper_id = helperId))

    suspend fun myAccepted(helperId: Long) = api.myAccepted(helperId)

    suspend fun myConfirmed(helperId: Long) = api.myConfirmed(helperId)
}