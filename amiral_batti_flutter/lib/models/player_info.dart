class PlayerInfo {
  final String id;
  final String name;
  final bool ready;

  PlayerInfo({
    required this.id,
    required this.name,
    required this.ready,
  });

  factory PlayerInfo.fromJson(Map<String, dynamic> json) {
    return PlayerInfo(
      id: json['id'] ?? '',
      name: json['name'] ?? '',
      ready: json['ready'] ?? false,
    );
  }
}
