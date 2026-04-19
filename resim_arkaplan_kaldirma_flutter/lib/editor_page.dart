import 'dart:io';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:share_plus/share_plus.dart';

import 'editor_controller.dart';
import 'zoomable_image_canvas.dart';

class EditorPage extends StatefulWidget {
  const EditorPage({super.key});

  @override
  State<EditorPage> createState() => _EditorPageState();
}

class _EditorPageState extends State<EditorPage> {
  final controller = EditorController();
  final picker = ImagePicker();

  @override
  void initState() {
    super.initState();
    controller.addListener(_onControllerChanged);
  }

  @override
  void dispose() {
    controller.removeListener(_onControllerChanged);
    controller.dispose();
    super.dispose();
  }

  void _onControllerChanged() {
    if (mounted) {
      setState(() {});
    }
  }

  Future<void> _pickImage() async {
    controller.setLoadingState();

    final file = await picker.pickImage(source: ImageSource.gallery);
    if (file == null) {
      controller.setIdleMessage('Fotoğraf seçilmedi.');
      return;
    }

    final bytes = await file.readAsBytes();
    controller.loadImageBytes(bytes);
  }

  Future<void> _saveAsPng() async {
    final file = await controller.exportAsPngFile();
    if (file == null) return;

    await Share.shareXFiles(
      [XFile(file.path)],
      text: 'Arka plansız PNG',
    );
  }

  @override
  Widget build(BuildContext context) {
    final state = controller;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Background Remover'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          children: [
            Row(
              children: [
                Expanded(
                  child: FilledButton(
                    onPressed: state.isProcessing ? null : _pickImage,
                    child: const Text('Fotoğraf Seç'),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: FilledButton(
                    onPressed:
                    state.canUndo && !state.isProcessing ? state.undo : null,
                    child: const Text('Geri Al'),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                Expanded(
                  child: FilledButton(
                    onPressed: state.workingImage != null && !state.isProcessing
                        ? state.reset
                        : null,
                    child: const Text('Sıfırla'),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: FilledButton(
                    onPressed: state.workingImage != null && !state.isProcessing
                        ? _saveAsPng
                        : null,
                    child: const Text('PNG Kaydet'),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            Align(
              alignment: Alignment.centerLeft,
              child: Text(state.infoText),
            ),
            const SizedBox(height: 12),
            Align(
              alignment: Alignment.centerLeft,
              child: Text(
                'Tolerans: ${state.tolerance.toInt()}',
                style: const TextStyle(fontWeight: FontWeight.bold),
              ),
            ),
            Slider(
              value: state.tolerance,
              min: 0,
              max: 255,
              onChanged: state.workingImage != null
                  ? controller.onToleranceChanged
                  : null,
            ),
            const SizedBox(height: 12),
            Expanded(
              child: ClipRRect(
                borderRadius: BorderRadius.circular(12),
                child: ZoomableImageCanvas(
                  image: state.workingImage,
                  onTapPixel: controller.onImageTapped,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

}