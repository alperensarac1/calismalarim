package com.example.yardimuygulamajetpack.service


import retrofit2.http.*

interface ApiService {
    @POST("auth_register.php") suspend fun register(@Body body: RegisterBody): ApiOk<Any>
    @POST("auth_login.php") suspend fun login(@Body body: LoginBody): ApiOk<Any>

    @POST("help_create.php") suspend fun createHelp(@Body body: HelpCreateBody): ApiOk<Any>
    @GET("help_my_active.php") suspend fun myActive(@Query("patient_id") patientId: Long): ApiOk<HelpActive>
    @POST("help_confirm.php") suspend fun confirm(@Body body: HelpConfirmBody): ApiOk<Any>
    @POST("help_cancel.php") suspend fun cancel(@Body body: HelpCancelBody): ApiOk<Any>

    @GET("help_list_open.php") suspend fun listOpen(@Query("helper_id") helperId: Long): ApiOk<OpenHelpItem>
    @POST("help_accept.php") suspend fun accept(@Body body: HelpAcceptBody): ApiOk<Any>
    @GET("help_my_accepted.php") suspend fun myAccepted(@Query("helper_id") helperId: Long): ApiOk<AcceptedHelpItem>
    @GET("help_my_confirmed.php") suspend fun myConfirmed(@Query("helper_id") helperId: Long): ApiOk<ConfirmedHelpItem>
}