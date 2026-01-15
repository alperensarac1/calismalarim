import 'package:flutter/material.dart';

import '../service/api_service.dart';

class RegistrationDialog extends StatefulWidget {
  final void Function(int id) onKayitBasarili;

  const RegistrationDialog({super.key, required this.onKayitBasarili});

  @override
  State<RegistrationDialog> createState() => _RegistrationDialogState();
}

class _RegistrationDialogState extends State<RegistrationDialog> {
  final _api = ApiService();
  final _ad = TextEditingController();
  final _numara = TextEditingController();

  bool _loading = false;
  String? _hata;

  @override
  void dispose() {
    _ad.dispose();
    _numara.dispose();
    super.dispose();
  }

  Future<void> _kayitOl() async {
    final ad = _ad.text.trim();
    final numara = _numara.text.trim();

    if (ad.isEmpty || numara.isEmpty) {
      setState(() => _hata = 'Boş alan bırakma!');
      return;
    }

    setState(() {
      _loading = true;
      _hata = null;
    });

    try {
      final resp = await _api.kullaniciKayit(ad: ad, numara: numara);
      if (resp.success && resp.id != null) {
        if (!mounted) return;
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Kayıt başarılı!')),
        );
        widget.onKayitBasarili(resp.id!);
      } else {
        setState(() => _hata = resp.error ?? 'Kayıt başarısız');
      }
    } catch (e) {
      setState(() => _hata = 'Hata: $e');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    // Compose: onDismissRequest = {} (kapatmayı engelle)
    return WillPopScope(
      onWillPop: () async => false,
      child: AlertDialog(
        title: const Text('Kayıt Ol'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: _ad,
              decoration: const InputDecoration(labelText: 'Ad'),
            ),
            const SizedBox(height: 8),
            TextField(
              controller: _numara,
              keyboardType: TextInputType.phone,
              decoration: const InputDecoration(labelText: 'Numara'),
            ),
            const SizedBox(height: 12),
            if (_loading) const CircularProgressIndicator(),
            if (_hata != null) ...[
              const SizedBox(height: 8),
              Text(_hata!, style: TextStyle(color: Theme.of(context).colorScheme.error)),
            ],
          ],
        ),
        actions: [
          TextButton(
            onPressed: _loading ? null : _kayitOl,
            child: const Text('Kayıt Ol'),
          ),
        ],
      ),
    );
  }
}
