import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../entity/session_manager.dart';
import '../viewmodel/entryekle_viewmodel.dart';



class EntryEkleScreen extends StatefulWidget {
  final SessionManager session;
  final VoidCallback onSaved;
  final VoidCallback onBack;

  const EntryEkleScreen({
    super.key,
    required this.session,
    required this.onSaved,
    required this.onBack,
  });

  @override
  State<EntryEkleScreen> createState() => _EntryEkleScreenState();
}

class _EntryEkleScreenState extends State<EntryEkleScreen> {
  final _titleCtrl = TextEditingController();
  final _contentCtrl = TextEditingController();

  bool _handledResult = false;

  @override
  void dispose() {
    _titleCtrl.dispose();
    _contentCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<EntryEkleViewModel>();
    final ui = vm.ui;

    // Compose'taki LaunchedEffect(addResult) benzeri
    WidgetsBinding.instance.addPostFrameCallback((_) async {
      final res = vm.addResult;
      if (res == null) {
        _handledResult = false;
        return;
      }
      if (_handledResult) return;
      _handledResult = true;

      if (res.success) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Entry eklendi')),
        );
        vm.clearResult();
        widget.onSaved();
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(res.message ?? 'Hata oluştu')),
        );
        vm.clearResult();
      }
    });

    return Scaffold(
      appBar: AppBar(
        title: const Text('Entry Ekle'),
        centerTitle: true,
        leading: TextButton(
          onPressed: widget.onBack,
          child: const Text('Geri'),
        ),
        leadingWidth: 72,
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            TextField(
              controller: _titleCtrl,
              decoration: const InputDecoration(
                labelText: 'Başlık',
                border: OutlineInputBorder(),
              ),
              textInputAction: TextInputAction.next,
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _contentCtrl,
              minLines: 6,
              maxLines: 10,
              decoration: const InputDecoration(
                labelText: 'İçerik',
                border: OutlineInputBorder(),
                alignLabelWithHint: true,
              ),
            ),
            const SizedBox(height: 16),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: ui.loading
                    ? null
                    : () async {
                  final title = _titleCtrl.text.trim();
                  final content = _contentCtrl.text.trim();

                  if (title.isEmpty || content.isEmpty) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Tüm alanları doldurun')),
                    );
                    return;
                  }

                  final userId = await widget.session.getUserId();
                  await context.read<EntryEkleViewModel>().addEntry(
                    userId: userId,
                    title: title,
                    content: content,
                  );
                },
                child: ui.loading
                    ? const SizedBox(
                  height: 20,
                  width: 20,
                  child: CircularProgressIndicator(strokeWidth: 2),
                )
                    : const Text('Kaydet'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
