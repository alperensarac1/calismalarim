package com.example.yardimuygulamakotlin.repo

import com.example.yardimuygulamakotlin.model.ApiOk
import com.example.yardimuygulamakotlin.model.LoginBody
import com.example.yardimuygulamakotlin.model.RegisterBody
import com.example.yardimuygulamakotlin.service.ApiClient
import com.example.yardimuygulamakotlin.service.ApiService

class AuthRepo(private val api: ApiService = ApiClient.api) {

    suspend fun login(phone: String, pass: String): ApiOk<Any>? {
        val r = api.login(LoginBody(phone, pass))
        return if (r.isSuccessful) r.body() else null
    }

    suspend fun register(body: RegisterBody): ApiOk<Any>? {
        val r = api.register(body)
        return if (r.isSuccessful) r.body() else null
    }
}