import 'package:flutter/foundation.dart';
import '../service/api_client.dart';
import '../service/api_resp.dart';
import '../service/endpoints.dart';
import '../model/address.dart';
import '../model/shipment.dart';

class HomeVM extends ChangeNotifier {
  final ApiClient api;
  HomeVM(this.api);

  bool isLoading = false;
  String? errorText;

  List<Shipment> shipments = [];
  List<Address> addresses = [];

  Future<void> refresh() async {
    isLoading = true;
    errorText = null;
    notifyListeners();

    try {
      final shipJson = await api.getJson(Endpoints.shipmentList, query: {"type": "all"});
      final addrJson = await api.getJson(Endpoints.addressList);

      final shipResp = ApiResp.fromJson(shipJson, (d) {
        final items = (d["items"] as List? ?? []).cast<Map<String, dynamic>>();
        return items.map((e) => Shipment.fromJson(e)).toList();
      });

      final addrResp = ApiResp.fromJson(addrJson, (d) {
        final items = (d["items"] as List? ?? []).cast<Map<String, dynamic>>();
        return items.map((e) => Address.fromJson(e)).toList();
      });

      if (!shipResp.ok) throw ApiError(shipResp.error ?? "shipment_list failed");
      if (!addrResp.ok) throw ApiError(addrResp.error ?? "address_list failed");

      shipments = shipResp.data ?? [];
      addresses = addrResp.data ?? [];
    } catch (e) {
      errorText = e.toString();
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  Future<void> setDefaultAddress(int id) async {
    isLoading = true;
    errorText = null;
    notifyListeners();

    try {
      final j = await api.postJson(Endpoints.addressSetDefault, {"id": id});
      final r = ApiResp<bool>.fromJson(j, (d) => true);
      if (!r.ok) throw ApiError(r.error ?? "Varsayılan ayarlanamadı");

      // hızlı UI update
      addresses = addresses
          .map((a) => Address(
        id: a.id,
        title: a.title,
        city: a.city,
        district: a.district,
        addressLine: a.addressLine,
        isDefault: a.id == id ? 1 : 0,
      ))
          .toList();
    } catch (e) {
      errorText = e.toString();
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  Future<void> deleteAddress(int id) async {
    isLoading = true;
    errorText = null;
    notifyListeners();

    try {
      final j = await api.postJson(Endpoints.addressDelete, {"id": id});
      final r = ApiResp<bool>.fromJson(j, (d) => true);
      if (!r.ok) throw ApiError(r.error ?? "Adres silinemedi");

      addresses.removeWhere((a) => a.id == id);
    } catch (e) {
      errorText = e.toString();
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }
}
