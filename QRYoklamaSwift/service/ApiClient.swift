import Foundation
struct ApiClient {
    static let base = "https://alperensaracdeneme.com"
    static let markURL = URL(string: base + "/qryoklama/api/index.php?p=attendance/mark")!

    static func postJSON(_ url: URL, body: [String:Any], completion: @escaping (Result<Data,Error>) -> Void) {
        var req = URLRequest(url: url)
        req.httpMethod = "POST"
        req.setValue("application/json; charset=utf-8", forHTTPHeaderField: "Content-Type")
        req.httpBody = try? JSONSerialization.data(withJSONObject: body, options: [])
        let task = URLSession.shared.dataTask(with: req) { data, resp, err in
            if let err = err { completion(.failure(err)); return }
            guard let http = resp as? HTTPURLResponse, let data = data else {
                completion(.failure(NSError(domain: "net", code: -1)))
                return
            }
            if (200..<300).contains(http.statusCode) {
                completion(.success(data))
            } else {
                completion(.failure(NSError(domain: "http", code: http.statusCode, userInfo: ["data": data])))
            }
        }
        task.resume()
    }
}
