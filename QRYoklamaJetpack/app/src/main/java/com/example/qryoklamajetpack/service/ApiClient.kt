package com.example.qryoklamajetpack.service

import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody


object ApiClient {

    private val JSON: MediaType = "application/json; charset=utf-8".toMediaType()

    private val client: OkHttpClient = OkHttpClient()

    fun postJson(url: String, jsonBody: String, cb: Callback) {
        val body = jsonBody.toRequestBody(JSON)

        val req = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Platform", "android")
            .build()

        client.newCall(req).enqueue(cb)
    }
}
