import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:image/image.dart' as img;

class ZoomableImageCanvas extends StatefulWidget {
  final img.Image? image;
  final void Function(int x, int y) onTapPixel;

  const ZoomableImageCanvas({
    super.key,
    required this.image,
    required this.onTapPixel,
  });

  @override
  State<ZoomableImageCanvas> createState() => _ZoomableImageCanvasState();
}

class _ZoomableImageCanvasState extends State<ZoomableImageCanvas> {
  final TransformationController _transformationController =
  TransformationController();

  Uint8List? _pngBytes;

  bool _magnifierVisible = false;
  Offset _touchPosition = Offset.zero;

  @override
  void didUpdateWidget(covariant ZoomableImageCanvas oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.image != oldWidget.image) {
      _pngBytes = widget.image == null
          ? null
          : Uint8List.fromList(img.encodePng(widget.image!));
    }
  }

  @override
  void initState() {
    super.initState();
    if (widget.image != null) {
      _pngBytes = Uint8List.fromList(img.encodePng(widget.image!));
    }
  }

  @override
  Widget build(BuildContext context) {
    final image = widget.image;

    return LayoutBuilder(
      builder: (context, constraints) {
        return Container(
          color: Colors.grey.shade300,
          child: image == null || _pngBytes == null
              ? const Center(child: Text('Fotoğraf seçilmedi'))
              : Stack(
            children: [
              GestureDetector(
                onLongPressStart: (details) {
                  setState(() {
                    _magnifierVisible = true;
                    _touchPosition = details.localPosition;
                  });
                },
                onLongPressMoveUpdate: (details) {
                  setState(() {
                    _touchPosition = details.localPosition;
                  });
                },
                onLongPressEnd: (_) {
                  setState(() {
                    _magnifierVisible = false;
                  });
                },
                onTapUp: (details) {
                  final mapped = _mapTouchToImagePixel(
                    touch: details.localPosition,
                    viewSize: constraints.biggest,
                    image: image,
                  );

                  if (mapped != null) {
                    widget.onTapPixel(mapped.$1, mapped.$2);
                  }
                },
                child: InteractiveViewer(
                  transformationController: _transformationController,
                  minScale: 1,
                  maxScale: 5,
                  child: Center(
                    child: Image.memory(
                      _pngBytes!,
                      gaplessPlayback: true,
                    ),
                  ),
                ),
              ),
              if (_magnifierVisible)
                Positioned(
                  top: 16,
                  right: 16,
                  child: _Magnifier(
                    sourceBytes: _pngBytes!,
                    image: image,
                    touch: _touchPosition,
                    editorSize: constraints.biggest,
                  ),
                ),
            ],
          ),
        );
      },
    );
  }

  (int, int)? _mapTouchToImagePixel({
    required Offset touch,
    required Size viewSize,
    required img.Image image,
  }) {
    final matrix = _transformationController.value;

    final inverse = Matrix4.inverted(matrix);
    final scenePoint = MatrixUtils.transformPoint(inverse, touch);

    final fitted = applyBoxFit(
      BoxFit.contain,
      Size(image.width.toDouble(), image.height.toDouble()),
      viewSize,
    );

    final renderSize = fitted.destination;
    final dx = (viewSize.width - renderSize.width) / 2;
    final dy = (viewSize.height - renderSize.height) / 2;

    final localX = scenePoint.dx - dx;
    final localY = scenePoint.dy - dy;

    if (localX < 0 ||
        localY < 0 ||
        localX > renderSize.width ||
        localY > renderSize.height) {
      return null;
    }

    final px = (localX / renderSize.width * image.width).toInt();
    final py = (localY / renderSize.height * image.height).toInt();

    if (px < 0 || py < 0 || px >= image.width || py >= image.height) {
      return null;
    }

    return (px, py);
  }
}

class _Magnifier extends StatelessWidget {
  final Uint8List sourceBytes;
  final img.Image image;
  final Offset touch;
  final Size editorSize;

  const _Magnifier({
    required this.sourceBytes,
    required this.image,
    required this.touch,
    required this.editorSize,
  });

  @override
  Widget build(BuildContext context) {
    const double size = 180;
    const double zoom = 2.5;

    final fitted = applyBoxFit(
      BoxFit.contain,
      Size(image.width.toDouble(), image.height.toDouble()),
      editorSize,
    );

    final renderSize = fitted.destination;
    final dx = (editorSize.width - renderSize.width) / 2;
    final dy = (editorSize.height - renderSize.height) / 2;

    final relativeX = ((touch.dx - dx) / renderSize.width).clamp(0.0, 1.0);
    final relativeY = ((touch.dy - dy) / renderSize.height).clamp(0.0, 1.0);

    final cropX = relativeX * image.width;
    final cropY = relativeY * image.height;

    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        border: Border.all(color: Colors.white, width: 4),
        boxShadow: const [
          BoxShadow(color: Colors.black26, blurRadius: 8),
        ],
      ),
      clipBehavior: Clip.antiAlias,
      child: Stack(
        children: [
          Positioned(
            left: -(cropX * zoom) + size / 2,
            top: -(cropY * zoom) + size / 2,
            child: Image.memory(
              sourceBytes,
              width: image.width * zoom,
              height: image.height * zoom,
              fit: BoxFit.fill,
            ),
          ),
          const Center(
            child: Icon(Icons.add, color: Colors.red, size: 22),
          ),
        ],
      ),
    );
  }
}