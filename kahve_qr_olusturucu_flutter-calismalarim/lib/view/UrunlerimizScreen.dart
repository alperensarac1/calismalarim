import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:kahve_qr_olusturucu/utils/Constants.dart';

import '../di/Providers.dart';
import '../model/urun.dart';
import 'UrunDetayScreen.dart';

class UrunlerimizScreen extends ConsumerWidget {
  const UrunlerimizScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(urunlerimizProvider);

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        UrunKategoriListesi(kategoriAdi: "Kampanyalar", urunler: state.kampanyalar),
        UrunKategoriListesi(kategoriAdi: "İçecekler", urunler: state.icecekler),
        UrunKategoriListesi(kategoriAdi: "Atıştırmalıklar", urunler: state.atistirmaliklar),
      ],
    );
  }
}

class UrunKategoriListesi extends StatelessWidget {
  final String kategoriAdi;
  final List<Urun> urunler;

  const UrunKategoriListesi({
    super.key,
    required this.kategoriAdi,
    required this.urunler,
  });

  @override
  Widget build(BuildContext context) {

    if (urunler.isEmpty) {
      return Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(kategoriAdi, style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 8),
          const SizedBox(
            height: 150,
            child: Center(child: CircularProgressIndicator()),
          ),
        ],
      );
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(kategoriAdi, style: Theme.of(context).textTheme.titleMedium),
        const SizedBox(height: 8),
        SizedBox(
          height: 250,
          child: ListView.builder(
            scrollDirection: Axis.horizontal,
            itemCount: urunler.length,
            itemBuilder: (context, index) {
              return UrunCard(urun: urunler[index]);
            },
          ),
        ),
      ],
    );
  }
}

class UrunCard extends StatelessWidget {
  final Urun urun;

  const UrunCard({super.key, required this.urun});

  @override
  Widget build(BuildContext context) {
    final bool indirimVar = urun.urunIndirim == 1;
    final double indirimYuzdesi = indirimVar
        ? ((urun.urunFiyat - urun.urunIndirimliFiyat) / urun.urunFiyat) * 100
        : 0;

    return GestureDetector(
      onTap: () {
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => UrunDetayScreen(
              urun: urun,
              onBackClick: () => Navigator.pop(context),
            ),
          ),
        );
      },
      child: Card(
        margin: const EdgeInsets.all(8),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        elevation: 4,
        child: SizedBox(
          width: 150,
          child: Column(
            children: [
              Stack(
                children: [
                  ClipRRect(
                    borderRadius: const BorderRadius.vertical(top: Radius.circular(12)),
                    child: CachedNetworkImage(
                      imageUrl: urun.urunResim,
                      height: 150,
                      width: double.infinity,
                      fit: BoxFit.cover,
                      placeholder: (_, __) => const Center(child: CircularProgressIndicator()),
                      errorWidget: (_, __, ___) => const Icon(Icons.broken_image),
                    ),
                  ),
                  if (indirimVar)
                    Positioned(
                      right: 4,
                      bottom: 4,
                      child: Container(
                        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                        color: Colors.red,
                        child: Text(
                          "%-${indirimYuzdesi.toStringAsFixed(1)}",
                          style: const TextStyle(color: Colors.white, fontSize: 12),
                        ),
                      ),
                    ),
                ],
              ),
              Padding(
                padding: const EdgeInsets.all(8),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(mainAxisAlignment: MainAxisAlignment.spaceBetween,children: [
                      Text(
                        urun.urunAd,
                        style: TextStyle(fontWeight: FontWeight.bold,color: kTextColor),
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                      ),
                      const SizedBox(height: 4),
                      if (indirimVar) ...[
                        Column(children: [
                          Text(
                            "${urun.urunFiyat.toInt()} TL",
                            style: const TextStyle(
                              decoration: TextDecoration.lineThrough,
                              fontSize: 12,
                              color: Colors.grey,
                            ),
                          ),
                          Text(
                            "${urun.urunIndirimliFiyat.toInt()} TL",
                            style: const TextStyle(color: Colors.red, fontWeight: FontWeight.bold),
                          ),
                        ],)
                      ] else
                        Text(
                          "${urun.urunFiyat.toInt()} TL",
                          style: TextStyle(fontWeight: FontWeight.bold,color: kTextColor),
                        ),
                    ],)

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