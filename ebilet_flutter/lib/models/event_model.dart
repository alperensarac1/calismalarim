import 'city_model.dart';
import 'district_model.dart';
import 'venue_model.dart';

/// Etkinlik modelidir.
///
/// Bazı endpointlerde tüm alanlar gelmeyebilir.
/// Bu yüzden birçok alan nullable tutuldu.
class EventModel {
  final int id;

  final int? cityId;
  final int? districtId;
  final int? venueId;

  final String title;
  final String? description;
  final String? posterUrl;
  final String? eventDate;

  final double? basePrice;
  final int? totalQuota;
  final int? soldCount;
  final int? remainingQuota;

  final String? cityName;
  final String? districtName;

  final VenueModel? venue;
  final CityModel? city;
  final DistrictModel? district;

  final String? createdAt;

  EventModel({
    required this.id,
    this.cityId,
    this.districtId,
    this.venueId,
    required this.title,
    this.description,
    this.posterUrl,
    this.eventDate,
    this.basePrice,
    this.totalQuota,
    this.soldCount,
    this.remainingQuota,
    this.cityName,
    this.districtName,
    this.venue,
    this.city,
    this.district,
    this.createdAt,
  });

  factory EventModel.fromJson(Map<String, dynamic> json) {
    return EventModel(
      id: int.tryParse(json['id'].toString()) ?? 0,
      cityId: json['city_id'] == null
          ? null
          : int.tryParse(json['city_id'].toString()),
      districtId: json['district_id'] == null
          ? null
          : int.tryParse(json['district_id'].toString()),
      venueId: json['venue_id'] == null
          ? null
          : int.tryParse(json['venue_id'].toString()),
      title: json['title']?.toString() ?? '',
      description: json['description']?.toString(),
      posterUrl: json['poster_url']?.toString(),
      eventDate: json['event_date']?.toString(),
      basePrice: json['base_price'] == null
          ? null
          : double.tryParse(json['base_price'].toString()),
      totalQuota: json['total_quota'] == null
          ? null
          : int.tryParse(json['total_quota'].toString()),
      soldCount: json['sold_count'] == null
          ? null
          : int.tryParse(json['sold_count'].toString()),
      remainingQuota: json['remaining_quota'] == null
          ? null
          : int.tryParse(json['remaining_quota'].toString()),
      cityName: json['city_name']?.toString(),
      districtName: json['district_name']?.toString(),
      venue: json['venue'] is Map<String, dynamic>
          ? VenueModel.fromJson(json['venue'])
          : null,
      city: json['city'] is Map<String, dynamic>
          ? CityModel.fromJson(json['city'])
          : null,
      district: json['district'] is Map<String, dynamic>
          ? DistrictModel.fromJson(json['district'])
          : null,
      createdAt: json['created_at']?.toString(),
    );
  }
}