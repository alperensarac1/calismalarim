class OdaModel {
  final int odaId;
  final String roomCode;
  final int createdBy;

  OdaModel({
    required this.odaId,
    required this.roomCode,
    required this.createdBy,
  });

  factory OdaModel.fromJson(Map<String, dynamic> json) {
    return OdaModel(
      odaId: json['room_id'],
      roomCode: json['room_code'],
      createdBy: json['created_by'],
    );
  }
}
