import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../service/api_client.dart';
import '../storage/token_store.dart';
import '../viewmodel/auth_vm.dart';

class RegisterScreen extends StatefulWidget {
  final ApiClient api;
  const RegisterScreen({super.key, required this.api});

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final firstCtrl = TextEditingController();
  final lastCtrl = TextEditingController();
  final phoneCtrl = TextEditingController();
  final tcCtrl = TextEditingController();
  final passCtrl = TextEditingController();

  final addrTitleCtrl = TextEditingController();
  final cityCtrl = TextEditingController();
  final districtCtrl = TextEditingController();
  final neighCtrl = TextEditingController();
  final addrLineCtrl = TextEditingController();
  final postalCtrl = TextEditingController();

  @override
  void dispose() {
    firstCtrl.dispose();
    lastCtrl.dispose();
    phoneCtrl.dispose();
    tcCtrl.dispose();
    passCtrl.dispose();
    addrTitleCtrl.dispose();
    cityCtrl.dispose();
    districtCtrl.dispose();
    neighCtrl.dispose();
    addrLineCtrl.dispose();
    postalCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final tokenStore = TokenStore();

    return ChangeNotifierProvider(
      create: (_) => AuthVM(widget.api, tokenStore),
      child: Consumer<AuthVM>(
        builder: (context, vm, _) {
          return Scaffold(
            appBar: AppBar(title: const Text("Kayıt Ol")),
            body: SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Column(
                children: [
                  if (vm.errorText != null)
                    Padding(
                      padding: const EdgeInsets.only(bottom: 10),
                      child: Text(vm.errorText!, style: const TextStyle(color: Colors.red)),
                    ),

                  // Kişisel
                  Align(
                    alignment: Alignment.centerLeft,
                    child: Text("Kişisel Bilgiler", style: Theme.of(context).textTheme.titleMedium),
                  ),
                  const SizedBox(height: 8),
                  TextField(controller: firstCtrl, decoration: const InputDecoration(labelText: "İsim")),
                  const SizedBox(height: 10),
                  TextField(controller: lastCtrl, decoration: const InputDecoration(labelText: "Soyisim")),
                  const SizedBox(height: 10),
                  TextField(
                    controller: phoneCtrl,
                    keyboardType: TextInputType.phone,
                    decoration: const InputDecoration(labelText: "Telefon (05xx... veya +905xx...)"),
                  ),
                  const SizedBox(height: 10),
                  TextField(
                    controller: tcCtrl,
                    keyboardType: TextInputType.number,
                    decoration: const InputDecoration(labelText: "TC Kimlik No (11 hane)"),
                  ),
                  const SizedBox(height: 10),
                  TextField(
                    controller: passCtrl,
                    obscureText: true,
                    decoration: const InputDecoration(labelText: "Şifre"),
                  ),

                  const SizedBox(height: 18),
                  Align(
                    alignment: Alignment.centerLeft,
                    child: Text("Adres Bilgileri", style: Theme.of(context).textTheme.titleMedium),
                  ),
                  const SizedBox(height: 8),
                  TextField(controller: addrTitleCtrl, decoration: const InputDecoration(labelText: "Adres başlığı (Ev/İş)")),
                  const SizedBox(height: 10),
                  TextField(controller: cityCtrl, decoration: const InputDecoration(labelText: "Şehir")),
                  const SizedBox(height: 10),
                  TextField(controller: districtCtrl, decoration: const InputDecoration(labelText: "İlçe")),
                  const SizedBox(height: 10),
                  TextField(controller: neighCtrl, decoration: const InputDecoration(labelText: "Mahalle (opsiyonel)")),
                  const SizedBox(height: 10),
                  TextField(
                    controller: addrLineCtrl,
                    maxLines: 3,
                    decoration: const InputDecoration(labelText: "Açık adres"),
                  ),
                  const SizedBox(height: 10),
                  TextField(
                    controller: postalCtrl,
                    keyboardType: TextInputType.number,
                    decoration: const InputDecoration(labelText: "Posta Kodu (opsiyonel)"),
                  ),

                  const SizedBox(height: 16),
                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton(
                      onPressed: vm.isLoading
                          ? null
                          : () async {
                        final ok = await vm.register(
                          first: firstCtrl.text,
                          last: lastCtrl.text,
                          phone: phoneCtrl.text,
                          tc: tcCtrl.text,
                          password: passCtrl.text,
                          addressTitle: addrTitleCtrl.text,
                          city: cityCtrl.text,
                          district: districtCtrl.text,
                          neighborhood: neighCtrl.text,
                          addressLine: addrLineCtrl.text,
                          postal: postalCtrl.text,
                        );
                        if (!ok) return;

                        if (mounted) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(content: Text("Kayıt başarılı. Giriş yapabilirsin.")),
                          );
                          Navigator.pop(context);
                        }
                      },
                      child: vm.isLoading
                          ? const SizedBox(height: 18, width: 18, child: CircularProgressIndicator(strokeWidth: 2))
                          : const Text("Kayıt Ol"),
                    ),
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}
