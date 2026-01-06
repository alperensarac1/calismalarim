import 'package:flutter/material.dart';

class SearchField extends StatelessWidget {
  final String value;
  final ValueChanged<String> onChanged;

  const SearchField({
    super.key,
    required this.value,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    return TextField(
      controller: TextEditingController(text: value)
        ..selection = TextSelection.collapsed(offset: value.length),
      onChanged: onChanged,
      decoration: const InputDecoration(
        labelText: 'Ara',
        border: OutlineInputBorder(),
      ),
    );
  }
}
