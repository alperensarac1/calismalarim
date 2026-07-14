/// Kullanıcı modelidir.
///
/// Backend alanları:
/// id
/// full_name
/// email
/// phone
/// role
/// api_token
/// created_at
class UserModel {
  final int id;
  final String fullName;
  final String email;
  final String? phone;
  final String role;
  final String? apiToken;
  final String? createdAt;

  UserModel({
    required this.id,
    required this.fullName,
    required this.email,
    this.phone,
    required this.role,
    this.apiToken,
    this.createdAt,
  });

  factory UserModel.fromJson(Map<String, dynamic> json) {
    return UserModel(
      id: int.tryParse(json['id'].toString()) ?? 0,
      fullName: json['full_name']?.toString() ?? '',
      email: json['email']?.toString() ?? '',
      phone: json['phone']?.toString(),
      role: json['role']?.toString() ?? 'user',
      apiToken: json['api_token']?.toString(),
      createdAt: json['created_at']?.toString(),
    );
  }
}