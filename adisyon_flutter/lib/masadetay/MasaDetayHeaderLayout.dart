import 'package:flutter/material.dart';
import '../model/Masa.dart';

class MasaDetayHeader extends StatefulWidget {
  final Masa masa;
  final VoidCallback onBackPressed;

  const MasaDetayHeader({
    super.key,
    required this.masa,
    required this.onBackPressed,
  });

  @override
  State<MasaDetayHeader> createState() => _MasaDetayHeaderState();
}

class _MasaDetayHeaderState extends State<MasaDetayHeader> {
  late Masa _masa;

  @override
  void initState() {
    super.initState();
    _masa = widget.masa;
  }

  void guncelleMasa(Masa yeniMasa) {
    setState(() {
      _masa = yeniMasa;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 50),
      child: Row(
        children: [
          IconButton(
            onPressed: widget.onBackPressed,
            icon: const Icon(Icons.close), // ic_remove benzeri simge
          ),
          const SizedBox(width: 16),
          Text(
            'Masa ${_masa.id}',
            style: const TextStyle(fontSize: 20),
          ),
        ],
      ),
    );
  }
}
