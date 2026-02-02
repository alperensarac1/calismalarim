package com.example.kargopaylasimkotlin.di



import android.content.Context
import com.example.kargopaylasimkotlin.repo.CargoRepository
import com.example.kargopaylasimkotlin.service.TokenStore


class AppContainer(context: Context) {
    val tokenStore = TokenStore(context)
    val api = ApiClient.createApi(context)
    val repo = CargoRepository(api, tokenStore)
}
