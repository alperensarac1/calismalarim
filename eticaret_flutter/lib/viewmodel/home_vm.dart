import 'package:flutter/foundation.dart';

import '../model/category_model.dart';
import '../model/product_model.dart';
import '../model/product_service.dart';


class HomeVm extends ChangeNotifier {
  final ProductService service;

  List<CategoryDto> categories = [];
  List<ProductListDto> items = [];
  int page = 1;
  int total = 0;
  bool loading = false;
  String? error;

  int? cat;
  String? q;
  bool discount = false;
  String sort = "newest";

  HomeVm(this.service);

  Future<void> init() async {
    await loadCategories();
    await reload();
  }

  Future<void> loadCategories() async {
    try {
      categories = await service.categories();
      notifyListeners();
    } catch (e) {
      // kategori hata olursa liste yine de çalışsın
    }
  }

  Future<void> reload() async {
    page = 1;
    items = [];
    total = 0;
    await loadNext();
  }

  Future<void> loadNext() async {
    if (loading) return;
    if (items.isNotEmpty && items.length >= total && total != 0) return;

    loading = true;
    error = null;
    notifyListeners();
    try {
      final resp = await service.products(
        page: page,
        cat: cat,
        q: q,
        discount: discount,
        sort: sort,
      );
      total = resp.total;
      // append + distinct
      final merged = [...items, ...resp.items];
      final uniq = <int, ProductListDto>{};
      for (final p in merged) {
        uniq[p.id] = p;
      }
      items = uniq.values.toList();
      page = page + 1;
    } catch (e) {
      error = e.toString();
    } finally {
      loading = false;
      notifyListeners();
    }
  }

  void setFilters({int? newCat, String? newQ, bool? newDiscount, String? newSort}) {
    cat = newCat ?? cat;
    q = newQ ?? q;
    discount = newDiscount ?? discount;
    sort = newSort ?? sort;
    reload();
  }
}
