class FloatingText {
  final int id;
  final String text;
  final double x;
  final double y;
  final bool isCrit;

  const FloatingText({
    required this.id,
    required this.text,
    required this.x,
    required this.y,
    this.isCrit = false,
  });
}