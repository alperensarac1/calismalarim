import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../model/Masa.dart';
import '../viewmodel/MasaDetayViewModel.dart';
import '../viewmodel/UrunViewModel.dart';
import 'MasaAdisyonLayout.dart';
import 'MasaDetayHeaderLayout.dart';
import 'UrunlerLayout.dart';

class MasaDetayLayout extends StatefulWidget {
  final Masa masa;
  final MasaDetayViewModel viewModel;
  final UrunViewModel urunVM;
  final VoidCallback onBackPressed;

  const MasaDetayLayout({
    super.key,
    required this.masa,
    required this.viewModel,
    required this.urunVM,
    required this.onBackPressed,
  });

  @override
  State<MasaDetayLayout> createState() => _MasaDetayLayoutState();
}

class _MasaDetayLayoutState extends State<MasaDetayLayout> {
  late Masa _masa;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      widget.urunVM.hazirla();  // bu olmadan circular hep döner
    });
    _masa = widget.masa;
  }

  void guncelleMasa(Masa yeniMasa) {
    setState(() {
      _masa = yeniMasa;
    });
  }

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider.value(
      value: widget.viewModel,
      child: Container(
        color: const Color(0xFFFFBABA),
        child: Column(
          children: [
            // HEADER
            SizedBox(
              height: 60,
              child: MasaDetayHeader(
                masa: _masa,
                onBackPressed: widget.onBackPressed,
              ),
            ),

            // BODY (Adisyon + Ürünler)
            Expanded(
              child: Row(
                children: [
                  SizedBox(
                    width: 200,
                    child: MasaAdisyonLayout(
                      masa: _masa,
                    ),
                  ),
                  Expanded(
                    child: Consumer<UrunViewModel>(
                      builder: (context, urunVM, _) {
                        final urunler = urunVM.urunler;
                        final kategoriler = urunVM.kategoriler;

                        if (urunler.isEmpty || kategoriler.isEmpty) {
                          return const Center(child: CircularProgressIndicator());
                        }

                        return UrunlerLayout(
                          tumUrunler: urunler,
                          kategoriler: kategoriler,
                          onUrunSec: (urun) {
                            widget.viewModel.urunEkle(urun.id);
                          },
                        );
                      },
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
