import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../viewmodel/giris_viewmodel.dart';

class LoginScreen extends StatefulWidget {
  final Future<void> Function(int userId, String username) onLoginSuccess;
  final VoidCallback onGoRegister;

  const LoginScreen({
    super.key,
    required this.onLoginSuccess,
    required this.onGoRegister,
  });

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _userCtrl = TextEditingController();
  final _passCtrl = TextEditingController();

  @override
  void dispose() {
    _userCtrl.dispose();
    _passCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<GirisViewModel>();

    // sonuç geldiğinde bir kere yakala
    WidgetsBinding.instance.addPostFrameCallback((_) async {
      final res = vm.loginResult;
      if (res == null) return;

      // tekrar tekrar tetiklenmesin diye: result'ı null'lamayı VM'ye ekleyebilirsin
      // şimdilik snack + success handling yapıyoruz.
      if (res.success && res.userId != null) {
        await widget.onLoginSuccess(res.userId!, _userCtrl.text.trim());
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(res.message ?? 'Giriş başarısız')),
        );
      }
    });

    return Scaffold(
      appBar: AppBar(title: const Text('Giriş Yap'), centerTitle: true),
      body: Padding(
        padding: const EdgeInsets.all(20),
        child: Center(
          child: SingleChildScrollView(
            child: Column(
              children: [
                TextField(
                  controller: _userCtrl,
                  decoration: const InputDecoration(
                    labelText: 'Kullanıcı adı',
                    border: OutlineInputBorder(),
                  ),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: _passCtrl,
                  obscureText: true,
                  decoration: const InputDecoration(
                    labelText: 'Şifre',
                    border: OutlineInputBorder(),
                  ),
                ),
                const SizedBox(height: 16),

                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    onPressed: vm.loading
                        ? null
                        : () async {
                      final u = _userCtrl.text.trim();
                      final p = _passCtrl.text.trim();
                      if (u.isEmpty || p.isEmpty) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(content: Text('Lütfen tüm alanları doldurun')),
                        );
                        return;
                      }
                      await context.read<GirisViewModel>().login(username: u, password: p);
                    },
                    child: vm.loading
                        ? const SizedBox(
                      height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2),
                    )
                        : const Text('Giriş Yap'),
                  ),
                ),

                Align(
                  alignment: Alignment.centerRight,
                  child: TextButton(
                    onPressed: widget.onGoRegister,
                    child: const Text('Hesabın yok mu? Kayıt ol'),
                  ),
                )
              ],
            ),
          ),
        ),
      ),
    );
  }
}
