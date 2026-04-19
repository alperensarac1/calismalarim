import 'dart:io';
import 'dart:typed_data';

import 'package:flutter/foundation.dart';
import 'package:image/image.dart' as img;
import 'package:path_provider/path_provider.dart';

import 'image_processor.dart';

class EditorController extends ChangeNotifier {
  img.Image? originalImage;
  img.Image? workingImage;

  double tolerance = 60;
  String infoText =
      'Önce fotoğraf seçin. Sonra silmek istediğiniz bölgeye dokunun.';
  bool isProcessing = false;

  bool hasActivePreview = false;
  img.Image? previewBaseImage;
  PointInt? lastTappedPoint;

  final List<img.Image> _undoStack = [];
  final int maxUndoCount = 10;

  int _previewRequestId = 0;

  bool get canUndo => _undoStack.isNotEmpty;

  void loadImageBytes(Uint8List bytes) {
    final decoded = img.decodeImage(bytes);
    if (decoded == null) {
      infoText = 'Resim yüklenemedi.';
      notifyListeners();
      return;
    }

    _previewRequestId++;
    originalImage = img.Image.from(decoded);
    workingImage = img.Image.from(decoded);
    previewBaseImage = null;
    lastTappedPoint = null;
    hasActivePreview = false;
    _undoStack.clear();
    isProcessing = false;
    infoText = 'Fotoğraf yüklendi. Silmek istediğiniz bölgeye dokunun.';
    notifyListeners();
  }

  void setLoadingState() {
    isProcessing = true;
    infoText = 'Fotoğraf yükleniyor...';
    notifyListeners();
  }

  void onToleranceChanged(double newValue) {
    tolerance = newValue;
    notifyListeners();

    if (hasActivePreview) {
      _renderPreviewFromActiveState();
    }
  }

  void onImageTapped(int x, int y) {
    final current = workingImage;
    if (current == null) return;
    if (x < 0 || y < 0 || x >= current.width || y >= current.height) return;

    _commitActivePreviewIfNeeded();
    _saveStateForUndo(current);

    previewBaseImage = img.Image.from(current);
    lastTappedPoint = PointInt(x, y);
    hasActivePreview = true;
    infoText = 'Canlı önizleme hazırlanıyor...';
    notifyListeners();

    _renderPreviewFromActiveState();
  }

  void undo() {
    _previewRequestId++;
    if (_undoStack.isEmpty) return;

    workingImage = _undoStack.removeLast();
    hasActivePreview = false;
    previewBaseImage = null;
    lastTappedPoint = null;
    isProcessing = false;
    infoText = 'Son işlem geri alındı.';
    notifyListeners();
  }

  void reset() {
    _previewRequestId++;
    if (originalImage == null) return;

    workingImage = img.Image.from(originalImage!);
    hasActivePreview = false;
    previewBaseImage = null;
    lastTappedPoint = null;
    _undoStack.clear();
    isProcessing = false;
    infoText = 'Görsel sıfırlandı.';
    notifyListeners();
  }

  Future<File?> exportAsPngFile() async {
    _commitActivePreviewIfNeeded();

    final image = workingImage;
    if (image == null) return null;

    isProcessing = true;
    infoText = 'PNG hazırlanıyor...';
    notifyListeners();

    try {
      final pngBytes = Uint8List.fromList(img.encodePng(image));
      final dir = await getTemporaryDirectory();
      final file = File(
        '${dir.path}/bg_removed_${DateTime.now().millisecondsSinceEpoch}.png',
      );
      await file.writeAsBytes(pngBytes, flush: true);

      isProcessing = false;
      infoText = 'PNG dosyası hazır.';
      notifyListeners();
      return file;
    } catch (_) {
      isProcessing = false;
      infoText = 'PNG dışa aktarma başarısız oldu.';
      notifyListeners();
      return null;
    }
  }

  void _renderPreviewFromActiveState() {
    final base = previewBaseImage;
    final point = lastTappedPoint;
    if (base == null || point == null) return;

    final requestId = ++_previewRequestId;
    final currentTolerance = tolerance;

    isProcessing = true;
    infoText = 'Canlı önizleme güncelleniyor...';
    notifyListeners();

    compute<_PreviewTaskInput, img.Image>(
      _previewCompute,
      _PreviewTaskInput(
        baseBytes: Uint8List.fromList(img.encodePng(base)),
        x: point.x,
        y: point.y,
        tolerance: currentTolerance,
      ),
    ).then((result) {
      if (requestId != _previewRequestId) return;

      workingImage = result;
      isProcessing = false;
      infoText = 'Canlı önizleme aktif. Tolerans: ${currentTolerance.toInt()}';
      notifyListeners();
    }).catchError((_) {
      if (requestId != _previewRequestId) return;

      isProcessing = false;
      infoText = 'Önizleme oluşturulamadı.';
      notifyListeners();
    });
  }

  static img.Image _previewCompute(_PreviewTaskInput input) {
    final decoded = img.decodeImage(input.baseBytes)!;
    return ImageProcessor.removeConnectedRegionByColor(
      source: decoded,
      startX: input.x,
      startY: input.y,
      tolerance: input.tolerance,
    );
  }

  void _commitActivePreviewIfNeeded() {
    if (!hasActivePreview) return;
    hasActivePreview = false;
    previewBaseImage = null;
    lastTappedPoint = null;
  }

  void _saveStateForUndo(img.Image image) {
    if (_undoStack.length >= maxUndoCount) {
      _undoStack.removeAt(0);
    }
    _undoStack.add(img.Image.from(image));
  }
  void setIdleMessage(String text) {
    isProcessing = false;
    infoText = text;
    notifyListeners();
  }
}

class PointInt {
  final int x;
  final int y;

  PointInt(this.x, this.y);
}

class _PreviewTaskInput {
  final Uint8List baseBytes;
  final int x;
  final int y;
  final double tolerance;

  _PreviewTaskInput({
    required this.baseBytes,
    required this.x,
    required this.y,
    required this.tolerance,
  });

}