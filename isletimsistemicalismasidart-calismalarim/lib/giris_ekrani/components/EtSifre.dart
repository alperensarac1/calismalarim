import 'package:flutter/material.dart';

class EtSifre extends StatefulWidget {
  late TextEditingController controller;
  late bool parolaGoster;
  late String hint = "";


  EtSifre({required this.controller,required this.parolaGoster,required this.hint});

  @override
  _EtSifreState createState() => _EtSifreState();
}

class _EtSifreState extends State<EtSifre> {
  bool parolaGoster = false;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 50.0),
      child: TextField(
        controller: widget.controller,
        obscureText: widget.parolaGoster,
        decoration: InputDecoration(
          hintText: widget.hint,
          suffixIcon: IconButton(
            icon: Icon(parolaGoster ? Icons.lock_open : Icons.lock),
            onPressed: () {
              setState(() {
                parolaGoster = !parolaGoster;
              });
            },
          ),
        ),
        keyboardType: TextInputType.visiblePassword,
      ),
    );
  }
}
