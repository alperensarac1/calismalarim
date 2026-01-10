// widget/haber_card.dart
import 'package:flutter/material.dart';
import '../model/haber_model.dart';
import 'haber_media.dart';

class HaberCard extends StatelessWidget {
  final HaberModel haber;
  final VoidCallback onTap;

  const HaberCard({
    super.key,
    required this.haber,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 300,
      height: 250,
      child: InkWell(
        onTap: onTap,
        child: Card(
          elevation: 4,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          clipBehavior: Clip.antiAlias,
          child: Column(
            children: [
              Expanded(
                child: HaberMedia(
                  mediaType: haber.media_type,
                  url: haber.media_url,
                  height: double.infinity,
                ),
              ),
              Padding(
                padding: const EdgeInsets.all(8),
                child: Row(
                  children: [
                    Expanded(
                      child: Text(
                        haber.baslik,
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                      ),
                    ),
                    const SizedBox(width: 8),
                    const Text(
                      "Devamını Oku→",
                      style: TextStyle(color: Colors.grey, fontSize: 12),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
