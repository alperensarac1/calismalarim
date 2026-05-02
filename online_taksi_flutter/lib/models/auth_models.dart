class LoginRequest {
  final String phone;
  final String password;

  LoginRequest({
    required this.phone,
    required this.password,
  });

  Map<String, dynamic> toJson() => {
    "phone": phone,
    "password": password,
  };
}

class RegisterRequest {
  final String fullName;
  final String phone;
  final String? email;
  final String password;
  final String role;

  RegisterRequest({
    required this.fullName,
    required this.phone,
    required this.email,
    required this.password,
    required this.role,
  });

  Map<String, dynamic> toJson() => {
    "full_name": fullName,
    "phone": phone,
    "email": email,
    "password": password,
    "role": role,
  };
}

class AuthResponse {
  final String accessToken;
  final String tokenType;
  final int userId;
  final String fullName;
  final String role;

  AuthResponse({
    required this.accessToken,
    required this.tokenType,
    required this.userId,
    required this.fullName,
    required this.role,
  });

  factory AuthResponse.fromJson(Map<String, dynamic> json) {
    return AuthResponse(
      accessToken: json["access_token"],
      tokenType: json["token_type"],
      userId: json["user_id"],
      fullName: json["full_name"],
      role: json["role"],
    );
  }
}
