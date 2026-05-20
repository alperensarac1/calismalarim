import 'package:flutter/material.dart';

class HomeScreen extends StatelessWidget {
  final VoidCallback onStartBroadcast;
  final VoidCallback onWatchBroadcasts;

  const HomeScreen({
    super.key,
    required this.onStartBroadcast,
    required this.onWatchBroadcasts,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Text(
                "Canlı Yayın Uygulaması",
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 26,
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF0F172A),
                ),
              ),

              const SizedBox(height: 32),

              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: onStartBroadcast,
                  child: const Text("Yayın Aç"),
                ),
              ),

              const SizedBox(height: 16),

              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: onWatchBroadcasts,
                  child: const Text("Yayınları İzle"),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}