package com.example.haberuygulama.deo

import com.example.haberuygulama.model.HaberModel
import com.example.haberuygulama.model.HaberTuruModel
import com.example.haberuygulama.model.YorumInsertRequest
import com.example.haberuygulama.model.YorumModel
import com.example.haberuygulama.servis.ApiClient
import com.example.haberuygulama.servis.ApiResponse
import com.example.haberuygulama.servis.HaberService
import retrofit2.Retrofit
import retrofit2.awaitResponse


class HaberDao(var retrofit: Retrofit) {

    private val service = retrofit.create(HaberService::class.java)

    suspend fun getHaberler(): List<HaberModel>? {
        return try {
            val response = service.getHaberler().awaitResponse()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }


    suspend fun getYorumlar(haberId: Int): List<YorumModel>? {
        return try {
            val response = service.getYorumlar(haberId).awaitResponse()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun insertYorum(yorum: YorumInsertRequest): ApiResponse? {
        return try {
            val response = service.insertYorum(yorum).awaitResponse()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getKategoriler(): List<HaberTuruModel>? {
        return try {
            val response = service.getKategoriler().awaitResponse()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }
    suspend fun getSon3Haber(): List<HaberModel>? {
        return try {
            val response = service.getSon3Haber().awaitResponse()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }
    suspend fun getSonDakikaHaberler(): List<HaberModel>? {
        return try {
            val response = service.getSonDakikaHaberler().awaitResponse()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }
    suspend fun getGundemHaberler(): List<HaberModel>? {
        return try {
            val response = service.getGundemHaberler().awaitResponse()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }


}

