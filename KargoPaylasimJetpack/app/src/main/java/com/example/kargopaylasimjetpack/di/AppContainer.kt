package com.example.kargopaylasimjetpack.di

import android.content.Context
import com.example.kargopaylasimjetpack.repository.Repo
import com.example.kargopaylasimjetpack.service.ApiClientFactory
import com.example.kargopaylasimjetpack.storage.TokenStore


class AppContainer(ctx: Context) {
    val tokenStore = TokenStore(ctx)
    val api = ApiClientFactory.create(tokenStore)
    val repo = Repo(api)
}
