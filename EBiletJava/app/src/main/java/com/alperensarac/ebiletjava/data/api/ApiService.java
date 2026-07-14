package com.alperensarac.ebiletjava.data.api;


import com.alperensarac.ebiletjava.data.model.ApiResponse;
import com.alperensarac.ebiletjava.data.model.City;
import com.alperensarac.ebiletjava.data.model.District;
import com.alperensarac.ebiletjava.data.model.Event;
import com.alperensarac.ebiletjava.data.model.Ticket;
import com.alperensarac.ebiletjava.data.model.User;
import com.alperensarac.ebiletjava.data.model.Venue;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/*
    ApiService.java

    Bu interface Retrofit'e hangi PHP dosyasına
    hangi POST alanlarıyla istek atacağını söyler.

    Örnek:

    @POST("auth/login.php")

    BASE_URL ile birleşince:
    http://10.0.2.2/event_ticket_api/auth/login.php
*/
public interface ApiService {

    /*
        Kullanıcı kayıt API'si.

        PHP:
        auth/register.php

        Beklenen POST:
        full_name
        email
        phone
        password
    */
    @FormUrlEncoded
    @POST("auth/register.php")
    Call<ApiResponse<User>> register(
            @Field("full_name") String fullName,
            @Field("email") String email,
            @Field("phone") String phone,
            @Field("password") String password
    );

    /*
        Kullanıcı giriş API'si.

        PHP:
        auth/login.php
    */
    @FormUrlEncoded
    @POST("auth/login.php")
    Call<ApiResponse<User>> login(
            @Field("email") String email,
            @Field("password") String password
    );

    /*
        Profil API'si.

        PHP:
        auth/profile.php
    */
    @FormUrlEncoded
    @POST("auth/profile.php")
    Call<ApiResponse<User>> profile(
            @Field("api_token") String apiToken
    );

    /*
        Şehir listeleme.

        PHP:
        locations/cities_list.php
    */
    @FormUrlEncoded
    @POST("locations/cities_list.php")
    Call<ApiResponse<List<City>>> getCities(
            @Field("api_token") String apiToken
    );

    /*
        Şehre göre ilçe listeleme.

        PHP:
        locations/districts_by_city.php
    */
    @FormUrlEncoded
    @POST("locations/districts_by_city.php")
    Call<ApiResponse<List<District>>> getDistrictsByCity(
            @Field("api_token") String apiToken,
            @Field("city_id") int cityId
    );

    /*
        İlçeye göre sahne/mekan listeleme.

        PHP:
        locations/venues_by_district.php
    */
    @FormUrlEncoded
    @POST("locations/venues_by_district.php")
    Call<ApiResponse<List<Venue>>> getVenuesByDistrict(
            @Field("api_token") String apiToken,
            @Field("city_id") int cityId,
            @Field("district_id") int districtId
    );

    /*
        Şehir + ilçe seçimine göre etkinlikleri listeleme.

        PHP:
        events/events_by_location.php
    */
    @FormUrlEncoded
    @POST("events/events_by_location.php")
    Call<ApiResponse<List<Event>>> getEventsByLocation(
            @Field("api_token") String apiToken,
            @Field("city_id") int cityId,
            @Field("district_id") int districtId
    );

    /*
        Etkinlik detayı.

        PHP:
        events/event_detail.php
    */
    @FormUrlEncoded
    @POST("events/event_detail.php")
    Call<ApiResponse<Event>> getEventDetail(
            @Field("api_token") String apiToken,
            @Field("event_id") int eventId
    );

    /*
        Bilet satın alma.

        PHP:
        tickets/ticket_buy.php
    */
    @FormUrlEncoded
    @POST("tickets/ticket_buy.php")
    Call<ApiResponse<Ticket>> buyTicket(
            @Field("api_token") String apiToken,
            @Field("event_id") int eventId
    );

    /*
        Kullanıcının biletleri.

        PHP:
        tickets/my_tickets.php
    */
    @FormUrlEncoded
    @POST("tickets/my_tickets.php")
    Call<ApiResponse<List<Ticket>>> getMyTickets(
            @Field("api_token") String apiToken
    );

    /*
        Tek bilet detayı.

        PHP:
        tickets/ticket_detail.php
    */
    @FormUrlEncoded
    @POST("tickets/ticket_detail.php")
    Call<ApiResponse<Ticket>> getTicketDetail(
            @Field("api_token") String apiToken,
            @Field("ticket_id") int ticketId
    );

    /*
        QR bilet kontrol.

        PHP:
        check/ticket_check.php

        Bu API sadece staff/admin tokenı ile çalışır.
    */
    @FormUrlEncoded
    @POST("check/ticket_check.php")
    Call<ApiResponse<Ticket>> checkTicket(
            @Field("api_token") String apiToken,
            @Field("ticket_code") String ticketCode
    );
}
