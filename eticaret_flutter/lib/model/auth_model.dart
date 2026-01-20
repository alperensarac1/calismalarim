class LoginResponse {
  final String token;
  LoginResponse({required this.token});

  factory LoginResponse.fromJson(Map<String, dynamic> j) {
    return LoginResponse(token: j["token"] ?? "");
  }
}
