class ApiResp<T> {
  final bool ok;
  final T? data;
  final String? error;

  ApiResp({required this.ok, this.data, this.error});

  factory ApiResp.fromJson(
      Map<String, dynamic> json,
      T Function(dynamic) parseData,
      ) {
    return ApiResp(
      ok: json["ok"] == true,
      data: json["data"] == null ? null : parseData(json["data"]),
      error: json["error"]?.toString(),
    );
  }
}
