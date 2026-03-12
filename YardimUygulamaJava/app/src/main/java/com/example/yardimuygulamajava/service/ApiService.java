package com.example.yardimuygulamajava.service;

import com.example.yardimuygulamajava.model.AcceptedHelpItem;
import com.example.yardimuygulamajava.model.ConfirmedHelpItem;
import com.example.yardimuygulamajava.model.HelpAcceptBody;
import com.example.yardimuygulamajava.model.HelpCancelBody;
import com.example.yardimuygulamajava.model.HelpConfirmBody;
import com.example.yardimuygulamajava.model.HelpCreateBody;
import com.example.yardimuygulamajava.model.HelpRequestActive;
import com.example.yardimuygulamajava.model.OpenHelpItem;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // AUTH
    @POST("auth_register.php")
    Call<ApiOk<Object>> register(@Body RegisterBody body);

    @POST("auth_login.php")
    Call<ApiOk<Object>> login(@Body LoginBody body);

    // PATIENT
    @POST("help_create.php")
    Call<ApiOk<Object>> createHelp(@Body HelpCreateBody body);

    @GET("help_my_active.php")
    Call<ApiOk<HelpRequestActive>> myActive(@Query("patient_id") long patientId);

    @POST("help_confirm.php")
    Call<ApiOk<Object>> confirm(@Body HelpConfirmBody body);

    @POST("help_cancel.php")
    Call<ApiOk<Object>> cancel(@Body HelpCancelBody body);

    // HELPER
    @GET("help_list_open.php")
    Call<ApiOk<OpenHelpItem>> listOpen(@Query("helper_id") long helperId);

    @POST("help_accept.php")
    Call<ApiOk<Object>> accept(@Body HelpAcceptBody body);

    @GET("help_my_accepted.php")
    Call<ApiOk<AcceptedHelpItem>> myAccepted(@Query("helper_id") long helperId);

    @GET("help_my_confirmed.php")
    Call<ApiOk<ConfirmedHelpItem>> myConfirmed(@Query("helper_id") long helperId);
}