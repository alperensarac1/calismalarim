import 'package:flutter/foundation.dart';
import '../service//api_client.dart';
import '../service/api_resp.dart';
import '../service/endpoints.dart';

class LookupReceiverData {
  final int receiverUserId;
  final String maskedFirst;
  final String maskedLast;

  LookupReceiverData({required this.receiverUserId, required this.maskedFirst, required this.maskedLast});

  factory LookupReceiverData.fromJson(Map<String, dynamic> j) => LookupReceiverData(
    receiverUserId: (j["receiver_user_id"] ?? 0) as int,
    maskedFirst: (j["masked_first_name"] ?? "") as String,
    maskedLast: (j["masked_last_name"] ?? "") as String,
  );
}

class CreateShipmentData {
  final int shipmentId;
  final String pickupCode;
  final String status;
  final String codeExpiresAt;

  CreateShipmentData({required this.shipmentId, required this.pickupCode, required this.status, required this.codeExpiresAt});

  factory CreateShipmentData.fromJson(Map<String, dynamic> j) => CreateShipmentData(
    shipmentId: (j["shipment_id"] ?? 0) as int,
    pickupCode: (j["pickup_code"] ?? "") as String,
    status: (j["status"] ?? "") as String,
    codeExpiresAt: (j["code_expires_at"] ?? "") as String,
  );
}

class CreateShipmentVM extends ChangeNotifier {
  final ApiClient api;
  CreateShipmentVM(this.api);

  bool isLoading = false;
  String? errorText;

  String phone = "";
  String? lookupText;
  bool canConfirm = false;
  String? confirmedPhoneE164;

  void reset() {
    errorText = null;
    lookupText = null;
    canConfirm = false;
    confirmedPhoneE164 = null;
    notifyListeners();
  }

  Future<void> lookup() async {
    reset();

    if (phone.trim().length < 10) {
      errorText = "Telefonu kontrol et.";
      notifyListeners();
      return;
    }

    isLoading = true;
    notifyListeners();

    try {
      // backend normalize_phone_e164_tr zaten yapıyor; istersen client’ta da normalize edersin.
      final j = await api.postJson(Endpoints.receiverLookup, {"phone": phone.trim()});
      final r = ApiResp.fromJson(j, (d) => LookupReceiverData.fromJson(d as Map<String, dynamic>));

      if (!r.ok || r.data == null) throw ApiError(r.error ?? "User not found");

      final d = r.data!;
      lookupText = "Bulunan: ${d.maskedFirst} ${d.maskedLast} • Onaylıyor musun?";
      canConfirm = true;
      confirmedPhoneE164 = phone.trim();
    } catch (e) {
      errorText = e.toString();
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  Future<CreateShipmentData?> confirmCreate() async {
    if (confirmedPhoneE164 == null) {
      errorText = "Önce kişiyi bul.";
      notifyListeners();
      return null;
    }

    isLoading = true;
    errorText = null;
    notifyListeners();

    try {
      final j = await api.postJson(Endpoints.shipmentCreate, {
        "receiver_phone": confirmedPhoneE164,
        "sender_address_id": null,
      });

      final r = ApiResp.fromJson(j, (d) => CreateShipmentData.fromJson(d as Map<String, dynamic>));
      if (!r.ok || r.data == null) {
        final msg = r.error ?? "Create shipment failed";
        if (msg.toLowerCase().contains("receiver address not found") || msg.toUpperCase().contains("RECEIVER_ADDRESS_MISSING")) {
          throw ApiError("Bu kullanıcı henüz adresini kaydetmemiş.");
        }
        throw ApiError(msg);
      }
      return r.data;
    } catch (e) {
      errorText = e.toString();
      return null;
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }
}
