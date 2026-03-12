package com.example.yardimuygulamakotlin.service

// ApiService.kt
import com.example.yardimuygulamakotlin.model.AcceptedHelpItem
import com.example.yardimuygulamakotlin.model.ApiOk
import com.example.yardimuygulamakotlin.model.ConfirmedHelpItem
import com.example.yardimuygulamakotlin.model.HelpAcceptBody
import com.example.yardimuygulamakotlin.model.HelpCancelBody
import com.example.yardimuygulamakotlin.model.HelpConfirmBody
import com.example.yardimuygulamakotlin.model.HelpCreateBody
import com.example.yardimuygulamakotlin.model.HelpRequestActive
import com.example.yardimuygulamakotlin.model.LoginBody
import com.example.yardimuygulamakotlin.model.OpenHelpItem
import com.example.yardimuygulamakotlin.model.RegisterBody
import retrofit2.http.*
import retrofit2.Response

interface ApiService {

    @POST("auth_register.php")
    suspend fun register(@Body body: RegisterBody): retrofit2.Response<ApiOk<Any>>

    @POST("auth_login.php")
    suspend fun login(@Body body: LoginBody): retrofit2.Response<ApiOk<Any>>

    @POST("help_create.php")
    suspend fun helpCreate(@Body body: HelpCreateBody): Response<ApiOk<Any>>

    @GET("help_list_open.php")
    suspend fun listOpen(@Query("helper_id") helperId: Long): Response<ApiOk<OpenHelpItem>>

    @POST("help_accept.php")
    suspend fun accept(@Body body: HelpAcceptBody): Response<ApiOk<Any>>

    @GET("help_my_accepted.php")
    suspend fun myAccepted(@Query("helper_id") helperId: Long): Response<ApiOk<AcceptedHelpItem>>

    @POST("help_confirm.php")
    suspend fun confirm(@Body body: HelpConfirmBody): Response<ApiOk<Any>>

    @GET("help_my_active.php")
    suspend fun myActive(@Query("patient_id") patientId: Long):Response<ApiOk<HelpRequestActive>>

    @POST("help_cancel.php")
    suspend fun cancel(@Body body: HelpCancelBody): Response<ApiOk<Any>>
    @GET("help_my_confirmed.php")
    suspend fun myConfirmed(@Query("helper_id") helperId: Long): retrofit2.Response<ApiOk<ConfirmedHelpItem>>
}
