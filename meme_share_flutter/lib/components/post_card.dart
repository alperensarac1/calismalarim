import 'package:flutter/material.dart';
import 'package:meme_share_flutter/components/video_post.dart';
import '../model/gonderi_model.dart';


class PostCard extends StatelessWidget {
  final GonderiModel item;
  final int currentUserId;

  const PostCard({
    super.key,
    required this.item,
    required this.currentUserId,
  });

  @override
  Widget build(BuildContext context) {
    const base = 'https://alperensaracdeneme.com/meme/';
    final fullUrl = base + item.mediaUrl;

    final alignEnd = item.userId == currentUserId;

    return Align(
      alignment: alignEnd ? Alignment.centerRight : Alignment.centerLeft,
      child: SizedBox(
        width: 240,
        child: Card(
          child: Padding(
            padding: const EdgeInsets.all(8),
            child: Column(
              children: [
                if (item.mediaType == 'image')
                  ClipRRect(
                    borderRadius: BorderRadius.circular(10),
                    child: Image.network(
                      fullUrl,
                      height: 200,
                      width: double.infinity,
                      fit: BoxFit.cover,
                      errorBuilder: (_, __, ___) => Container(
                        height: 200,
                        alignment: Alignment.center,
                        child: const Text('Resim yüklenemedi'),
                      ),
                    ),
                  )
                else if (item.mediaType == 'video')
                  VideoPost(url: fullUrl)
                else
                  Container(height: 200),

                const SizedBox(height: 8),
                Row(
                  children: [
                    Expanded(child: Text('Kullanıcı #${item.userId}')),
                    Text(
                      item.uploadedAt,
                      style: Theme.of(context).textTheme.bodySmall,
                    ),
                  ],
                )
              ],
            ),
          ),
        ),
      ),
    );
  }
}
