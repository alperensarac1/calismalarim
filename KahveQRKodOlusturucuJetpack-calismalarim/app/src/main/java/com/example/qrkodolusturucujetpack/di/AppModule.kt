package com.example.qrkodolusturucujetpack.di

import android.content.Context
import com.example.qrkodolusturucu.services.Services
import com.example.qrkodolusturucu.services.ServicesImpl
import com.example.qrkodolusturucujetpack.data.numara_kayit.NumaraKayitSp
import com.example.qrkodolusturucujetpack.usecases.DogrulamaKoduOlustur

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun provideKahveServisDao():Services = ServicesImpl().getInstance()

    @Provides
    @Singleton
    fun provideNumaraKayitSP(@ApplicationContext context:Context): NumaraKayitSp
    = NumaraKayitSp(context)

    @Provides
    @Singleton
    fun provideDogrulamaKoduOlustur(): DogrulamaKoduOlustur = DogrulamaKoduOlustur()
}