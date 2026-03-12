package com.example.yardimuygulamakotlin.repo
// HelperRepo.kt
import com.example.yardimuygulamakotlin.model.AcceptedHelpItem
import com.example.yardimuygulamakotlin.model.ApiOk
import com.example.yardimuygulamakotlin.model.ConfirmedHelpItem
import com.example.yardimuygulamakotlin.model.HelpAcceptBody
import com.example.yardimuygulamakotlin.model.OpenHelpItem
import com.example.yardimuygulamakotlin.service.ApiClient
import com.example.yardimuygulamakotlin.service.ApiService
import retrofit2.Response

class HelperRepo(private val api: ApiService = ApiClient.api) {

    suspend fun listOpen(helperId: Long): ApiOk<OpenHelpItem>? {
        val r = api.listOpen(helperId)
        return if (r.isSuccessful) r.body() else null
    }

    suspend fun accept(requestId: Long, helperId: Long): ApiOk<Any>? {
        val r = api.accept(HelpAcceptBody(requestId, helperId))
        return if (r.isSuccessful) r.body() else null
    }

    suspend fun myAccepted(helperId: Long): ApiOk<AcceptedHelpItem>? {
        val r = api.myAccepted(helperId)
        return if (r.isSuccessful) r.body() else null
    }
    suspend fun myConfirmed(helperId: Long): ApiOk<ConfirmedHelpItem>? {
        val r = api.myConfirmed(helperId)
        return if (r.isSuccessful) r.body() else null
    }
}
