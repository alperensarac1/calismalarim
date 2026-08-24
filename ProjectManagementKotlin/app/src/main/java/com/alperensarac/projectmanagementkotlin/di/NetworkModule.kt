package com.alperensarac.projectmanagementkotlin.di

import android.content.Context
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.core.network.authenticator.TokenAuthenticator
import com.alperensarac.projectmanagementkotlin.core.network.interceptor.AuthInterceptor
import com.alperensarac.projectmanagementkotlin.core.network.qualifier.ApiBaseUrl
import com.alperensarac.projectmanagementkotlin.core.network.qualifier.AuthenticatedClient
import com.alperensarac.projectmanagementkotlin.core.network.qualifier.PublicClient
import com.alperensarac.projectmanagementkotlin.core.network.qualifier.PublicRetrofit
import com.alperensarac.projectmanagementkotlin.data.remote.api.AuthApi
import com.alperensarac.projectmanagementkotlin.data.remote.api.DashboardApi
import com.alperensarac.projectmanagementkotlin.data.remote.api.ProjectsApi
import com.alperensarac.projectmanagementkotlin.data.remote.api.RefreshApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Uygulamanın public ve authenticated network dependency'lerini oluşturur.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CONNECT_TIMEOUT_SECONDS = 30L
    private const val READ_TIMEOUT_SECONDS = 60L
    private const val WRITE_TIMEOUT_SECONDS = 60L

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
            isLenient = false
            encodeDefaults = true
        }
    }

    @ApiBaseUrl
    @Provides
    @Singleton
    fun provideApiBaseUrl(
        @ApplicationContext context: Context
    ): String {
        val baseUrl = context
            .getString(R.string.api_base_url)
            .trim()

        require(baseUrl.isNotBlank()) {
            "API base URL boş olamaz."
        }

        require(
            baseUrl.startsWith("http://") ||
                    baseUrl.startsWith("https://")
        ) {
            "API base URL http:// veya https:// ile başlamalıdır."
        }

        require(baseUrl.endsWith("/")) {
            "API base URL '/' karakteriyle bitmelidir."
        }

        return baseUrl
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
            redactHeader("Authorization")
        }
    }

    @PublicClient
    @Provides
    @Singleton
    fun providePublicOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(
                CONNECT_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
            .readTimeout(
                READ_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
            .writeTimeout(
                WRITE_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
            .retryOnConnectionFailure(true)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @AuthenticatedClient
    @Provides
    @Singleton
    fun provideAuthenticatedOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(
                CONNECT_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
            .readTimeout(
                READ_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
            .writeTimeout(
                WRITE_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
            .retryOnConnectionFailure(true)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    @PublicRetrofit
    @Provides
    @Singleton
    fun providePublicRetrofit(
        @ApiBaseUrl apiBaseUrl: String,
        json: Json,
        @PublicClient okHttpClient: OkHttpClient
    ): Retrofit {
        return createRetrofit(
            apiBaseUrl = apiBaseUrl,
            json = json,
            okHttpClient = okHttpClient
        )
    }


    @Provides
    @Singleton
    fun provideAuthenticatedRetrofit(
        @ApiBaseUrl apiBaseUrl: String,
        json: Json,
        @AuthenticatedClient okHttpClient: OkHttpClient
    ): Retrofit {
        return createRetrofit(
            apiBaseUrl = apiBaseUrl,
            json = json,
            okHttpClient = okHttpClient
        )
    }

    @Provides
    @Singleton
    fun provideAuthApi(
        retrofit: Retrofit
    ): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRefreshApi(
        @PublicRetrofit retrofit: Retrofit
    ): RefreshApi {
        return retrofit.create(RefreshApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDashboardApi(
        retrofit: Retrofit
    ): DashboardApi {
        return retrofit.create(DashboardApi::class.java)
    }

    /**
     * Projects endpointleri korumalı olduğu için authenticated Retrofit
     * kullanılır.
     */
    @Provides
    @Singleton
    fun provideProjectsApi(
        retrofit: Retrofit
    ): ProjectsApi {
        return retrofit.create(ProjectsApi::class.java)
    }

    private fun createRetrofit(
        apiBaseUrl: String,
        json: Json,
        okHttpClient: OkHttpClient
    ): Retrofit {
        val jsonMediaType =
            "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(apiBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory(jsonMediaType)
            )
            .build()
    }
}