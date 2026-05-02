import 'package:flutter/material.dart';
import '../core/session_manager.dart';
import '../network/api_client.dart';
import '../repositories/auth_repository.dart';
import 'customer_home_screen.dart';

class RegisterScreen extends StatefulWidget {
  const RegisterScreen({super.key});

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final fullNameController = TextEditingController();
  final phoneController = TextEditingController();
  final emailController = TextEditingController();
  final passwordController = TextEditingController();

  late final SessionManager sessionManager;
  late final AuthRepository authRepository;

  bool isLoading = false;

  @override
  void initState() {
    super.initState();

    sessionManager = SessionManager();
    authRepository = AuthRepository(
      apiClient: ApiClient(sessionManager: sessionManager),
    );
  }

  Future<void> _register() async {
    final fullName = fullNameController.text.trim();
    final phone = phoneController.text.trim();
    final email = emailController.text.trim();
    final password = passwordController.text.trim();

    if (fullName.isEmpty || phone.isEmpty || password.isEmpty) {
      _showMessage("Ad soyad, telefon ve şifre zorunlu");
      return;
    }

    setState(() => isLoading = true);

    try {
      final response = await authRepository.registerCustomer(
        fullName: fullName,
        phone: phone,
        email: email.isEmpty ? null : email,
        password: password,
      );

      await sessionManager.saveAuth(
        token: response.accessToken,
        userId: response.userId,
        fullName: response.fullName,
        role: response.role,
      );

      if (!mounted) return;

      Navigator.pushAndRemoveUntil(
        context,
        MaterialPageRoute(builder: (_) => const CustomerHomeScreen()),
            (_) => false,
      );
    } catch (e) {
      _showMessage(e.toString());
    } finally {
      if (mounted) {
        setState(() => isLoading = false);
      }
    }
  }

  void _showMessage(String text) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(text)),
    );
  }

  @override
  void dispose() {
    fullNameController.dispose();
    phoneController.dispose();
    emailController.dispose();
    passwordController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Center(
          child: SingleChildScrollView(
            child: Column(
              children: [
                const Text(
                  "Müşteri Kayıt",
                  style: TextStyle(fontSize: 28, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 24),
                TextField(
                  controller: fullNameController,
                  decoration: const InputDecoration(
                    labelText: "Ad Soyad",
                    border: OutlineInputBorder(),
                  ),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: phoneController,
                  keyboardType: TextInputType.phone,
                  decoration: const InputDecoration(
                    labelText: "Telefon",
                    border: OutlineInputBorder(),
                  ),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: emailController,
                  keyboardType: TextInputType.emailAddress,
                  decoration: const InputDecoration(
                    labelText: "Email opsiyonel",
                    border: OutlineInputBorder(),
                  ),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: passwordController,
                  obscureText: true,
                  decoration: const InputDecoration(
                    labelText: "Şifre",
                    border: OutlineInputBorder(),
                  ),
                ),
                const SizedBox(height: 20),
                SizedBox(
                  width: double.infinity,
                  child: FilledButton(
                    onPressed: isLoading ? null : _register,
                    child: isLoading
                        ? const CircularProgressIndicator()
                        : const Text("Kayıt Ol"),
                  ),
                ),
                const SizedBox(height: 12),
                TextButton(
                  onPressed: isLoading ? null : () => Navigator.pop(context),
                  child: const Text("Geri Dön"),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
