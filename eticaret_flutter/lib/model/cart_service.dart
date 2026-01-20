

import '../api/api_client.dart';
import '../api/api_response.dart';
import '../api/endpoints.dart';
import 'cart_model.dart';

class CartService {
  final ApiClient api;
  CartService(this.api);

  Future<CartDto> getCart() async {
    final res = await api.dio.get(Endpoints.cart);
    final wrapped = ApiResponse.fromJson(res.data, (d) => CartDto.fromJson(d));
    if (!wrapped.ok || wrapped.data == null) throw Exception(wrapped.error ?? "Cart hata");
    return wrapped.data!;
  }

  Future<AddToCartResponse> addToCart(AddToCartRequest body) async {
    final res = await api.dio.post(Endpoints.cartAdd, data: body.toJson());
    final wrapped = ApiResponse.fromJson(res.data, (d) => AddToCartResponse.fromJson(d));
    if (!wrapped.ok || wrapped.data == null) throw Exception(wrapped.error ?? "Sepete ekleme hata");
    return wrapped.data!;
  }

  Future<bool> updateItem(int itemId, UpdateCartItemRequest body) async {
    final res = await api.dio.post(
      Endpoints.cartItem as String,
      queryParameters: {"id": itemId},
      data: body.toJson(),
    );
    final wrapped = ApiResponse.fromJson(res.data, (d) => BasicOk.fromJson(d));
    if (!wrapped.ok || wrapped.data == null) throw Exception(wrapped.error ?? "Güncelleme hata");
    return wrapped.data!.ok;
  }

  Future<bool> deleteItem(int itemId) async {
    final res = await api.dio.delete(
      Endpoints.cartItem as String,
      queryParameters: {"id": itemId},
    );
    final wrapped = ApiResponse.fromJson(res.data, (d) => BasicOk.fromJson(d));
    if (!wrapped.ok || wrapped.data == null) throw Exception(wrapped.error ?? "Silme hata");
    return wrapped.data!.ok;
  }
}
