import Foundation
import UIKit

final class TestApi {

    private let baseURL = "https://alperensaracdeneme.com/sozluk/"
    /// İstersen log’u bir UITextView’e yazdırmak için ver:
    weak var logView: UITextView?

    // MARK: - Public tests
    func testLogin(username: String = "1", password: String = "1") {
        let params: [String: Any] = ["username": username, "password": password]
        postJSON(endpoint: "sozluk_login.php", params: params, label: "Login Testi")
    }

    func testEntryList() {
        getRaw(endpoint: "sozluk_entry_list.php", label: "Entry Listesi Testi")
    }

    func testSingleEntry(entryId: Int) {
        getRaw(endpoint: "sozluk_entry_get.php?entry_id=\(entryId)", label: "Tekil Entry Testi")
    }

    // MARK: - Helpers
    private func postJSON(endpoint: String, params: [String: Any], label: String) {
        guard let url = URL(string: baseURL + endpoint) else {
            log("❌ \(label): Geçersiz URL"); return
        }
        var req = URLRequest(url: url, timeoutInterval: 20)
        req.httpMethod = "POST"
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        req.httpBody = try? JSONSerialization.data(withJSONObject: params)

        log("\n🔑 \(label) → \(endpoint)")
        URLSession.shared.dataTask(with: req) { [weak self] data, resp, err in
            if let err = err { self?.log("❌ \(label): \(err.localizedDescription)"); return }
            if let http = resp as? HTTPURLResponse { self?.log("📡 HTTP \(http.statusCode)") }
            guard let data = data else { self?.log("⚠️ Veri yok"); return }
            self?.log("➡️ Yanıt:\n\(String(data: data, encoding: .utf8) ?? "—")")
        }.resume()
    }

    private func getRaw(endpoint: String, label: String) {
        guard let url = URL(string: baseURL + endpoint) else {
            log("❌ \(label): Geçersiz URL"); return
        }
        var req = URLRequest(url: url, timeoutInterval: 20)
        req.httpMethod = "GET"

        log("\n🔍 \(label) → \(endpoint)")
        URLSession.shared.dataTask(with: req) { [weak self] data, resp, err in
            if let err = err { self?.log("❌ \(label): \(err.localizedDescription)"); return }
            if let http = resp as? HTTPURLResponse { self?.log("📡 HTTP \(http.statusCode)") }
            guard let data = data else { self?.log("⚠️ Veri yok"); return }
            self?.log("➡️ Yanıt:\n\(String(data: data, encoding: .utf8) ?? "—")")
        }.resume()
    }

    private func log(_ s: String) {
        print(s)
        DispatchQueue.main.async { [weak self] in
            guard let tv = self?.logView else { return }
            tv.text.append(s + "\n")
            let bottom = NSRange(location: max(0, tv.text.count - 1), length: 1)
            tv.scrollRangeToVisible(bottom)
        }
    }
}
