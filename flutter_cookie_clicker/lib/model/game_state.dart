class GameState {
  final double score;
  final double cps;
  final int baseTap;
  final int extraTap;
  final int prestigeLevel;

  const GameState({
    this.score = 0.0,
    this.cps = 0.0,
    this.baseTap = 1,
    this.extraTap = 0,
    this.prestigeLevel = 0,
  });

  GameState copyWith({
    double? score,
    double? cps,
    int? baseTap,
    int? extraTap,
    int? prestigeLevel,
  }) {
    return GameState(
      score: score ?? this.score,
      cps: cps ?? this.cps,
      baseTap: baseTap ?? this.baseTap,
      extraTap: extraTap ?? this.extraTap,
      prestigeLevel: prestigeLevel ?? this.prestigeLevel,
    );
  }

  factory GameState.fromJson(Map<String, dynamic> json) => GameState(
    score: (json['score'] ?? 0.0).toDouble(),
    cps: (json['cps'] ?? 0.0).toDouble(),
    baseTap: json['baseTap'] ?? 1,
    extraTap: json['extraTap'] ?? 0,
    prestigeLevel: json['prestigeLevel'] ?? 0,
  );

  Map<String, dynamic> toJson() => {
    'score': score,
    'cps': cps,
    'baseTap': baseTap,
    'extraTap': extraTap,
    'prestigeLevel': prestigeLevel,
  };
}
