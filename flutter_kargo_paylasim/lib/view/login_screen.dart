import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../service/api_client.dart';
import '../storage/token_store.dart';
import '../viewmodel/auth_vm.dart';
import 'register_screen.dart';

class LoginScreen extends StatefulWidget {
  final Future<void> Function() onLoggedIn;
  const LoginScreen({super.key, required this.onLoggedIn});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final phoneCtrl = TextEditingController();
  final passCtrl = TextEditingController();

  @override
  void dispose() {
    phoneCtrl.dispose();
    passCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final api = context.read<ApiClient>();
    final tokenStore = TokenStore();

    return ChangeNotifierProvider(
      create: (_) => AuthVM(api, tokenStore),
      child: Consumer<AuthVM>(
        builder: (context, vm, _) {
          return Scaffold(
            appBar: AppBar(title: const Text("Giriş")),
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
                    decoration: const InputDecoration(labelText: "Telefon (05xx... veya +905xx...)"),
                  ),
                  const SizedBox(height: 10),
                  TextField(
                    controller: passCtrl,
                    obscureText: true,
                    decoration: const InputDecoration(labelText: "Şifre"),
                  ),
                  const SizedBox(height: 16),
                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton(
                      onPressed: vm.isLoading
                          ? null
                          : () async {
                        final ok = await vm.login(phoneCtrl.text, passCtrl.text);
                        if (!ok) return;
                        await widget.onLoggedIn();
                      },
                      child: vm.isLoading
                          ? const SizedBox(height: 18, width: 18, child: CircularProgressIndicator(strokeWidth: 2))
                          : const Text("Giriş Yap"),
                    ),
                  ),
                  const SizedBox(height: 10),
                  TextButton(
                    onPressed: () {
                      Navigator.push(context, MaterialPageRoute(builder: (_) => RegisterScreen(api: api)));
                    },
                    child: const Text("Hesabın yok mu? Kayıt Ol"),
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
