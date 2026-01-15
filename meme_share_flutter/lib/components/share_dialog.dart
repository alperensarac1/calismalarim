import 'dart:io';
import 'package:flutter/material.dart';

class ShareDialog extends StatefulWidget {
  final String filePath;
  final bool isVideo;

  const ShareDialog({
    super.key,
    required this.filePath,
    required this.isVideo,
  });

  @override
  State<ShareDialog> createState() => _ShareDialogState();
}

class _ShareDialogState extends State<ShareDialog> {
  final _caption = TextEditingController();

  @override
  void dispose() {
    _caption.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('Paylaş'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (!widget.isVideo)
            ClipRRect(
              borderRadius: BorderRadius.circular(10),
              child: Image.file(
                File(widget.filePath),
                height: 200,
                width: double.infinity,
                fit: BoxFit.cover,
                errorBuilder: (_, __, ___) => Container(
                  height: 200,
                  alignment: Alignment.center,
                  child: const Text('Önizleme yok'),
                ),
              ),
            )
          else
            Container(
              height: 200,
              width: double.infinity,
              alignment: Alignment.center,
              color: Colors.black12,
              child: const Text('Video seçildi'),
            ),
          const SizedBox(height: 12),
          TextField(
            controller: _caption,
            decoration: const InputDecoration(labelText: 'Açıklama'),
          ),
        ],
      ),
      actions: [
        TextButton(onPressed: () => Navigator.pop(context, null), child: const Text('İptal')),
        TextButton(
          onPressed: () => Navigator.pop(context, _caption.text.trim()),
          child: const Text('Gönder'),
        ),
      ],
    );
  }
}
