package com.alperensarac.projectmanagementkotlin.di

import com.alperensarac.projectmanagementkotlin.data.remote.api.CommentsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

/**
 * CommentsApi provider.
 *
 * Yorum endpointleri [Authorize] kullandığı için authenticated Retrofit
 * üzerinden oluşturulur.
 */
@Module
@InstallIn(SingletonComponent::class)
object CommentsNetworkModule {

    @Provides
    @Singleton
    fun provideCommentsApi(
        retrofit: Retrofit
    ): CommentsApi {

        return retrofit.create(
            CommentsApi::class.java
        )
    }
}