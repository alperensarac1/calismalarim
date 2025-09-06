//
//  Extension.swift
//  MemeShareSwiftUI
//
//  Created by Alperen Saraç on 2.09.2025.
//

import Foundation


extension URL: Identifiable {
    public var id: String { absoluteString }
}

extension APIService {
    /// Test amaçlı ayrıntılı loglama yapan getter
    func debugGetJoinedRooms(userId: Int) async {
        let items = [URLQueryItem(name: "user_id", value: "\(userId)")]
        guard let url = makeURL("rooms-get-joined.php", query: items) else {
            print("❌ invalid URL")
            return
        }

        print("➡️ GET \(url.absoluteString)  (userId=\(userId))")

        do {
            let (data, resp) = try await session.data(from: url)
            let http = resp as? HTTPURLResponse
            let status = http?.statusCode ?? -1
            let ct = http?.value(forHTTPHeaderField: "Content-Type") ?? "nil"
            let bodyString = String(data: data, encoding: .utf8) ?? "<non-utf8>"

            print("🛰️ STATUS:", status, "CT:", ct)
            print("🛰️ BODY:", bodyString)

            // 1) Generic JSON olarak parse et (alan adları/net yapı için)
            do {
                let any = try JSONSerialization.jsonObject(with: data, options: [])
                if let arr = any as? [[String: Any]] {
                    print("🧪 JSON array count:", arr.count)
                    if let first = arr.first {
                        print("🧩 first item keys:", Array(first.keys))
                        print("🧩 first item:", first)
                    }
                } else {
                    print("🧪 JSON is not [[String: Any]]")
                }
            } catch {
                print("❌ JSONSerialization error:", error.localizedDescription)
            }

            // 2) Model decode testi
            do {
                let rooms = try decode([OdaModel].self, from: data, status: status, response: resp)
                print("✅ Decoded OdaModel count:", rooms.count)
                if let first = rooms.first {
                    print("✅ First room -> odaId:\(first.odaId) code:\(first.roomCode) by:\(first.createdBy)")
                }
            } catch {
                print("❌ Decoding to [OdaModel] failed:", error.localizedDescription)
            }

        } catch {
            print("❌ Request error:", error.localizedDescription)
        }
    }
}
