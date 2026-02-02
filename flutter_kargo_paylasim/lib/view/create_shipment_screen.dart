import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../service/api_client.dart';
import '../viewmodel/create_shipment_vm.dart';

class CreateShipmentScreen extends StatelessWidget {
  final ApiClient api;
  const CreateShipmentScreen({super.key, required this.api});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => CreateShipmentVM(api),
      child: const _CreateShipmentBody(),
    );
  }
}

class _CreateShipmentBody extends StatefulWidget {
  const _CreateShipmentBody();

  @override
  State<_CreateShipmentBody> createState() => _CreateShipmentBodyState();
}

class _CreateShipmentBodyState extends State<_CreateShipmentBody> {
  final phoneCtrl = TextEditingController();

  @override
  void dispose() {
    phoneCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<CreateShipmentVM>();

    return Scaffold(
      appBar: AppBar(title: const Text("Yeni Gönderi")),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            if (vm.errorText != null)
              Padding(
                padding: const EdgeInsets.only(bottom: 10),
                child: Text(vm.errorText!, style: const TextStyle(color: Colors.red)),
              ),

            TextField(
              controller: phoneCtrl,
              keyboardType: TextInputType.phone,
              decoration: const InputDecoration(labelText: "Alıcı Telefon"),
              onChanged: (v) => vm.phone = v,
            ),
            const SizedBox(height: 12),

            Row(
              children: [
                Expanded(
                  child: ElevatedButton(
                    onPressed: vm.isLoading ? null : () async => vm.lookup(),
                    child: vm.isLoading
                        ? const SizedBox(height: 18, width: 18, child: CircularProgressIndicator(strokeWidth: 2))
                        : const Text("Bul"),
                  ),
                ),
                const SizedBox(width: 10),
                OutlinedButton(
                  onPressed: vm.isLoading ? null : () => vm.reset(),
                  child: const Text("İptal"),
                ),
              ],
            ),

            const SizedBox(height: 14),
            if (vm.lookupText != null) Text(vm.lookupText!),

            const Spacer(),

            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: (!vm.canConfirm || vm.isLoading)
                    ? null
                    : () async {
                  final d = await vm.confirmCreate();
                  if (d == null) return;

                  if (!context.mounted) return;
                  await showDialog(
                    context: context,
                    builder: (_) => AlertDialog(
                      title: const Text("Gönderi Oluşturuldu"),
                      content: Text("Kod: ${d.pickupCode}\nSon geçerlilik: ${d.codeExpiresAt}"),
                      actions: [
                        TextButton(
                          onPressed: () {
                            // kopyalama istersen Clipboard ekleyebiliriz
                            Navigator.pop(context);
                          },
                          child: const Text("Tamam"),
                        ),
                      ],
                    ),
                  );

                  if (context.mounted) Navigator.pop(context); // home'a dön
                },
                child: const Text("Onayla ve Oluştur"),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
