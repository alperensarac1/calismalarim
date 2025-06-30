import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../di/Providers.dart';
import '../utils/Constants.dart';
import '../viewmodel/MainVM.dart';
import 'QRKodScreen.dart';
import 'UrunlerimizScreen.dart';

class MainScreen extends ConsumerStatefulWidget {
  const MainScreen({super.key});

  @override
  ConsumerState<MainScreen> createState() => _MainScreenState();
}

class _MainScreenState extends ConsumerState<MainScreen> {
  bool qrGorunurMu = true;

  @override
  void initState() {
    super.initState();

    // Widget çizildikten sonra numara kontrolü yap
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final viewModel = ref.read(mainVMProvider.notifier);
      if (!viewModel.checkTelefonNumarasi()) {
        _showPhoneDialog(viewModel);
      }
    });
  }

  void _showPhoneDialog(MainVM viewModel) {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder:
          (_) => PhoneInputDialog(
            onPhoneEntered: (value) {
              viewModel.numarayiKaydet(value);
              Navigator.pop(context);
            },
            onCancel: () => Navigator.pop(context),
          ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final viewModel = ref.watch(mainVMProvider.notifier);
    final state = ref.watch(mainVMProvider);

    return Scaffold(
      backgroundColor: kbackgroundColor,
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            const SizedBox(height: 50),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                Text(
                  "Kullanımlar: ${state.kahveSayisi}/5",
                  style: TextStyle(fontSize: 20, color: kTextColor),
                ),
                Text(
                  "Hediye Kahve: ${state.hediyeKahve}",
                  style: TextStyle(fontSize: 20, color: kTextColor),
                ),
              ],
            ),
            const SizedBox(height: 40),
            Container(
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(30),
              ),
              child: Row(mainAxisAlignment: MainAxisAlignment.center,mainAxisSize: MainAxisSize.min,
                children: [
                  ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: qrGorunurMu ? Colors.pink.shade200 : Colors.white,
                      elevation: 2,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(20),
                      ),
                    ),
                    onPressed: () => setState(() => qrGorunurMu = true),
                    child: Text(
                      "QR Kod",
                      style: TextStyle(
                        fontWeight: FontWeight.bold,
                        color: qrGorunurMu ? Colors.white : Colors.black,
                      ),
                    ),
                  ),
                  ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: !qrGorunurMu ? Colors.pink.shade200 : Colors.white,
                      elevation: 2,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(20),
                      ),
                    ),
                    onPressed: () => setState(() => qrGorunurMu = false),
                    child:Text(
                      "Ürünlerimiz",
                      style: TextStyle(
                        fontWeight: FontWeight.bold,
                          color: !qrGorunurMu ? Colors.white : Colors.black,
                      ),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 20),
            Expanded(
              child:
                  qrGorunurMu
                      ? QRKodOlusturScreen() // senin QR ekran widget'ın
                      : UrunlerimizScreen(), // ürünlerimiz ekranı
            ),
          ],
        ),
      ),
    );
  }
}

class PhoneInputDialog extends StatelessWidget {
  final void Function(String) onPhoneEntered;
  final VoidCallback onCancel;

  const PhoneInputDialog({
    required this.onPhoneEntered,
    required this.onCancel,
  });

  @override
  Widget build(BuildContext context) {
    final controller = TextEditingController();

    return AlertDialog(
      title: Text("Bilgi Girişi"),
      content: TextField(
        controller: controller,
        decoration: InputDecoration(hintText: "Telefon numaranızı girin"),
        keyboardType: TextInputType.number,
      ),
      actions: [
        TextButton(
          onPressed: () {
            if (controller.text.isNotEmpty) {
              onPhoneEntered(controller.text);
            }
          },
          child: Text("Tamam"),
        ),
        TextButton(onPressed: onCancel, child: Text("İptal")),
      ],
    );
  }
}
