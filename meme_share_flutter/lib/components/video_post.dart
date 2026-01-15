import 'package:flutter/material.dart';
import 'package:video_player/video_player.dart';

class VideoPost extends StatefulWidget {
  final String url;
  const VideoPost({super.key, required this.url});

  @override
  State<VideoPost> createState() => _VideoPostState();
}

class _VideoPostState extends State<VideoPost> {
  VideoPlayerController? _controller;
  bool playing = false;

  @override
  void dispose() {
    _controller?.dispose();
    super.dispose();
  }

  Future<void> _start() async {
    setState(() => playing = true);
    final c = VideoPlayerController.networkUrl(Uri.parse(widget.url));
    await c.initialize();
    await c.play();
    setState(() => _controller = c);

    c.addListener(() {
      if (!mounted) return;
      if (c.value.isInitialized && c.value.position >= c.value.duration && !c.value.isPlaying) {
        // bitti
        setState(() {
          playing = false;
          _controller?.dispose();
          _controller = null;
        });
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    if (!playing || _controller == null) {
      return Container(
        height: 200,
        alignment: Alignment.center,
        child: FilledButton(
          onPressed: _start,
          child: const Text('▶'),
        ),
      );
    }

    return ClipRRect(
      borderRadius: BorderRadius.circular(10),
      child: SizedBox(
        height: 200,
        width: double.infinity,
        child: FittedBox(
          fit: BoxFit.cover,
          child: SizedBox(
            width: _controller!.value.size.width,
            height: _controller!.value.size.height,
            child: VideoPlayer(_controller!),
          ),
        ),
      ),
    );
  }
}
