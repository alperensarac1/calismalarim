import 'dart:io';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:provider/provider.dart';

import '../components/post_card.dart';
import '../components/share_dialog.dart';
import '../components/snackbar.dart';
import '../model/gonderi_model.dart';
import '../service/api_client.dart';

import '../service/meme_service.dart';
import '../viewmodel/oda_vm.dart';


class OdaScreen extends StatefulWidget {
  final int roomId;
  final int userId;

  const OdaScreen({
    super.key,
    required this.roomId,
    required this.userId,
  });

  @override
  State<OdaScreen> createState() => _OdaScreenState();
}

class _OdaScreenState extends State<OdaScreen> {
  final _picker = ImagePicker();

  bool loading = false;
  List<GonderiModel> posts = [];

  late final MemeApiService api;

  @override
  void initState() {
    super.initState();
    api = MemeApiService(ApiClient.createDio());
    _refreshPosts();
  }

  Future<void> _refreshPosts() async {
    setState(() => loading = true);
    try {
      final list = await api.getAllMedia(widget.roomId);
      setState(() => posts = list);
    } catch (e) {
      setState(() => posts = []);
      if (mounted) showSnack(context, 'Gönderiler alınamadı: $e');
    } finally {
      if (mounted) setState(() => loading = false);
    }
  }

  Future<void> _pickAndShare() async {
    // image_picker: hem resim hem video için seçim
    // Basit: önce kullanıcıya seçenek soralım
    final type = await showModalBottomSheet<String>(
      context: context,
      builder: (ctx) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              title: const Text('Resim seç'),
              onTap: () => Navigator.pop(ctx, 'image'),
            ),
            ListTile(
              title: const Text('Video seç'),
              onTap: () => Navigator.pop(ctx, 'video'),
            ),
          ],
        ),
      ),
    );

    if (type == null) return;

    XFile? picked;
    if (type == 'image') {
      picked = await _picker.pickImage(source: ImageSource.gallery);
    } else {
      picked = await _picker.pickVideo(source: ImageSource.gallery);
    }

    if (picked == null) return;

    final isVideo = type == 'video';
    final filePath = picked.path;

    final caption = await showDialog<String?>(
      context: context,
      builder: (_) => ShareDialog(
        filePath: filePath,
        isVideo: isVideo,
      ),
    );

    if (caption == null) return;

    final odaVm = context.read<OdaVM>();

    if (isVideo) {
      // Video upload: MemeApiService içindeki uploadVideoMultipart kullan
      try {
        showSnack(context, 'Video yükleniyor...');
        final res = await api.uploadVideoMultipart(
          roomId: widget.roomId,
          userId: widget.userId,
          caption: caption,
          filePath: filePath,
        );

        showSnack(context, res.success ? '✅ Video başarıyla yüklendi' : '⚠️ Video yükleme başarısız: ${res.message}');
      } catch (e) {
        showSnack(context, 'Bağlantı hatası: $e');
      }
    } else {
      // Image upload (base64) VM üzerinden
      await odaVm.uploadImageBase64(
        filePath: filePath,
        roomId: widget.roomId,
        userId: widget.userId,
        caption: caption,
      );
      if (mounted && odaVm.uploadResult != null) {
        showSnack(context, odaVm.uploadResult!);
      }
    }

    await _refreshPosts();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Oda #${widget.roomId}')),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _pickAndShare,
        label: const Text('Paylaş'),
      ),
      body: loading
          ? const Center(child: CircularProgressIndicator())
          : posts.isEmpty
          ? const Center(child: Text('Henüz gönderi yok'))
          : RefreshIndicator(
        onRefresh: _refreshPosts,
        child: ListView.separated(
          padding: const EdgeInsets.all(12),
          itemCount: posts.length,
          separatorBuilder: (_, __) => const SizedBox(height: 12),
          itemBuilder: (_, i) => PostCard(
            item: posts[i],
            currentUserId: widget.userId,
          ),
        ),
      ),
    );
  }
}
