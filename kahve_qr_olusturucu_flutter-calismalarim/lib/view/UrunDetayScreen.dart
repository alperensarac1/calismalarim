import 'package:flutter/material.dart';
import 'package:cached_network_image/cached_network_image.dart';
import '../model/urun.dart';
import '../utils/Constants.dart';

class UrunDetayScreen extends StatelessWidget {
  final Urun urun;
  final VoidCallback onBackClick;

  const UrunDetayScreen({
    Key? key,
    required this.urun,
    required this.onBackClick,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(backgroundColor: kbackgroundColor,
      appBar: AppBar(
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: onBackClick,
        ),
        title: Text(urun.urunAd),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: ListView(
          children: [
            // Ürün Görseli
            ClipRRect(
              borderRadius: BorderRadius.circular(16),
              child: CachedNetworkImage(
                imageUrl: urun.urunResim,
                height: 220,
                width: double.infinity,
                fit: BoxFit.cover,
                placeholder: (context, url) => const CircularProgressIndicator(),
                errorWidget: (context, url, error) => Image.asset('images/mocha.jpg'),
              ),
            ),
            const SizedBox(height: 16),
            // Ürün Adı
            Text(
              urun.urunAd,
              style: Theme.of(context).textTheme.headlineSmall,
            ),
            const SizedBox(height: 8),
            // Ürün Açıklaması
            Text(
              urun.urunAciklama,
              style: Theme.of(context).textTheme.bodyMedium,
            ),
            const SizedBox(height: 16),
            // Fiyat Bilgisi
            if (urun.urunIndirim == 1) ...[
              Text(
                "${urun.urunFiyat.toInt()} TL",
                style: const TextStyle(
                  decoration: TextDecoration.lineThrough,
                  fontSize: 16,
                  color: Colors.grey,
                ),
              ),
              Text(
                "${urun.urunIndirimliFiyat.toInt()} TL",
                style: const TextStyle(
                  fontSize: 20,
                  color: Colors.red,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ] else ...[
              Text(
                "${urun.urunFiyat.toInt()} TL",
                style: const TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ]
          ],
        ),
      ),
    );
  }
}
