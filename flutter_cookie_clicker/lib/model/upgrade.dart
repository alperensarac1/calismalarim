import 'dart:math';

class Upgrade {
  final int id;
  final String title;
  final String desc;
  final String icon;
  final double basePrice;
  final double cpsGain;
  final int tapGain;
  final double priceMultiplier;
  final int level;

  const Upgrade({
    required this.id,
    required this.title,
    required this.desc,
    required this.icon,
    required this.basePrice,
    this.cpsGain = 0.0,
    this.tapGain = 0,
    this.priceMultiplier = 1.15,
    this.level = 0,
  });

  double currentPrice() => basePrice * pow(priceMultiplier, level);

  Upgrade copyWith({
    int? level,
  }) {
    return Upgrade(
      id: id,
      title: title,
      desc: desc,
      icon: icon,
      basePrice: basePrice,
      cpsGain: cpsGain,
      tapGain: tapGain,
      priceMultiplier: priceMultiplier,
      level: level ?? this.level,
    );
  }
}


