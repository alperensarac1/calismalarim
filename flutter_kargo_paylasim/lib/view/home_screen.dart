import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../viewmodel/home_vm.dart';
import '../service/api_client.dart';
import 'create_address_screen.dart';
import 'create_shipment_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});
  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  @override
  void initState() {
    super.initState();
    Future.microtask(() => context.read<HomeVM>().refresh());
  }

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<HomeVM>();
    final api = context.read<ApiClient>();

    return Scaffold(
      appBar: AppBar(
        title: const Text("Home"),
        actions: [
          TextButton(
            onPressed: () async {
              await Navigator.push(context, MaterialPageRoute(builder: (_) => CreateAddressScreen(api: api)));
              await vm.refresh();
            },
            child: const Text("+ Adres", style: TextStyle(color: Colors.white)),
          ),
          TextButton(
            onPressed: () async {
              await Navigator.push(context, MaterialPageRoute(builder: (_) => CreateShipmentScreen(api: api)));
              await vm.refresh();
            },
            child: const Text("+ Yeni", style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: vm.refresh,
        child: ListView(
          children: [
            if (vm.errorText != null)
              Padding(
                padding: const EdgeInsets.all(12),
                child: Text(vm.errorText!, style: const TextStyle(color: Colors.red)),
              ),

            const Padding(
              padding: EdgeInsets.all(12),
              child: Text("Gönderiler", style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
            ),
            ...vm.shipments.map((s) => ListTile(
              title: Text("ID: #${s.id} • ${s.status}"),
              subtitle: Text("Kod: ${s.pickupCode}${s.cargoCompanyName != null ? " • ${s.cargoCompanyName}" : ""}"),
            )),

            const Padding(
              padding: EdgeInsets.all(12),
              child: Text("Adresler", style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
            ),
            ...vm.addresses.map((a) => Card(
              margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(children: [
                      Expanded(child: Text(a.title, style: const TextStyle(fontWeight: FontWeight.bold))),
                      if (a.isDefault == 1)
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                          decoration: BoxDecoration(
                            color: Colors.green.withOpacity(0.15),
                            borderRadius: BorderRadius.circular(999),
                          ),
                          child: const Text("Varsayılan", style: TextStyle(fontSize: 12)),
                        ),
                    ]),
                    const SizedBox(height: 6),
                    Text("${a.district} / ${a.city}", style: const TextStyle(color: Colors.black54)),
                    const SizedBox(height: 4),
                    Text(a.addressLine, maxLines: 2, overflow: TextOverflow.ellipsis, style: const TextStyle(color: Colors.black54)),
                    const SizedBox(height: 10),
                    Row(
                      children: [
                        if (a.isDefault != 1)
                          OutlinedButton(
                            onPressed: () => vm.setDefaultAddress(a.id),
                            child: const Text("Varsayılan"),
                          ),
                        const SizedBox(width: 8),
                        OutlinedButton(
                          onPressed: () => vm.deleteAddress(a.id),
                          style: OutlinedButton.styleFrom(foregroundColor: Colors.red),
                          child: const Text("Sil"),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            )),

            if (vm.isLoading)
              const Padding(
                padding: EdgeInsets.all(12),
                child: Center(child: CircularProgressIndicator()),
              ),
          ],
        ),
      ),
    );
  }
}
