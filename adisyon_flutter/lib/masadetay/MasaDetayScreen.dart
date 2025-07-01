import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../model/Masa.dart';
import '../viewmodel/MasaDetayViewModel.dart';
import '../viewmodel/MasalarViewModel.dart';
import '../viewmodel/UrunViewModel.dart';
import 'MasaDetayLayout.dart';

class MasaDetayScreen extends StatefulWidget {
  final int masaId;

  const MasaDetayScreen({super.key, required this.masaId});

  @override
  State<MasaDetayScreen> createState() => _MasaDetayScreenState();
}

class _MasaDetayScreenState extends State<MasaDetayScreen> {
  late final MasaDetayViewModel viewModel;
  late final UrunViewModel urunViewModel;

  @override
  void initState() {
    super.initState();

    viewModel = MasaDetayViewModel(widget.masaId);
    urunViewModel = UrunViewModel();
    urunViewModel.kategorileriYukle();
    urunViewModel.urunleriYukle();
    viewModel.yukleTumVeriler();


    WidgetsBinding.instance.addPostFrameCallback((_) {
      viewModel.addListener(() {
        if (!mounted) return;
        if (viewModel.odemeTamamlandi) {
          final updatedMasa = viewModel.masa;
          if (updatedMasa != null) {
            final masalarVM = Provider.of<MasalarViewModel>(context, listen: false);
            masalarVM.guncelleMasa(updatedMasa);
          }

          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text("Ödeme tamamlandı")),
          );

          Navigator.of(context).pop();
        }
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    final Masa varsayilanMasa = Masa(
      id: widget.masaId,
      masaAdi: '',
      acikMi: 1,
      sure: 0,
      toplamFiyat: 0,
    );

    return Scaffold(
      body: MasaDetayLayout(
        masa: viewModel.masa ?? varsayilanMasa,
        viewModel: viewModel,
        urunVM: urunViewModel,
        onBackPressed: () {
          Navigator.of(context).pop();
        },
      ),
    );
  }
}
