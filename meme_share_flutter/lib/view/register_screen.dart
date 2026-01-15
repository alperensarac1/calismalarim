import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../components/snackbar.dart';
import '../viewmodel/register_vm.dart';

class RegisterScreen extends StatefulWidget {
  final VoidCallback onNavigateLogin;
  final VoidCallback onRegisterSuccess;

  const RegisterScreen({
    super.key,
    required this.onNavigateLogin,
    required this.onRegisterSuccess,
  });

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final _username = TextEditingController();
  final _password = TextEditingController();

  @override
  void dispose() {
    _username.dispose();
    _password.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<RegisterVM>(
      builder: (context, vm, _) {
        return Scaffold(
          body: Padding(
            padding: const EdgeInsets.all(24),
            child: Center(
              child: SingleChildScrollView(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Text('Kayıt Ol', style: Theme.of(context).textTheme.headlineMedium),
                    const SizedBox(height: 24),
                    TextField(
                      controller: _username,
                      decoration: const InputDecoration(labelText: 'Kullanıcı adı'),
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: _password,
                      decoration: const InputDecoration(labelText: 'Şifre'),
                      obscureText: true,
                    ),
                    const SizedBox(height: 20),
                    ElevatedButton(
                      onPressed: vm.isLoading
                          ? null
                          : () async {
                        final u = _username.text.trim();
                        final p = _password.text.trim();
                        if (u.isEmpty || p.isEmpty) {
                          showSnack(context, 'Tüm alanları doldurun');
                          return;
                        }

                        await vm.registerUser(u, p);
                        final res = vm.registerResult;

                        if (!mounted) return;

                        if (res?.success == true) {
                          showSnack(context, 'Kayıt başarılı! Giriş yapabilirsiniz');
                          widget.onRegisterSuccess();
                        } else {
                          showSnack(context, 'Hata: ${res?.message ?? vm.error ?? "Sunucu hatası"}');
                        }
                      },
                      child: vm.isLoading
                          ? const SizedBox(height: 18, width: 18, child: CircularProgressIndicator())
                          : const Text('Kayıt Ol'),
                    ),
                    TextButton(
                      onPressed: vm.isLoading ? null : widget.onNavigateLogin,
                      child: const Text('Zaten hesabın var mı? Giriş yap'),
                    ),
                  ],
                ),
              ),
            ),
          ),
        );
      },
    );
  }
}
