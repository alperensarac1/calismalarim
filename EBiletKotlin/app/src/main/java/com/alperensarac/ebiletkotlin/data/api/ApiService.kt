package com.alperensarac.ebiletkotlin.data.api


import com.alperensarac.ebiletkotlin.data.model.ApiResponse
import com.alperensarac.ebiletkotlin.data.model.City
import com.alperensarac.ebiletkotlin.data.model.District
import com.alperensarac.ebiletkotlin.data.model.Event
import com.alperensarac.ebiletkotlin.data.model.Ticket
import com.alperensarac.ebiletkotlin.data.model.User
import com.alperensarac.ebiletkotlin.data.model.Venue
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/*
    ApiService

    Retrofit burada hangi PHP dosyasına hangi parametrelerle
    istek atacağını öğrenir.

    Örnek:

    @POST("auth/login.php")
    fun login(...)

    Bu şu URL'ye gider:
    BASE_URL + "auth/login.php"

    Yani:
    http://10.0.2.2/event_ticket_api/auth/login.php
*/
interface ApiService {

    /*
        Kullanıcı kayıt API'si.

        PHP:
        auth/register.php

        Beklenen POST:
        - full_name
        - email
        - phone
        - password
    */
    @FormUrlEncoded
    @POST("auth/register.php")
    fun register(
        @Field("full_name") fullName: String,
        @Field("email") email: String,
        @Field("phone") phone: String,
        @Field("password") password: String
    ): Call<ApiResponse<User>>

    /*
        Kullanıcı giriş API'si.

        PHP:
        auth/login.php

        Beklenen POST:
        - email
        - password
    */
    @FormUrlEncoded
    @POST("auth/login.php")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<ApiResponse<User>>

    /*
        Profil bilgisi.

        PHP:
        auth/profile.php

        Beklenen POST:
        - api_token
    */
    @FormUrlEncoded
    @POST("auth/profile.php")
    fun profile(
        @Field("api_token") apiToken: String
    ): Call<ApiResponse<User>>

    /*
        Şehirleri listeleme.

        PHP:
        locations/cities_list.php
    */
    @FormUrlEncoded
    @POST("locations/cities_list.php")
    fun getCities(
        @Field("api_token") apiToken: String
    ): Call<ApiResponse<List<City>>>

    /*
        Şehre göre ilçeleri listeleme.

        PHP:
        locations/districts_by_city.php
    */
    @FormUrlEncoded
    @POST("locations/districts_by_city.php")
    fun getDistrictsByCity(
        @Field("api_token") apiToken: String,
        @Field("city_id") cityId: Int
    ): Call<ApiResponse<List<District>>>

    /*
        İlçeye göre mekanları listeleme.

        PHP:
        locations/venues_by_district.php
    */
    @FormUrlEncoded
    @POST("locations/venues_by_district.php")
    fun getVenuesByDistrict(
        @Field("api_token") apiToken: String,
        @Field("city_id") cityId: Int,
        @Field("district_id") districtId: Int
    ): Call<ApiResponse<List<Venue>>>

    /*
        Şehir + ilçe seçimine göre etkinlikleri listeleme.

        PHP:
        events/events_by_location.php

        venue_id opsiyonel demiştik ama Retrofit'te sade tutmak için
        şimdilik göndermiyoruz.
    */
    @FormUrlEncoded
    @POST("events/events_by_location.php")
    fun getEventsByLocation(
        @Field("api_token") apiToken: String,
        @Field("city_id") cityId: Int,
        @Field("district_id") districtId: Int
    ): Call<ApiResponse<List<Event>>>

    /*
        Etkinlik detayı.

        PHP:
        events/event_detail.php
    */
    @FormUrlEncoded
    @POST("events/event_detail.php")
    fun getEventDetail(
        @Field("api_token") apiToken: String,
        @Field("event_id") eventId: Int
    ): Call<ApiResponse<Event>>

    /*
        Bilet satın alma.

        PHP:
        tickets/ticket_buy.php
    */
    @FormUrlEncoded
    @POST("tickets/ticket_buy.php")
    fun buyTicket(
        @Field("api_token") apiToken: String,
        @Field("event_id") eventId: Int
    ): Call<ApiResponse<Ticket>>

    /*
        Biletlerim.

        PHP:
        tickets/my_tickets.php
    */
    @FormUrlEncoded
    @POST("tickets/my_tickets.php")
    fun getMyTickets(
        @Field("api_token") apiToken: String
    ): Call<ApiResponse<List<Ticket>>>

    /*
        Bilet detay.

        PHP:
        tickets/ticket_detail.php
    */
    @FormUrlEncoded
    @POST("tickets/ticket_detail.php")
    fun getTicketDetail(
        @Field("api_token") apiToken: String,
        @Field("ticket_id") ticketId: Int
    ): Call<ApiResponse<Ticket>>

    /*
        QR bilet kontrol.

        PHP:
        check/ticket_check.php

        Bu API sadece staff veya admin tokenı ile çalışır.
    */
    @FormUrlEncoded
    @POST("check/ticket_check.php")
    fun checkTicket(
        @Field("api_token") apiToken: String,
        @Field("ticket_code") ticketCode: String
    ): Call<ApiResponse<Ticket>>
}