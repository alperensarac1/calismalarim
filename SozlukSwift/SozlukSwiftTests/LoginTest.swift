//
//  LoginTest.swift
//  SozlukSwiftTests
//
//  Created by Alperen Saraç on 10.08.2025.
//

import Foundation

import XCTest

final class LoginTests: XCTestCase {
    func testAPI() async throws {
        await getRaw(endpoint: "sozluk_entry_list.php", label: "Entry Listesi")
        // diğer test çağrılarını buraya ekle
    }
}

let BASE_URL = "https://alperensaracdeneme.com/sozluk/"

// MARK: - Helpers
func postJSON(endpoint: String, params: [String: Any], label: String) async {
    guard let url = URL(string: BASE_URL + endpoint) else { print("❌ \(label): URL"); return }
    var req = URLRequest(url: url, timeoutInterval: 20)
    req.httpMethod = "POST"
    req.setValue("application/json", forHTTPHeaderField: "Content-Type")
    req.httpBody = try? JSONSerialization.data(withJSONObject: params)
    do {
        let (data, resp) = try await URLSession.shared.data(for: req)
        if let http = resp as? HTTPURLResponse { print("📡 \(label) [JSON] HTTP \(http.statusCode)") }
        print("➡️ \(label) [JSON] Yanıt:\n", String(data: data, encoding: .utf8) ?? "—")
    } catch { print("❌ \(label) [JSON]: \(error.localizedDescription)") }
}

func postForm(endpoint: String, params: [String: String], label: String) async {
    guard let url = URL(string: BASE_URL + endpoint) else { print("❌ \(label): URL"); return }
    var req = URLRequest(url: url, timeoutInterval: 20)
    req.httpMethod = "POST"
    req.setValue("application/x-www-form-urlencoded; charset=utf-8", forHTTPHeaderField: "Content-Type")
    let body = params.map { "\($0.key)=\($0.value.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? "")" }
                     .joined(separator: "&")
    req.httpBody = body.data(using: .utf8)
    do {
        let (data, resp) = try await URLSession.shared.data(for: req)
        if let http = resp as? HTTPURLResponse { print("📡 \(label) [FORM] HTTP \(http.statusCode)") }
        print("➡️ \(label) [FORM] Yanıt:\n", String(data: data, encoding: .utf8) ?? "—")
    } catch { print("❌ \(label) [FORM]: \(error.localizedDescription)") }
}

func getRaw(endpoint: String, label: String) async {
    guard let url = URL(string: BASE_URL + endpoint) else { print("❌ \(label): URL"); return }
    var req = URLRequest(url: url, timeoutInterval: 20)
    req.httpMethod = "GET"
    do {
        let (data, resp) = try await URLSession.shared.data(for: req)
        if let http = resp as? HTTPURLResponse { print("📡 \(label) HTTP \(http.statusCode)") }
        print("➡️ \(label) Yanıt:\n", String(data: data, encoding: .utf8) ?? "—")
    } catch { print("❌ \(label): \(error.localizedDescription)") }
}

// MARK: - Tests
@main
struct Runner {
    static func main() async {
        print("🔎 API Smoke Tests\n")

        // 1) Entry list & tekil entry (zaten çalışıyordu)
        await getRaw(endpoint: "sozluk_entry_list.php", label: "Entry Listesi")
        await getRaw(endpoint: "sozluk_entry_get.php?entry_id=1", label: "Tekil Entry (1)")

        // 2) Register (JSON & FORM)
        let uname = "tester\(Int(Date().timeIntervalSince1970))"
        let pwd   = "123456"
        let mail  = "\(uname)@mail.com"
        await postJSON(endpoint: "sozluk_register.php",
                       params: ["username": uname, "password": pwd, "email": mail],
                       label: "Register")
        await postForm(endpoint: "sozluk_register.php",
                       params: ["username": uname, "password": pwd, "email": mail],
                       label: "Register")

        // 3) Login (JSON & FORM) — yeni oluşturulan kullanıcıyla
        await postJSON(endpoint: "sozluk_login.php",
                       params: ["username": uname, "password": pwd],
                       label: "Login")
        await postForm(endpoint: "sozluk_login.php",
                       params: ["username": uname, "password": pwd],
                       label: "Login")

        // 4) Entry ekle (JSON) — backend kullanıcı doğrulaması gerektirebilir
        // user_id = 1/gerçek id gerekiyor olabilir; login yanıtında id dönmüyorsa backend’e ekleyelim.
        await postJSON(endpoint: "sozluk_entry_insert.php",
                       params: ["user_id": "1", "title": "Swift Smoke", "content": "Command Line test"],
                       label: "Entry Ekle")

        // 5) Yorum ekle (JSON) — entry_id’yi bildiğin bir kayıtla değiştir
        await postJSON(endpoint: "sozluk_comment_insert.php",
                       params: ["entry_id": "1", "user_id": "1", "comment_text": "Merhaba Swift!"],
                       label: "Yorum Ekle")

        print("\n✅ Bitti")
    }
}
