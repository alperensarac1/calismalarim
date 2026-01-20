

import 'package:eticaret_flutter/model/product_model.dart';

import '../api/api_client.dart';
import '../api/endpoints.dart';
import 'category_model.dart';

class ProductService {
  final ApiClient api;
  ProductService(this.api);

  Future<List<CategoryDto>> categories() async {
    final res = await api.dio.get(Endpoints.categories);
    return (res.data as List).map((e) => CategoryDto.fromJson(e)).toList();
  }

  Future<ProductListPage> products({
    required int page,
    int? cat,
    String? q,
    bool discount = false,
    String sort = "newest",
  }) async {
    final res = await api.dio.get(Endpoints.products, queryParameters: {
      "page": page,
      if (cat != null) "cat": cat,
      if (q != null && q.isNotEmpty) "q": q,
      "discount": discount ? 1 : 0,
      "sort": sort,
    });
    return ProductListPage.fromJson(res.data);
  }
}
