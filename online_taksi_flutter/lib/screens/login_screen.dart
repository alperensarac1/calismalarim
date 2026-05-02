import 'package:flutter/material.dart';
import 'package:online_taksi_flutter/screens/register_screen.dart';
import '../core/session_manager.dart';
import '../network/api_client.dart';
import '../repositories/auth_repository.dart';
import '../repositories/driver_home_screen.dart';
import 'customer_home_screen.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final phoneController = TextEditingController();
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

  Future<void> _login() async {
    final phone = phoneController.text.trim();
    final password = passwordController.text.trim();

    if (phone.isEmpty || password.isEmpty) {
      _showMessage("Telefon ve şifre zorunlu");
      return;
    }

    setState(() => isLoading = true);

    try {
      final response = await authRepository.login(
        phone: phone,
        password: password,
      );

      await sessionManager.saveAuth(
        token: response.accessToken,
        userId: response.userId,
        fullName: response.fullName,
        role: response.role,
      );

      if (!mounted) return;

      if (response.role == "driver") {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(builder: (_) => const DriverHomeScreen()),
        );
      } else {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(builder: (_) => const CustomerHomeScreen()),
        );
      }
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
    phoneController.dispose();
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
                  "onlinetaksi Giriş",
                  style: TextStyle(fontSize: 28, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 24),
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
                    onPressed: isLoading ? null : _login,
                    child: isLoading
                        ? const CircularProgressIndicator()
                        : const Text("Giriş Yap"),
                  ),
                ),
                const SizedBox(height: 12),
                TextButton(
                  onPressed: isLoading
                      ? null
                      : () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => const RegisterScreen(),
                      ),
                    );
                  },
                  child: const Text("Hesabın yok mu? Kayıt ol"),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
