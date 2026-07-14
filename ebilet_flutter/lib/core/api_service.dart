import '../models/api_response.dart';
import '../models/city_model.dart';
import '../models/district_model.dart';
import '../models/event_model.dart';
import '../models/ticket_model.dart';
import '../models/user_model.dart';
import '../models/venue_model.dart';
import 'api_client.dart';

/// APIService
///
/// Uygulamadaki tüm endpoint fonksiyonlarını tek yerde toplar.
class ApiService {
  static Future<ApiResponse<UserModel>> register({
    required String fullName,
    required String email,
    required String phone,
    required String password,
  }) async {
    final json = await ApiClient.post(
      'auth/register.php',
      {
        'full_name': fullName,
        'email': email,
        'phone': phone,
        'password': password,
      },
    );

    return ApiResponse<UserModel>.fromJson(
      json,
          (data) => UserModel.fromJson(data),
    );
  }

  static Future<ApiResponse<UserModel>> login({
    required String email,
    required String password,
  }) async {
    final json = await ApiClient.post(
      'auth/login.php',
      {
        'email': email,
        'password': password,
      },
    );

    return ApiResponse<UserModel>.fromJson(
      json,
          (data) => UserModel.fromJson(data),
    );
  }

  static Future<ApiResponse<UserModel>> profile({
    required String apiToken,
  }) async {
    final json = await ApiClient.post(
      'auth/profile.php',
      {
        'api_token': apiToken,
      },
    );

    return ApiResponse<UserModel>.fromJson(
      json,
          (data) => UserModel.fromJson(data),
    );
  }

  static Future<ApiResponse<List<CityModel>>> getCities({
    required String apiToken,
  }) async {
    final json = await ApiClient.post(
      'locations/cities_list.php',
      {
        'api_token': apiToken,
      },
    );

    return ApiResponse<List<CityModel>>.fromJson(
      json,
          (data) {
        final list = data as List;
        return list
            .map((item) => CityModel.fromJson(item as Map<String, dynamic>))
            .toList();
      },
    );
  }

  static Future<ApiResponse<List<DistrictModel>>> getDistrictsByCity({
    required String apiToken,
    required int cityId,
  }) async {
    final json = await ApiClient.post(
      'locations/districts_by_city.php',
      {
        'api_token': apiToken,
        'city_id': cityId.toString(),
      },
    );

    return ApiResponse<List<DistrictModel>>.fromJson(
      json,
          (data) {
        final list = data as List;
        return list
            .map((item) => DistrictModel.fromJson(item as Map<String, dynamic>))
            .toList();
      },
    );
  }

  static Future<ApiResponse<List<VenueModel>>> getVenuesByDistrict({
    required String apiToken,
    required int cityId,
    required int districtId,
  }) async {
    final json = await ApiClient.post(
      'locations/venues_by_district.php',
      {
        'api_token': apiToken,
        'city_id': cityId.toString(),
        'district_id': districtId.toString(),
      },
    );

    return ApiResponse<List<VenueModel>>.fromJson(
      json,
          (data) {
        final list = data as List;
        return list
            .map((item) => VenueModel.fromJson(item as Map<String, dynamic>))
            .toList();
      },
    );
  }

  static Future<ApiResponse<List<EventModel>>> getEventsByLocation({
    required String apiToken,
    required int cityId,
    required int districtId,
  }) async {
    final json = await ApiClient.post(
      'events/events_by_location.php',
      {
        'api_token': apiToken,
        'city_id': cityId.toString(),
        'district_id': districtId.toString(),
      },
    );

    return ApiResponse<List<EventModel>>.fromJson(
      json,
          (data) {
        final list = data as List;
        return list
            .map((item) => EventModel.fromJson(item as Map<String, dynamic>))
            .toList();
      },
    );
  }

  static Future<ApiResponse<EventModel>> getEventDetail({
    required String apiToken,
    required int eventId,
  }) async {
    final json = await ApiClient.post(
      'events/event_detail.php',
      {
        'api_token': apiToken,
        'event_id': eventId.toString(),
      },
    );

    return ApiResponse<EventModel>.fromJson(
      json,
          (data) => EventModel.fromJson(data),
    );
  }

  static Future<ApiResponse<TicketModel>> buyTicket({
    required String apiToken,
    required int eventId,
  }) async {
    final json = await ApiClient.post(
      'tickets/ticket_buy.php',
      {
        'api_token': apiToken,
        'event_id': eventId.toString(),
      },
    );

    return ApiResponse<TicketModel>.fromJson(
      json,
          (data) => TicketModel.fromJson(data),
    );
  }

  static Future<ApiResponse<List<TicketModel>>> getMyTickets({
    required String apiToken,
  }) async {
    final json = await ApiClient.post(
      'tickets/my_tickets.php',
      {
        'api_token': apiToken,
      },
    );

    return ApiResponse<List<TicketModel>>.fromJson(
      json,
          (data) {
        final list = data as List;
        return list
            .map((item) => TicketModel.fromJson(item as Map<String, dynamic>))
            .toList();
      },
    );
  }

  static Future<ApiResponse<TicketModel>> getTicketDetail({
    required String apiToken,
    required int ticketId,
  }) async {
    final json = await ApiClient.post(
      'tickets/ticket_detail.php',
      {
        'api_token': apiToken,
        'ticket_id': ticketId.toString(),
      },
    );

    return ApiResponse<TicketModel>.fromJson(
      json,
          (data) => TicketModel.fromJson(data),
    );
  }

  static Future<ApiResponse<TicketModel>> checkTicket({
    required String apiToken,
    required String ticketCode,
  }) async {
    final json = await ApiClient.post(
      'check/ticket_check.php',
      {
        'api_token': apiToken,
        'ticket_code': ticketCode,
      },
    );

    return ApiResponse<TicketModel>.fromJson(
      json,
          (data) => TicketModel.fromJson(data),
    );
  }
}