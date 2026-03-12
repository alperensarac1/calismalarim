package com.example.yardimuygulamajetpack.repo

import com.example.yardimuygulamajetpack.model.LoginBody
import com.example.yardimuygulamajetpack.model.RegisterBody
import com.example.yardimuygulamajetpack.service.ApiClient

class AuthRepo {
    private val api = ApiClient.api

    suspend fun login(phone: String, pass: String) =
        api.login(LoginBody(phone, pass))

    suspend fun register(body: RegisterBody) =
        api.register(body)
}