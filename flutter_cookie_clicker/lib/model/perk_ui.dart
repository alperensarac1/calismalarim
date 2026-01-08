import 'dart:math';

class PerkUi {
  final String key;
  final String title;
  final String desc;
  final int baseCost;
  final double scaling;
  final int level;
  final int maxLevel;

  const PerkUi({
    required this.key,
    required this.title,
    required this.desc,
    required this.baseCost,
    required this.scaling,
    required this.level,
    this.maxLevel = 0x7fffffff, // Int.MAX_VALUE
  });

  int costForNext() {
    final v = (baseCost * pow(scaling, level)).toInt();
    return v < baseCost ? baseCost : v;
  }
}
