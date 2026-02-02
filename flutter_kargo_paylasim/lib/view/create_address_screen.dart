import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../service/api_client.dart';
import '../viewmodel/address_create_vm.dart';

class CreateAddressScreen extends StatelessWidget {
  final ApiClient api;
  const CreateAddressScreen({super.key, required this.api});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => AddressCreateVM(api),
      child: const _CreateAddressBody(),
    );
  }
}

class _CreateAddressBody extends StatefulWidget {
  const _CreateAddressBody();

  @override
  State<_CreateAddressBody> createState() => _CreateAddressBodyState();
}

class _CreateAddressBodyState extends State<_CreateAddressBody> {
  final titleCtrl = TextEditingController();
  final cityCtrl = TextEditingController();
  final districtCtrl = TextEditingController();
  final neighCtrl = TextEditingController();
  final lineCtrl = TextEditingController();
  final postalCtrl = TextEditingController();

  @override
  void dispose() {
    titleCtrl.dispose();
    cityCtrl.dispose();
    districtCtrl.dispose();
    neighCtrl.dispose();
    lineCtrl.dispose();
    postalCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<AddressCreateVM>();

    return Scaffold(
      appBar: AppBar(title: const Text("Adres Ekle")),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            if (vm.errorText != null)
              Padding(
                padding: const EdgeInsets.only(bottom: 10),
                child: Text(vm.errorText!, style: const TextStyle(color: Colors.red)),
              ),

            TextField(controller: titleCtrl, decoration: const InputDecoration(labelText: "Adres Başlığı (Ev/İş)")),
            const SizedBox(height: 10),
            TextField(controller: cityCtrl, decoration: const InputDecoration(labelText: "Şehir")),
            const SizedBox(height: 10),
            TextField(controller: districtCtrl, decoration: const InputDecoration(labelText: "İlçe")),
            const SizedBox(height: 10),
            TextField(controller: neighCtrl, decoration: const InputDecoration(labelText: "Mahalle (opsiyonel)")),
            const SizedBox(height: 10),
            TextField(controller: lineCtrl, maxLines: 3, decoration: const InputDecoration(labelText: "Açık Adres")),
            const SizedBox(height: 10),
            TextField(controller: postalCtrl, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: "Posta Kodu (opsiyonel)")),

            const SizedBox(height: 16),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: vm.isLoading
                    ? null
                    : () async {
                  vm.title = titleCtrl.text;
                  vm.city = cityCtrl.text;
                  vm.district = districtCtrl.text;
                  vm.neighborhood = neighCtrl.text;
                  vm.addressLine = lineCtrl.text;
                  vm.postal = postalCtrl.text;

                  final ok = await vm.save();
                  if (!ok) return;
                  if (context.mounted) Navigator.pop(context);
                },
                child: vm.isLoading
                    ? const SizedBox(height: 18, width: 18, child: CircularProgressIndicator(strokeWidth: 2))
                    : const Text("Kaydet"),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
