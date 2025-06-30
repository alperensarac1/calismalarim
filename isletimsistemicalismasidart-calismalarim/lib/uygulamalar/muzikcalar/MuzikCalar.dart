import 'package:flutter/material.dart';
import 'package:audioplayers/audioplayers.dart';
import 'package:permission_handler/permission_handler.dart';

class MuzikCalar extends StatefulWidget {
  @override
  _MuzikCalarState createState() => _MuzikCalarState();
}

class _MuzikCalarState extends State<MuzikCalar> {
  final AudioPlayer _audioPlayer = AudioPlayer();
  int _currentSongIndex = 0;
  bool _isPlaying = false;
  Duration _currentPosition = Duration.zero;
  Duration _songDuration = Duration.zero;

  final List<String> _songs = [
    'assets/music/vidya.mp3',
    'assets/music/shine.mp3',
    'assets/music/disfigure.mp3',
  ];
  final List<String> _songNames = ["Vidya", "Shine", "Disfigure"];

  @override
  void initState() {
    super.initState();
    _setupAudio();
  }

  Future<void> _setupAudio() async {
    _audioPlayer.onPositionChanged.listen((position) {
      setState(() {
        _currentPosition = position;
      });
    });

    _audioPlayer.onDurationChanged.listen((duration) {
      setState(() {
        _songDuration = duration;
      });
    });

    _audioPlayer.onPlayerComplete.listen((event) {
      _playNext();
    });

    await _checkPermission();
  }

  Future<void> _checkPermission() async {
    if (!await Permission.storage.isGranted) {
      await Permission.storage.request();
    }
  }


  Future<void> _startSong(int index) async {
    await _audioPlayer.stop();
    await _audioPlayer.play(AssetSource(_songs[index]));
    setState(() {
      _currentSongIndex = index;
      _isPlaying = true;
    });
  }

  Future<void> _togglePlayPause() async {
    if (_isPlaying) {
      await _audioPlayer.pause();
    } else {
      await _audioPlayer.resume();
    }
    setState(() {
      _isPlaying = !_isPlaying;
    });
  }

  Future<void> _playNext() async {
    int nextIndex = (_currentSongIndex + 1) % _songs.length;
    await _startSong(nextIndex);
  }

  Future<void> _playPrevious() async {
    int prevIndex = (_currentSongIndex - 1 + _songs.length) % _songs.length;
    await _startSong(prevIndex);
  }

  @override
  void dispose() {
    _audioPlayer.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text("Müzik Çalar")),
      body: Padding(
        padding: EdgeInsets.all(16.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              _songNames[_currentSongIndex],
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            ),
            SizedBox(height: 20),


            Slider(
              value: _currentPosition.inSeconds.toDouble(),
              max: _songDuration.inSeconds.toDouble(),
              onChanged: (value) async {
                await _audioPlayer.seek(Duration(seconds: value.toInt()));
                setState(() {
                  _currentPosition = Duration(seconds: value.toInt());
                });
              },
            ),

            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                IconButton(
                  icon: Icon(Icons.skip_previous, size: 40),
                  onPressed: _playPrevious,
                ),
                IconButton(
                  icon: Icon(_isPlaying ? Icons.pause : Icons.play_arrow, size: 50),
                  onPressed: _togglePlayPause,
                ),
                IconButton(
                  icon: Icon(Icons.skip_next, size: 40),
                  onPressed: _playNext,
                ),
              ],
            ),

            SizedBox(height: 20),

            Expanded(
              child: ListView.builder(
                itemCount: _songNames.length,
                itemBuilder: (context, index) {
                  return ListTile(
                    title: Text(_songNames[index]),
                    onTap: () => _startSong(index),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
