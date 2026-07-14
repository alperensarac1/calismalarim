import 'package:ebilet_flutter/screens/auth/register_screen.dart';
import 'package:flutter/material.dart';

import '../../core/api_service.dart';
import '../../core/app_colors.dart';
import '../../core/session_manager.dart';
import '../../widgets/app_button.dart';
import '../../widgets/app_text_field.dart';
import '../home/home_screen.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final TextEditingController _emailController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();

  bool _isLoading = false;

  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  bool _isValidEmail(String email) {
    final regex = RegExp(r'^\S+@\S+\.\S+$');
    return regex.hasMatch(email);
  }

  void _showMessage(String message) {
    if (!mounted) return;

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
      ),
    );
  }

  Future<void> _login() async {
    final email = _emailController.text.trim();
    final password = _passwordController.text.trim();

    if (email.isEmpty) {
      _showMessage('E-posta zorunludur');
      return;
    }

    if (!_isValidEmail(email)) {
      _showMessage('Geçerli bir e-posta giriniz');
      return;
    }

    if (password.isEmpty) {
      _showMessage('Şifre zorunludur');
      return;
    }

    if (password.length < 6) {
      _showMessage('Şifre en az 6 karakter olmalıdır');
      return;
    }

    setState(() {
      _isLoading = true;
    });

    try {
      final response = await ApiService.login(
        email: email,
        password: password,
      );

      if (!mounted) return;

      setState(() {
        _isLoading = false;
      });

      if (!response.success) {
        _showMessage(response.message);
        return;
      }

      final user = response.data;

      if (user == null) {
        _showMessage('Kullanıcı bilgisi alınamadı');
        return;
      }

      await SessionManager.saveUser(user);

      if (!mounted) return;

      Navigator.pushAndRemoveUntil(
        context,
        MaterialPageRoute(
          builder: (_) => const HomeScreen(),
        ),
            (route) => false,
      );
    } catch (e) {
      if (!mounted) return;

      setState(() {
        _isLoading = false;
      });

      _showMessage(e.toString());
    }
  }

  void _goRegister() {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => const RegisterScreen(),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: Column(
              children: [
                _buildHeader(),
                const SizedBox(height: 28),
                _buildFormCard(),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildHeader() {
    return const Column(
      children: [
        Icon(
          Icons.confirmation_number_rounded,
          size: 70,
          color: AppColors.blue,
        ),
        SizedBox(height: 14),
        Text(
          'Etkinlik Bileti',
          textAlign: TextAlign.center,
          style: TextStyle(
            fontSize: 32,
            fontWeight: FontWeight.bold,
            color: AppColors.darkText,
          ),
        ),
        SizedBox(height: 8),
        Text(
          'Etkinlikleri keşfet, biletini QR kodla kullan.',
          textAlign: TextAlign.center,
          style: TextStyle(
            fontSize: 15,
            color: AppColors.grayText,
          ),
        ),
      ],
    );
  }

  Widget _buildFormCard() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(22),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.08),
            blurRadius: 14,
            offset: const Offset(0, 6),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Giriş Yap',
            style: TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: AppColors.darkText,
            ),
          ),
          const SizedBox(height: 18),
          AppTextField(
            controller: _emailController,
            hintText: 'E-posta',
            keyboardType: TextInputType.emailAddress,
            prefixIcon: Icons.email_outlined,
          ),
          const SizedBox(height: 12),
          AppTextField(
            controller: _passwordController,
            hintText: 'Şifre',
            obscureText: true,
            textInputAction: TextInputAction.done,
            prefixIcon: Icons.lock_outline,
          ),
          const SizedBox(height: 18),
          AppButton(
            text: 'Giriş Yap',
            backgroundColor: AppColors.blue,
            isLoading: _isLoading,
            onPressed: _login,
          ),
          const SizedBox(height: 12),
          Center(
            child: TextButton(
              onPressed: _isLoading ? null : _goRegister,
              child: const Text(
                'Hesabın yok mu? Kayıt ol',
                style: TextStyle(
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}