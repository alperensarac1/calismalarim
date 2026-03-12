package com.example.yardimuygulamakotlin.repo

import com.example.yardimuygulamakotlin.model.ApiOk
import com.example.yardimuygulamakotlin.model.HelpCancelBody
import com.example.yardimuygulamakotlin.model.HelpConfirmBody
import com.example.yardimuygulamakotlin.model.HelpCreateBody
import com.example.yardimuygulamakotlin.model.HelpRequestActive
import com.example.yardimuygulamakotlin.service.ApiClient
import com.example.yardimuygulamakotlin.service.ApiService

// PatientRepo.kt
class PatientRepo(private val api: ApiService = ApiClient.api) {

    suspend fun createHelp(
        patientId: Long,
        servis: String,
        oda: String,
        lat: Double,
        lng: Double,
        hastane: String? = null
    ): ApiOk<Any>? {
        val body = HelpCreateBody(
            patient_id = patientId,
            hastane_adi = hastane,
            servis_adi = servis,
            oda_no = oda,
            lat = lat,
            lng = lng
        )
        val r = api.helpCreate(body)
        return if (r.isSuccessful) r.body() else null
    }
    suspend fun cancel(requestId: Long, patientId: Long): ApiOk<Any>? {
        val r = api.cancel(HelpCancelBody(requestId, patientId))
        return if (r.isSuccessful) r.body() else null
    }
    suspend fun myActive(patientId: Long): ApiOk<HelpRequestActive>? {
        val r = api.myActive(patientId)
        return if (r.isSuccessful) r.body() else null
    }

    suspend fun confirm(requestId: Long, patientId: Long): ApiOk<Any>? {
        val r = api.confirm(HelpConfirmBody(requestId, patientId))
        return if (r.isSuccessful) r.body() else null
    }
}
