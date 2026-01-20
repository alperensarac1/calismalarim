class ApiResponse<T> {
  final bool ok;
  final T? data;
  final String? error;

  ApiResponse({required this.ok, required this.data, required this.error});

  factory ApiResponse.fromJson(
      Map<String, dynamic> j,
      T Function(dynamic json) fromData,
      ) {
    return ApiResponse(
      ok: j["ok"] == true || j["success"] == true,
      data: j["data"] == null ? null : fromData(j["data"]),
      error: j["error"]?.toString() ?? j["message"]?.toString(),
    );
  }
}
