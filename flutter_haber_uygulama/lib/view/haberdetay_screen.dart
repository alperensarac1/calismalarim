// view/haber_detay_screen.dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../components/haber_media.dart';
import '../model/haber_model.dart';
import '../viewmodel/haberdetay_viewmodel.dart';
import '../viewmodel/haberler_viewmodel.dart';


class HaberDetayScreen extends StatefulWidget {
  final HaberModel haber;

  const HaberDetayScreen({super.key, required this.haber});

  @override
  State<HaberDetayScreen> createState() => _HaberDetayScreenState();
}

class _HaberDetayScreenState extends State<HaberDetayScreen> {
  final _rumuzCtrl = TextEditingController();
  final _yorumCtrl = TextEditingController();
  bool _loaded = false;

  @override
  void dispose() {
    _rumuzCtrl.dispose();
    _yorumCtrl.dispose();
    super.dispose();
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (!_loaded) {
      _loaded = true;
      context.read<HaberDetayViewModel>().loadYorumlar(widget.haber.id);
      context.read<HaberlerViewModel>().loadSon3Haber();
    }
  }

  @override
  Widget build(BuildContext context) {
    final yorumVm = context.watch<HaberDetayViewModel>();
    final haberVm = context.watch<HaberlerViewModel>();

    return Scaffold(
      appBar: AppBar(title: const Text("Detay")),
      body: ListView(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Text(
              widget.haber.baslik,
              style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Color(0xFF707070)),
            ),
          ),

          HaberMedia(
            mediaType: widget.haber.media_type,
            url: widget.haber.media_url,
            height: 200,
          ),

          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: Text("${widget.haber.ad} ${widget.haber.soyad} - ${widget.haber.unvan}",
                style: const TextStyle(fontSize: 16)),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Text(widget.haber.yayinlanma_tarihi, style: const TextStyle(fontSize: 16)),
          ),

          Padding(
            padding: const EdgeInsets.all(16),
            child: Text(
              widget.haber.icerik,
              style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
            ),
          ),

          const SizedBox(height: 24),
          const Padding(
            padding: EdgeInsets.all(16),
            child: Text(
              "Son Haberler",
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Color(0xFF707070)),
            ),
          ),

          SizedBox(
            height: 220,
            child: ListView.separated(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              scrollDirection: Axis.horizontal,
              itemCount: haberVm.sonHaberler.length,
              separatorBuilder: (_, __) => const SizedBox(width: 8),
              itemBuilder: (context, i) {
                final item = haberVm.sonHaberler[i];
                return SizedBox(
                  width: 260,
                  child: InkWell(
                    onTap: () {
                      Navigator.pushReplacement(
                        context,
                        MaterialPageRoute(builder: (_) => HaberDetayScreen(haber: item)),
                      );
                    },
                    child: Card(
                      child: Padding(
                        padding: const EdgeInsets.all(12),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(item.baslik,
                                maxLines: 2,
                                overflow: TextOverflow.ellipsis,
                                style: const TextStyle(fontWeight: FontWeight.bold)),
                            const SizedBox(height: 8),
                            ClipRRect(
                              borderRadius: BorderRadius.circular(8),
                              child: HaberMedia(
                                mediaType: 'image',
                                url: item.media_url,
                                height: 120,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                );
              },
            ),
          ),

          const SizedBox(height: 24),
          const Padding(
            padding: EdgeInsets.symmetric(horizontal: 16),
            child: Text(
              "Yorum Yaz",
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Color(0xFF6640A3)),
            ),
          ),

          Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              children: [
                TextField(
                  controller: _rumuzCtrl,
                  decoration: const InputDecoration(labelText: "Rumuz", border: OutlineInputBorder()),
                ),
                const SizedBox(height: 8),
                TextField(
                  controller: _yorumCtrl,
                  decoration: const InputDecoration(labelText: "Yorumunuz", border: OutlineInputBorder()),
                  minLines: 2,
                  maxLines: 5,
                ),
                const SizedBox(height: 10),
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    onPressed: () async {
                      final rumuz = _rumuzCtrl.text.trim();
                      final yorum = _yorumCtrl.text.trim();

                      if (rumuz.isEmpty || yorum.isEmpty) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(content: Text("İlgili alanlar boş bırakılamaz")),
                        );
                        return;
                      }

                      await yorumVm.yorumEkle(widget.haber.id, rumuz, yorum);
                      _rumuzCtrl.clear();
                      _yorumCtrl.clear();
                    },
                    child: const Text("GÖNDER"),
                  ),
                ),
                if (yorumVm.error != null) ...[
                  const SizedBox(height: 8),
                  Text(yorumVm.error!, style: const TextStyle(color: Colors.red)),
                ],
              ],
            ),
          ),

          const SizedBox(height: 24),
          const Padding(
            padding: EdgeInsets.all(16),
            child: Text(
              "Yorumlar",
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Color(0xFF6640A3)),
            ),
          ),

          if (yorumVm.loading && yorumVm.yorumlar.isEmpty)
            const Padding(
              padding: EdgeInsets.all(16),
              child: Center(child: CircularProgressIndicator()),
            ),

          ...yorumVm.yorumlar.map(
                (y) => Card(
              margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(y.yorum_metni),
                    const SizedBox(height: 6),
                    Text("— ${y.takma_ad}", style: const TextStyle(fontStyle: FontStyle.italic)),
                  ],
                ),
              ),
            ),
          ),
          const SizedBox(height: 24),
        ],
      ),
    );
  }
}
