import 'package:eticaret_flutter/auth/register_page.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../main_shell.dart';
import '../viewmodel/auth_vm.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final email = TextEditingController();
  final pass = TextEditingController();

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<AuthVm>();

    return Scaffold(
      appBar: AppBar(title: const Text("Giriş")),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            TextField(controller: email, decoration: const InputDecoration(labelText: "E-posta")),
            TextField(controller: pass, obscureText: true, decoration: const InputDecoration(labelText: "Şifre")),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: vm.inFlight
                    ? null
                    : () async {
                  final ok = await vm.login(email.text.trim(), pass.text);
                  if (!mounted) return;
                  if (ok) {
                    Navigator.pushReplacement(
                      context,
                      MaterialPageRoute(builder: (_) => const MainShell()),
                    );
                  }
                },
                child: Text(vm.inFlight ? "Giriş..." : "Giriş Yap"),
              ),
            ),
            TextButton(
              onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const RegisterPage())),
              child: const Text("Kayıt ol"),
            ),
            if (vm.error != null) Text(vm.error!, style: const TextStyle(color: Colors.red)),
          ],
        ),
      ),
    );
  }
}
