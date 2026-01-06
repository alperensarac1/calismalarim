import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../viewmodel/kayit_viewmodel.dart';


class RegisterScreen extends StatefulWidget {
  final VoidCallback onRegisterSuccess;
  final VoidCallback onGoLogin;

  const RegisterScreen({
    super.key,
    required this.onRegisterSuccess,
    required this.onGoLogin,
  });

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final _userCtrl = TextEditingController();
  final _mailCtrl = TextEditingController();
  final _passCtrl = TextEditingController();

  @override
  void dispose() {
    _userCtrl.dispose();
    _mailCtrl.dispose();
    _passCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<KayitViewModel>();

    WidgetsBinding.instance.addPostFrameCallback((_) {
      final res = vm.registerResult;
      if (res == null) return;

      if (res.success) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Kayıt başarılı. Giriş yapabilirsiniz.')),
        );
        widget.onRegisterSuccess();
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(res.message ?? 'Kayıt başarısız')),
        );
      }
    });

    return Scaffold(
      appBar: AppBar(title: const Text('Kayıt Ol'), centerTitle: true),
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
                  controller: _mailCtrl,
                  keyboardType: TextInputType.emailAddress,
                  decoration: const InputDecoration(
                    labelText: 'E-posta',
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
                      final e = _mailCtrl.text.trim();
                      final p = _passCtrl.text.trim();
                      if (u.isEmpty || e.isEmpty || p.isEmpty) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(content: Text('Lütfen tüm alanları doldurun')),
                        );
                        return;
                      }
                      await context.read<KayitViewModel>().register(
                        username: u,
                        email: e,
                        password: p,
                      );
                    },
                    child: vm.loading
                        ? const SizedBox(
                      height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2),
                    )
                        : const Text('Kayıt Ol'),
                  ),
                ),

                Align(
                  alignment: Alignment.centerRight,
                  child: TextButton(
                    onPressed: widget.onGoLogin,
                    child: const Text('Zaten hesabın var mı? Giriş yap'),
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
