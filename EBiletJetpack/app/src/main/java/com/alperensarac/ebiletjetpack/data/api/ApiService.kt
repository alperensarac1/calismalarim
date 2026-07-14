package com.alperensarac.ebiletjetpack.data.api


import com.alperensarac.ebiletjetpack.data.model.ApiResponse
import com.alperensarac.ebiletjetpack.data.model.City
import com.alperensarac.ebiletjetpack.data.model.District
import com.alperensarac.ebiletjetpack.data.model.Event
import com.alperensarac.ebiletjetpack.data.model.Ticket
import com.alperensarac.ebiletjetpack.data.model.User
import com.alperensarac.ebiletjetpack.data.model.Venue
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/*
    ApiService

    Retrofit'e hangi PHP dosyasına hangi POST alanlarıyla
    istek atacağını anlatır.

    Örnek:
    @POST("auth/login.php")

    Tam URL:
    BASE_URL + "auth/login.php"
*/
interface ApiService {

    @FormUrlEncoded
    @POST("auth/register.php")
    fun register(
        @Field("full_name") fullName: String,
        @Field("email") email: String,
        @Field("phone") phone: String,
        @Field("password") password: String
    ): Call<ApiResponse<User>>

    @FormUrlEncoded
    @POST("auth/login.php")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<ApiResponse<User>>

    @FormUrlEncoded
    @POST("auth/profile.php")
    fun profile(
        @Field("api_token") apiToken: String
    ): Call<ApiResponse<User>>

    @FormUrlEncoded
    @POST("locations/cities_list.php")
    fun getCities(
        @Field("api_token") apiToken: String
    ): Call<ApiResponse<List<City>>>

    @FormUrlEncoded
    @POST("locations/districts_by_city.php")
    fun getDistrictsByCity(
        @Field("api_token") apiToken: String,
        @Field("city_id") cityId: Int
    ): Call<ApiResponse<List<District>>>

    @FormUrlEncoded
    @POST("locations/venues_by_district.php")
    fun getVenuesByDistrict(
        @Field("api_token") apiToken: String,
        @Field("city_id") cityId: Int,
        @Field("district_id") districtId: Int
    ): Call<ApiResponse<List<Venue>>>

    @FormUrlEncoded
    @POST("events/events_by_location.php")
    fun getEventsByLocation(
        @Field("api_token") apiToken: String,
        @Field("city_id") cityId: Int,
        @Field("district_id") districtId: Int
    ): Call<ApiResponse<List<Event>>>

    @FormUrlEncoded
    @POST("events/event_detail.php")
    fun getEventDetail(
        @Field("api_token") apiToken: String,
        @Field("event_id") eventId: Int
    ): Call<ApiResponse<Event>>

    @FormUrlEncoded
    @POST("tickets/ticket_buy.php")
    fun buyTicket(
        @Field("api_token") apiToken: String,
        @Field("event_id") eventId: Int
    ): Call<ApiResponse<Ticket>>

    @FormUrlEncoded
    @POST("tickets/my_tickets.php")
    fun getMyTickets(
        @Field("api_token") apiToken: String
    ): Call<ApiResponse<List<Ticket>>>

    @FormUrlEncoded
    @POST("tickets/ticket_detail.php")
    fun getTicketDetail(
        @Field("api_token") apiToken: String,
        @Field("ticket_id") ticketId: Int
    ): Call<ApiResponse<Ticket>>

    @FormUrlEncoded
    @POST("check/ticket_check.php")
    fun checkTicket(
        @Field("api_token") apiToken: String,
        @Field("ticket_code") ticketCode: String
    ): Call<ApiResponse<Ticket>>
}