import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../components/snackbar.dart';
import '../viewmodel/login_vm.dart';

class LoginScreen extends StatefulWidget {
  final VoidCallback onNavigateRegister;
  final void Function(int userId) onLoginSuccess;

  const LoginScreen({
    super.key,
    required this.onNavigateRegister,
    required this.onLoginSuccess,
  });

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
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
    return Consumer<LoginVM>(
      builder: (context, vm, _) {
        // Compose LaunchedEffect(loginResult) karşılığı: build içinde sadece "bir kere" tetiklememek için
        // success olduğunda navigation'ı buton sonrası bekliyoruz (aşağıda).
        return Scaffold(
          body: Padding(
            padding: const EdgeInsets.all(24),
            child: Center(
              child: SingleChildScrollView(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Text('Giriş Yap', style: Theme.of(context).textTheme.headlineMedium),
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

                        await vm.loginUser(u, p);

                        final res = vm.loginResult;
                        if (!mounted) return;

                        if (res?.success == true) {
                          showSnack(context, 'Giriş başarılı!');
                          widget.onLoginSuccess(res!.userId);
                        } else {
                          showSnack(context, 'Hata: ${res?.message ?? vm.error ?? "Giriş başarısız"}');
                        }
                      },
                      child: vm.isLoading
                          ? const SizedBox(height: 18, width: 18, child: CircularProgressIndicator())
                          : const Text('Giriş Yap'),
                    ),
                    TextButton(
                      onPressed: vm.isLoading ? null : widget.onNavigateRegister,
                      child: const Text('Hesabın yok mu? Kayıt ol'),
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
