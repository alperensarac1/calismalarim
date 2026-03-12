package com.example.yardimuygulamajetpack.repo

import com.example.yardimuygulamajetpack.model.HelpCancelBody
import com.example.yardimuygulamajetpack.model.HelpConfirmBody
import com.example.yardimuygulamajetpack.model.HelpCreateBody
import com.example.yardimuygulamajetpack.service.ApiClient

class PatientRepo {
    private val api = ApiClient.api

    suspend fun createHelp(patientId: Long, servis: String, oda: String, lat: Double, lng: Double) =
        api.createHelp(HelpCreateBody(patient_id = patientId, servis_adi = servis, oda_no = oda, lat = lat, lng = lng))

    suspend fun myActive(patientId: Long) = api.myActive(patientId)

    suspend fun confirm(reqId: Long, patientId: Long) =
        api.confirm(HelpConfirmBody(request_id = reqId, patient_id = patientId))

    suspend fun cancel(reqId: Long, patientId: Long) =
        api.cancel(HelpCancelBody(request_id = reqId, patient_id = patientId))
}