package com.alperensarac.projectmanagementkotlin.di

import com.alperensarac.projectmanagementkotlin.core.datastore.token.TokenStorage
import com.alperensarac.projectmanagementkotlin.core.datastore.token.TokenStorageImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Uygulamanın veri saklama bağımlılıklarını Hilt dependency graph'a bağlar.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {

    /**
     * TokenStorage istenen yerlere TokenStorageImpl verilmesini sağlar.
     */
    @Binds
    @Singleton
    abstract fun bindTokenStorage(
        implementation: TokenStorageImpl
    ): TokenStorage
}