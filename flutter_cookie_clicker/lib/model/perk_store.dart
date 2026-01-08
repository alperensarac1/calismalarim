class PerkStore {
  final int points;
  final int gprod;
  final int crit;
  final int discount;
  final int tapTop;

  const PerkStore({
    this.points = 0,
    this.gprod = 0,
    this.crit = 0,
    this.discount = 0,
    this.tapTop = 0,
  });

  PerkStore copyWith({
    int? points,
    int? gprod,
    int? crit,
    int? discount,
    int? tapTop,
  }) {
    return PerkStore(
      points: points ?? this.points,
      gprod: gprod ?? this.gprod,
      crit: crit ?? this.crit,
      discount: discount ?? this.discount,
      tapTop: tapTop ?? this.tapTop,
    );
  }

  factory PerkStore.fromJson(Map<String, dynamic> json) => PerkStore(
    points: json['points'] ?? 0,
    gprod: json['gprod'] ?? 0,
    crit: json['crit'] ?? 0,
    discount: json['discount'] ?? 0,
    tapTop: json['tapTop'] ?? 0,
  );

  Map<String, dynamic> toJson() => {
    'points': points,
    'gprod': gprod,
    'crit': crit,
    'discount': discount,
    'tapTop': tapTop,
  };
}
