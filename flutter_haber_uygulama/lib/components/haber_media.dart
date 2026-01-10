// widget/haber_media.dart
import 'package:flutter/material.dart';
import 'package:video_player/video_player.dart';

class HaberMedia extends StatefulWidget {
  final String mediaType; // "video" veya "image"
  final String url;
  final double height;
  final BoxFit fit;

  const HaberMedia({
    super.key,
    required this.mediaType,
    required this.url,
    required this.height,
    this.fit = BoxFit.cover,
  });

  @override
  State<HaberMedia> createState() => _HaberMediaState();
}

class _HaberMediaState extends State<HaberMedia> {
  VideoPlayerController? _controller;

  @override
  void initState() {
    super.initState();
    if (widget.mediaType == 'video') {
      _controller = VideoPlayerController.networkUrl(Uri.parse(widget.url))
        ..initialize().then((_) {
          if (!mounted) return;
          setState(() {});
          _controller?.setLooping(true);
          _controller?.play();
        });
    }
  }

  @override
  void didUpdateWidget(covariant HaberMedia oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.url != widget.url || oldWidget.mediaType != widget.mediaType) {
      _controller?.dispose();
      _controller = null;

      if (widget.mediaType == 'video') {
        _controller = VideoPlayerController.networkUrl(Uri.parse(widget.url))
          ..initialize().then((_) {
            if (!mounted) return;
            setState(() {});
            _controller?.setLooping(true);
            _controller?.play();
          });
      }
    }
  }

  @override
  void dispose() {
    _controller?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (widget.mediaType == 'video') {
      final c = _controller;
      if (c == null || !c.value.isInitialized) {
        return SizedBox(
          height: widget.height,
          child: const Center(child: CircularProgressIndicator()),
        );
      }
      return SizedBox(
        height: widget.height,
        width: double.infinity,
        child: FittedBox(
          fit: BoxFit.cover,
          clipBehavior: Clip.hardEdge,
          child: SizedBox(
            width: c.value.size.width,
            height: c.value.size.height,
            child: VideoPlayer(c),
          ),
        ),
      );
    }

    return Image.network(
      widget.url,
      height: widget.height,
      width: double.infinity,
      fit: widget.fit,
      errorBuilder: (_, __, ___) => SizedBox(
        height: widget.height,
        child: const Center(child: Icon(Icons.broken_image)),
      ),
      loadingBuilder: (context, child, progress) {
        if (progress == null) return child;
        return SizedBox(
          height: widget.height,
          child: const Center(child: CircularProgressIndicator()),
        );
      },
    );
  }
}
