
import '../api/api_client.dart';
import '../api/api_response.dart';
import '../api/endpoints.dart';
import 'order_model.dart';

class OrdersService {
  final ApiClient api;
  OrdersService(this.api);

  Future<CheckoutResponse> checkout(CheckoutRequest body) async {
    final res = await api.dio.post(Endpoints.checkout, data: body.toJson());
    final wrapped = ApiResponse.fromJson(res.data, (d) => CheckoutResponse.fromJson(d));
    if (!wrapped.ok || wrapped.data == null) throw Exception(wrapped.error ?? "Checkout hata");
    return wrapped.data!;
  }

  Future<List<OrderSummaryDto>> getOrders() async {
    final res = await api.dio.get(Endpoints.orders);
    final wrapped = ApiResponse.fromJson(res.data, (d) {
      return (d as List).map((e) => OrderSummaryDto.fromJson(e)).toList();
    });
    if (!wrapped.ok || wrapped.data == null) throw Exception(wrapped.error ?? "Orders hata");
    return wrapped.data!;
  }

  Future<OrderDetailDto> getOrderDetail(int orderId) async {
    final res = await api.dio.get(Endpoints.order, queryParameters: {"id": orderId});
    final wrapped = ApiResponse.fromJson(res.data, (d) => OrderDetailDto.fromJson(d));
    if (!wrapped.ok || wrapped.data == null) throw Exception(wrapped.error ?? "Order detail hata");
    return wrapped.data!;
  }
}
