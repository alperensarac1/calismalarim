import 'package:flutter/foundation.dart';
import '../service/api_client.dart';
import '../service/api_resp.dart';
import '../service/endpoints.dart';

class AddressCreateVM extends ChangeNotifier {
  final ApiClient api;
  AddressCreateVM(this.api);

  bool isLoading = false;
  String? errorText;

  String title = "";
  String city = "";
  String district = "";
  String neighborhood = "";
  String addressLine = "";
  String postal = "";

  Future<bool> save() async {
    errorText = null;
    if (title.isEmpty || city.isEmpty || district.isEmpty || addressLine.isEmpty) {
      errorText = "Başlık, şehir, ilçe ve açık adres zorunlu.";
      notifyListeners();
      return false;
    }

    isLoading = true;
    notifyListeners();

    try {
      final j = await api.postJson(Endpoints.addressCreate, {
        "title": title,
        "city": city,
        "district": district,
        "neighborhood": neighborhood,
        "address_line": addressLine,
        "postal_code": postal,
      });

      final r = ApiResp.fromJson(j, (d) => d);
      if (!r.ok) throw ApiError(r.error ?? "Adres eklenemedi");
      return true;
    } catch (e) {
      errorText = e.toString();
      return false;
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }
}
