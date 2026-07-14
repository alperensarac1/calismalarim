//
//  ApiClient.swift
//  EBiletSwift
//
//  Created by Alperen Saraç on 25.06.2026.
//

import Foundation

/*
    APIClient

    URLSession ile PHP backend'e POST isteği atar.

    Android'de Retrofit kullanmıştık.
    Swift tarafında şimdilik native URLSession kullanıyoruz.

    Avantaj:
    - Ek kütüphane gerektirmez.
    - Storyboard projesi daha sade olur.
*/
final class APIClient {

    static let shared = APIClient()

    /*
        iOS Simulator için:
        localhost bilgisayarındaki Apache sunucusunu temsil eder.

        Gerçek iPhone için:
        http://192.168.1.35/event_ticket_api/
        gibi bilgisayar IP adresi kullanılmalıdır.
    */
    static let baseURL = "https://alperensaracdeneme.com/event_ticket_api/"

    private init() {}

    /*
        Genel POST isteği.

        T:
        Backend'den dönmesini beklediğimiz data modelidir.

        endpoint:
        "auth/login.php" gibi endpoint path.

        parameters:
        POST form alanları.
    */
    func post<T: Decodable>(
        endpoint: String,
        parameters: [String: String],
        completion: @escaping (Result<APIResponse<T>, Error>) -> Void
    ) {
        guard let url = URL(string: APIClient.baseURL + endpoint) else {
            completion(.failure(APIError.invalidURL))
            return
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"

        /*
            PHP tarafı $_POST beklediği için
            application/x-www-form-urlencoded formatında gönderiyoruz.
        */
        request.setValue(
            "application/x-www-form-urlencoded",
            forHTTPHeaderField: "Content-Type"
        )

        /*
            ["email": "a@test.com", "password": "123456"]
            değerini:
            email=a%40test.com&password=123456
            formatına çeviriyoruz.
        */
        let bodyString = parameters
            .map { key, value in
                let escapedKey = key.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? key
                let escapedValue = value.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? value
                return "\(escapedKey)=\(escapedValue)"
            }
            .joined(separator: "&")

        request.httpBody = bodyString.data(using: .utf8)

        /*
            URLSession dataTask asenkron çalışır.
            Cevap geldiğinde completion döner.
        */
        URLSession.shared.dataTask(with: request) { data, response, error in

            if let error = error {
                DispatchQueue.main.async {
                    completion(.failure(error))
                }
                return
            }

            guard let httpResponse = response as? HTTPURLResponse else {
                DispatchQueue.main.async {
                    completion(.failure(APIError.invalidResponse))
                }
                return
            }

            guard (200...299).contains(httpResponse.statusCode) else {
                DispatchQueue.main.async {
                    completion(.failure(APIError.httpError(code: httpResponse.statusCode)))
                }
                return
            }

            guard let data = data else {
                DispatchQueue.main.async {
                    completion(.failure(APIError.emptyData))
                }
                return
            }

            do {
                let decoder = JSONDecoder()

                /*
                    Model dosyalarında CodingKeys kullandığımız için
                    burada ayrıca keyDecodingStrategy ayarlamıyoruz.
                */
                let decoded = try decoder.decode(APIResponse<T>.self, from: data)

                DispatchQueue.main.async {
                    completion(.success(decoded))
                }

            } catch {
                /*
                    Decode hatası olduğunda debug için ham JSON'u görmek faydalı olur.
                */
                if let rawJson = String(data: data, encoding: .utf8) {
                    print("JSON Decode Error Raw Response:")
                    print(rawJson)
                }

                DispatchQueue.main.async {
                    completion(.failure(error))
                }
            }

        }.resume()
    }
}

/*
    API hata tipleri.
*/
enum APIError: LocalizedError {

    case invalidURL
    case invalidResponse
    case httpError(code: Int)
    case emptyData

    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "Geçersiz API adresi"
        case .invalidResponse:
            return "Geçersiz sunucu cevabı"
        case .httpError(let code):
            return "HTTP sunucu hatası: \(code)"
        case .emptyData:
            return "Sunucudan boş veri döndü"
        }
    }
}
