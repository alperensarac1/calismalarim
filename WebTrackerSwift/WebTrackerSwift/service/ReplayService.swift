//
//  ReplayService.swift
//  WebTrackerSwift
//
//  Created by Alperen Saraç on 22.03.2026.
//

import Foundation

enum ReplayService {
    static func replay(
        originalLog: NetworkLog,
        editedBaseUrl: String,
        editedParams: String,
        completion: @escaping (String) -> Void
    ) {
        if editedBaseUrl.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            completion("Base URL boş olamaz")
            return
        }

        DispatchQueue.global(qos: .userInitiated).async {
            do {
                let request: URLRequest

                if originalLog.method.caseInsensitiveCompare("GET") == .orderedSame {
                    let finalUrlString = RequestUtils.buildFinalUrl(baseUrl: editedBaseUrl, query: editedParams)

                    guard let url = URL(string: finalUrlString) else {
                        DispatchQueue.main.async {
                            completion("Geçersiz URL")
                        }
                        return
                    }

                    var req = URLRequest(url: url)
                    req.httpMethod = "GET"
                    request = req
                } else {
                    guard let url = URL(string: editedBaseUrl) else {
                        DispatchQueue.main.async {
                            completion("Geçersiz URL")
                        }
                        return
                    }

                    var req = URLRequest(url: url)
                    req.httpMethod = "POST"
                    req.setValue(RequestUtils.detectContentType(for: editedParams), forHTTPHeaderField: "Content-Type")
                    req.httpBody = editedParams.data(using: .utf8)
                    request = req
                }

                let semaphore = DispatchSemaphore(value: 0)
                var resultText = ""

                URLSession.shared.dataTask(with: request) { data, response, error in
                    defer { semaphore.signal() }

                    if let error {
                        resultText = error.localizedDescription
                        return
                    }

                    let httpResponse = response as? HTTPURLResponse
                    let responseCode = httpResponse?.statusCode ?? -1
                    let headerText = httpResponse?.allHeaderFields.map { "\($0.key): \($0.value)" }.joined(separator: "\n") ?? "Header yok"
                    let bodyText = String(data: data ?? Data(), encoding: .utf8) ?? "Body okunamadı"

                    resultText = """
                    HTTP CODE: \(responseCode)

                    REQUEST:
                    \(buildReplayRequestSummary(originalLog: originalLog, editedBaseUrl: editedBaseUrl, editedParams: editedParams))

                    RESPONSE HEADERS:
                    \(headerText)

                    RESPONSE BODY:
                    \(String(bodyText.prefix(5000)))
                    """
                }.resume()

                semaphore.wait()

                DispatchQueue.main.async {
                    completion(resultText)
                }

            } catch {
                DispatchQueue.main.async {
                    completion(error.localizedDescription)
                }
            }
        }
    }

    private static func buildReplayRequestSummary(
        originalLog: NetworkLog,
        editedBaseUrl: String,
        editedParams: String
    ) -> String {
        """
        ORIGINAL METHOD: \(originalLog.method)
        SOURCE         : \(originalLog.source)
        EDITED BASE URL: \(editedBaseUrl)
        EDITED PARAMS  :
        \(editedParams.isEmpty ? "yok" : editedParams)
        """
    }
}
