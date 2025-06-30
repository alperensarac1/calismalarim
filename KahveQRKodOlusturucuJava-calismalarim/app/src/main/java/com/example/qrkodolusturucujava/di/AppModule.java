package com.example.qrkodolusturucujava.di;

import android.content.Context;

import com.example.qrkodolusturucujava.data.numara_kayit.NumaraKayitSp;
import com.example.qrkodolusturucujava.services.Services;
import com.example.qrkodolusturucujava.services.ServicesImpl;
import com.example.qrkodolusturucujava.usecases.DogrulamaKoduOlustur;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public Services provideKahveServisDao() {
        return new ServicesImpl().getInstance();
    }

    @Provides
    @Singleton
    public NumaraKayitSp provideNumaraKayitSP(@ApplicationContext Context context) {
        return new NumaraKayitSp(context);
    }

    @Provides
    @Singleton
    public DogrulamaKoduOlustur provideDogrulamaKoduOlustur() {
        return new DogrulamaKoduOlustur();
    }
}

