package com.example.haberuygulamajetpack.deo

import com.example.haberuygulamajetpack.model.HaberModel
import com.example.haberuygulamajetpack.model.HaberTuruModel
import com.example.haberuygulamajetpack.model.YorumInsertRequest
import com.example.haberuygulamajetpack.model.YorumModel
import com.example.haberuygulamajetpack.servis.ApiClient
import com.example.haberuygulamajetpack.servis.ApiResponse
import com.example.haberuygulamajetpack.servis.HaberService
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
    suspend fun getHaberById(haberId: Int): HaberModel? {
        return try {
            val response = service.getHaberById(haberId).awaitResponse()
            if (response.isSuccessful) response.body()!!.data else null
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

