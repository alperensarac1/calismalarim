import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../viewmodel/auth_vm.dart';


class RegisterPage extends StatefulWidget {
  const RegisterPage({super.key});

  @override
  State<RegisterPage> createState() => _RegisterPageState();
}

class _RegisterPageState extends State<RegisterPage> {
  final name = TextEditingController();
  final email = TextEditingController();
  final pass = TextEditingController();

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<AuthVm>();

    return Scaffold(
      appBar: AppBar(title: const Text("Kayıt Ol")),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            TextField(controller: name, decoration: const InputDecoration(labelText: "Ad Soyad")),
            TextField(controller: email, decoration: const InputDecoration(labelText: "E-posta")),
            TextField(controller: pass, obscureText: true, decoration: const InputDecoration(labelText: "Şifre")),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: vm.inFlight
                    ? null
                    : () async {
                  final ok = await vm.register(name.text.trim(), email.text.trim(), pass.text);
                  if (!mounted) return;
                  if (ok) Navigator.pop(context);
                },
                child: Text(vm.inFlight ? "Kaydediliyor..." : "Kayıt Ol"),
              ),
            ),
            if (vm.error != null) Text(vm.error!, style: const TextStyle(color: Colors.red)),
          ],
        ),
      ),
    );
  }
}
