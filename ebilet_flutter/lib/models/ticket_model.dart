import 'city_model.dart';
import 'district_model.dart';
import 'event_model.dart';
import 'user_model.dart';
import 'venue_model.dart';

/// Bilet modelidir.
///
/// Kullanıldığı endpointler:
/// - ticket_buy.php
/// - my_tickets.php
/// - ticket_detail.php
/// - ticket_check.php
class TicketModel {
  final int? id;
  final int? ticketId;

  final int? eventId;
  final String? eventTitle;

  final String? ticketCode;
  final String? qrCodeText;

  final double? price;

  final String? status;
  final String? ticketStatus;

  final String? purchasedAt;
  final String? usedAt;
  final String? transactionId;

  final EventModel? event;
  final CityModel? city;
  final DistrictModel? district;
  final VenueModel? venue;
  final TicketLocationModel? location;
  final UserModel? user;

  final CheckedByModel? checkedBy;

  /// ticket_check.php sonucu:
  /// approved
  /// already_used
  /// invalid
  /// cancelled
  /// passive_event
  final String? result;

  TicketModel({
    this.id,
    this.ticketId,
    this.eventId,
    this.eventTitle,
    this.ticketCode,
    this.qrCodeText,
    this.price,
    this.status,
    this.ticketStatus,
    this.purchasedAt,
    this.usedAt,
    this.transactionId,
    this.event,
    this.city,
    this.district,
    this.venue,
    this.location,
    this.user,
    this.checkedBy,
    this.result,
  });

  factory TicketModel.fromJson(Map<String, dynamic> json) {
    return TicketModel(
      id: json['id'] == null ? null : int.tryParse(json['id'].toString()),
      ticketId: json['ticket_id'] == null
          ? null
          : int.tryParse(json['ticket_id'].toString()),
      eventId: json['event_id'] == null
          ? null
          : int.tryParse(json['event_id'].toString()),
      eventTitle: json['event_title']?.toString(),
      ticketCode: json['ticket_code']?.toString(),
      qrCodeText: json['qr_code_text']?.toString(),
      price: json['price'] == null
          ? null
          : double.tryParse(json['price'].toString()),
      status: json['status']?.toString(),
      ticketStatus: json['ticket_status']?.toString(),
      purchasedAt: json['purchased_at']?.toString(),
      usedAt: json['used_at']?.toString(),
      transactionId: json['transaction_id']?.toString(),
      event: json['event'] is Map<String, dynamic>
          ? EventModel.fromJson(json['event'])
          : null,
      city: json['city'] is Map<String, dynamic>
          ? CityModel.fromJson(json['city'])
          : null,
      district: json['district'] is Map<String, dynamic>
          ? DistrictModel.fromJson(json['district'])
          : null,
      venue: json['venue'] is Map<String, dynamic>
          ? VenueModel.fromJson(json['venue'])
          : null,
      location: json['location'] is Map<String, dynamic>
          ? TicketLocationModel.fromJson(json['location'])
          : null,
      user: json['user'] is Map<String, dynamic>
          ? UserModel.fromJson(json['user'])
          : null,
      checkedBy: json['checked_by'] is Map<String, dynamic>
          ? CheckedByModel.fromJson(json['checked_by'])
          : null,
      result: json['result']?.toString(),
    );
  }

  /// ListView içinde güvenli id olarak kullanacağız.
  int get resolvedTicketId {
    return ticketId ?? id ?? 0;
  }
}

/// Bilet konum bilgisi.
class TicketLocationModel {
  final String? cityName;
  final String? districtName;
  final String? venueName;
  final String? venueAddress;

  TicketLocationModel({
    this.cityName,
    this.districtName,
    this.venueName,
    this.venueAddress,
  });

  factory TicketLocationModel.fromJson(Map<String, dynamic> json) {
    return TicketLocationModel(
      cityName: json['city_name']?.toString(),
      districtName: json['district_name']?.toString(),
      venueName: json['venue_name']?.toString(),
      venueAddress: json['venue_address']?.toString(),
    );
  }
}

/// QR kontrolünü yapan görevli bilgisi.
class CheckedByModel {
  final int id;
  final String fullName;

  CheckedByModel({
    required this.id,
    required this.fullName,
  });

  factory CheckedByModel.fromJson(Map<String, dynamic> json) {
    return CheckedByModel(
      id: int.tryParse(json['id'].toString()) ?? 0,
      fullName: json['full_name']?.toString() ?? '',
    );
  }
}