import 'package:flutter/material.dart';

import '../../core/api_service.dart';
import '../../core/app_colors.dart';
import '../../core/session_manager.dart';
import '../../widgets/app_button.dart';
import '../../widgets/app_text_field.dart';
import '../home/home_screen.dart';

class RegisterScreen extends StatefulWidget {
  const RegisterScreen({super.key});

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final TextEditingController _fullNameController = TextEditingController();
  final TextEditingController _emailController = TextEditingController();
  final TextEditingController _phoneController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();

  bool _isLoading = false;

  @override
  void dispose() {
    _fullNameController.dispose();
    _emailController.dispose();
    _phoneController.dispose();
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

  Future<void> _register() async {
    final fullName = _fullNameController.text.trim();
    final email = _emailController.text.trim();
    final phone = _phoneController.text.trim();
    final password = _passwordController.text.trim();

    if (fullName.isEmpty) {
      _showMessage('Ad soyad zorunludur');
      return;
    }

    if (fullName.length < 3) {
      _showMessage('Ad soyad en az 3 karakter olmalıdır');
      return;
    }

    if (email.isEmpty) {
      _showMessage('E-posta zorunludur');
      return;
    }

    if (!_isValidEmail(email)) {
      _showMessage('Geçerli bir e-posta giriniz');
      return;
    }

    if (phone.isNotEmpty && phone.length < 10) {
      _showMessage('Telefon numarası eksik görünüyor');
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
      final response = await ApiService.register(
        fullName: fullName,
        email: email,
        phone: phone,
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

  void _goLogin() {
    Navigator.pop(context);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text('Kayıt Ol'),
        backgroundColor: AppColors.background,
        foregroundColor: AppColors.darkText,
        elevation: 0,
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: Column(
            children: [
              _buildHeader(),
              const SizedBox(height: 24),
              _buildFormCard(),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildHeader() {
    return const Column(
      children: [
        Icon(
          Icons.person_add_alt_1_rounded,
          size: 64,
          color: AppColors.green,
        ),
        SizedBox(height: 12),
        Text(
          'Yeni Hesap Oluştur',
          textAlign: TextAlign.center,
          style: TextStyle(
            fontSize: 28,
            fontWeight: FontWeight.bold,
            color: AppColors.darkText,
          ),
        ),
        SizedBox(height: 8),
        Text(
          'Etkinlik biletlerini kolayca satın almak için kayıt ol.',
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
            'Kayıt Bilgileri',
            style: TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: AppColors.darkText,
            ),
          ),
          const SizedBox(height: 18),
          AppTextField(
            controller: _fullNameController,
            hintText: 'Ad Soyad',
            prefixIcon: Icons.person_outline,
          ),
          const SizedBox(height: 12),
          AppTextField(
            controller: _emailController,
            hintText: 'E-posta',
            keyboardType: TextInputType.emailAddress,
            prefixIcon: Icons.email_outlined,
          ),
          const SizedBox(height: 12),
          AppTextField(
            controller: _phoneController,
            hintText: 'Telefon',
            keyboardType: TextInputType.phone,
            prefixIcon: Icons.phone_outlined,
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
            text: 'Kayıt Ol',
            backgroundColor: AppColors.green,
            isLoading: _isLoading,
            onPressed: _register,
          ),
          const SizedBox(height: 12),
          Center(
            child: TextButton(
              onPressed: _isLoading ? null : _goLogin,
              child: const Text(
                'Zaten hesabın var mı? Giriş yap',
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