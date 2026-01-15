import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:provider/provider.dart';

import '../util/app_config.dart';
import '../viewmodel/mesajlar_viewmodel.dart';

class SingleChatScreen extends StatefulWidget {
  final int aliciId;
  final String aliciAd;
  static const routeName = '/singleChat';

  const SingleChatScreen({
    super.key,
    required this.aliciId,
    required this.aliciAd,
  });

  @override
  State<SingleChatScreen> createState() => _SingleChatScreenState();
}

class _SingleChatScreenState extends State<SingleChatScreen> {
  final _scrollController = ScrollController();
  final _textController = TextEditingController();
  final _picker = ImagePicker();

  XFile? _preview;

  int get benimId => AppConfig.kullaniciId;

  @override
  void initState() {
    super.initState();
    // LaunchedEffect(aliciId)
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<MesajlarViewModel>().mesajlariYuklePeriyodik(
        gonderenId: benimId,
        aliciId: widget.aliciId,
      );
    });
  }

  @override
  void dispose() {
    _scrollController.dispose();
    _textController.dispose();
    super.dispose();
  }

  void _scrollToBottom() {
    if (!_scrollController.hasClients) return;
    _scrollController.animateTo(
      _scrollController.position.maxScrollExtent,
      duration: const Duration(milliseconds: 250),
      curve: Curves.easeOut,
    );
  }

  Future<void> _pickImage() async {
    final x = await _picker.pickImage(source: ImageSource.gallery, imageQuality: 80);
    if (x != null) setState(() => _preview = x);
  }

  Future<void> _send() async {
    final text = _textController.text.trim();
    if (text.isEmpty && _preview == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Boş mesaj gönderilemez')),
      );
      return;
    }

    String? base64Img;
    if (_preview != null) {
      final bytes = await File(_preview!.path).readAsBytes();
      base64Img = base64Encode(bytes);
    }

    await context.read<MesajlarViewModel>().mesajGonder(
      gonderenId: benimId,
      aliciId: widget.aliciId,
      mesajText: text,
      base64Image: base64Img,
    );

    setState(() => _preview = null);
    _textController.clear();
    // gönderince aşağı kay
    WidgetsBinding.instance.addPostFrameCallback((_) => _scrollToBottom());
  }

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<MesajlarViewModel>();
    final mesajlar = vm.mesajlar;

    // Compose: LaunchedEffect(mesajlar.size) => mesaj sayısı değişince aşağı kay
    WidgetsBinding.instance.addPostFrameCallback((_) => _scrollToBottom());

    return Scaffold(
      appBar: AppBar(
        title: Text(widget.aliciAd),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => Navigator.pop(context),
        ),
      ),
      body: Column(
        children: [
          Expanded(
            child: ListView.builder(
              controller: _scrollController,
              itemCount: mesajlar.length,
              itemBuilder: (context, i) {
                final msg = mesajlar[i];
                final benMi = msg.gonderenId == benimId;

                return Align(
                  alignment: benMi ? Alignment.centerRight : Alignment.centerLeft,
                  child: Container(
                    margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                    padding: const EdgeInsets.all(10),
                    constraints: const BoxConstraints(maxWidth: 320),
                    decoration: BoxDecoration(
                      color: benMi ? Colors.blue.shade100 : Colors.grey.shade200,
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        if (msg.resimVar == 1 && msg.resimUrl != null && msg.resimUrl!.isNotEmpty)
                          Padding(
                            padding: const EdgeInsets.only(bottom: 8),
                            child: ClipRRect(
                              borderRadius: BorderRadius.circular(8),
                              child: Image.network(msg.resimUrl!, fit: BoxFit.cover),
                            ),
                          ),
                        if (msg.mesajText != null && msg.mesajText!.isNotEmpty)
                          Text(msg.mesajText!),
                        const SizedBox(height: 4),
                        Text(
                          msg.tarih,
                          style: const TextStyle(fontSize: 11, color: Colors.black54),
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),
          ),

          // Preview
          if (_preview != null)
            GestureDetector(
              onTap: () => setState(() => _preview = null),
              child: Container(
                height: 180,
                width: double.infinity,
                margin: const EdgeInsets.fromLTRB(12, 8, 12, 0),
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(10),
                  child: Image.file(File(_preview!.path), fit: BoxFit.cover),
                ),
              ),
            ),

          // Input bar
          SafeArea(
            top: false,
            child: Padding(
              padding: const EdgeInsets.fromLTRB(8, 8, 8, 8),
              child: Row(
                children: [
                  IconButton(
                    onPressed: _pickImage,
                    icon: const Icon(Icons.add),
                  ),
                  Expanded(
                    child: TextField(
                      controller: _textController,
                      decoration: const InputDecoration(
                        hintText: 'Mesaj yaz…',
                        border: OutlineInputBorder(),
                        isDense: true,
                      ),
                      textInputAction: TextInputAction.send,
                      onSubmitted: (_) => _send(),
                    ),
                  ),
                  const SizedBox(width: 8),
                  IconButton(
                    onPressed: _send,
                    icon: const Icon(Icons.send),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
