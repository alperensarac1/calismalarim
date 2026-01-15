import 'package:flutter/material.dart';

void showSnack(BuildContext context, String msg) {
  ScaffoldMessenger.of(context).hideCurrentSnackBar();
  ScaffoldMessenger.of(context).showSnackBar(
    SnackBar(content: Text(msg)),
  );
}
