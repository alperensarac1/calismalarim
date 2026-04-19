class Ship {
  final int size;
  final bool placed;

  Ship({
    required this.size,
    required this.placed,
  });

  Ship copyWith({
    int? size,
    bool? placed,
  }) {
    return Ship(
      size: size ?? this.size,
      placed: placed ?? this.placed,
    );
  }
}
