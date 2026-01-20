import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../model/product_model.dart';
import '../viewmodel/home_vm.dart';


class HomePage extends StatefulWidget {
  const HomePage({super.key});
  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final search = TextEditingController();
  final scroll = ScrollController();

  @override
  void initState() {
    super.initState();
    final vm = context.read<HomeVm>();
    vm.init();

    scroll.addListener(() {
      if (scroll.position.pixels >= scroll.position.maxScrollExtent - 400) {
        context.read<HomeVm>().loadNext();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<HomeVm>();

    return Scaffold(
      appBar: AppBar(title: const Text("Anasayfa")),
      body: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          children: [
            TextField(controller: search, decoration: const InputDecoration(labelText: "Ara")),
            const SizedBox(height: 8),
            Row(
              children: [
                ElevatedButton(
                  onPressed: () => vm.setFilters(newQ: search.text.trim().isEmpty ? null : search.text.trim()),
                  child: const Text("Ara"),
                ),
                const SizedBox(width: 8),
                FilterChip(
                  label: const Text("İndirim"),
                  selected: vm.discount,
                  onSelected: (v) => vm.setFilters(newDiscount: v),
                ),
              ],
            ),
            const SizedBox(height: 8),

            // Categories
            SizedBox(
              height: 44,
              child: ListView.separated(
                scrollDirection: Axis.horizontal,
                itemCount: vm.categories.length + 1,
                separatorBuilder: (_, __) => const SizedBox(width: 8),
                itemBuilder: (_, i) {
                  if (i == 0) {
                    return ActionChip(
                      label: const Text("Tümü"),
                      onPressed: () => vm.setFilters(newCat: null),
                    );
                  }
                  final c = vm.categories[i - 1];
                  return ActionChip(
                    label: Text(c.name),
                    onPressed: () => vm.setFilters(newCat: c.id),
                  );
                },
              ),
            ),

            if (vm.loading) const LinearProgressIndicator(),
            if (vm.error != null) Text(vm.error!, style: const TextStyle(color: Colors.red)),

            const SizedBox(height: 8),
            Expanded(
              child: GridView.builder(
                controller: scroll,
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 2,
                  childAspectRatio: 0.74,
                  crossAxisSpacing: 10,
                  mainAxisSpacing: 10,
                ),
                itemCount: vm.items.length,
                itemBuilder: (_, i) => _ProductCard(vm.items[i]),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _ProductCard extends StatelessWidget {
  final ProductListDto p;
  const _ProductCard(this.p);

  @override
  Widget build(BuildContext context) {
    return Card(
      child: InkWell(
        onTap: () {
          // TODO: Product detail route ekleriz
        },
        child: Padding(
          padding: const EdgeInsets.all(10),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: Container(
                  width: double.infinity,
                  color: Colors.black12,
                  child: p.imageUrl == null
                      ? const Icon(Icons.image_not_supported)
                      : Image.network(p.imageUrl!, fit: BoxFit.cover),
                ),
              ),
              const SizedBox(height: 8),
              Text(p.name, maxLines: 2),
              const SizedBox(height: 4),
              Text("₺${p.price.toStringAsFixed(2)}", style: const TextStyle(fontWeight: FontWeight.bold)),
            ],
          ),
        ),
      ),
    );
  }
}
