//
//  SozlukApiService.swift
//  SozlukSwift
//
//  Created by Alperen Saraç on 9.08.2025.
//

import Foundation
class ApiUtils {
    static let baseURL = "https://alperensaracdeneme.com/sozluk/"
}

final class SozlukApiService {
    static let shared = SozlukApiService()
    private init() {}
    private let base = "https://alperensaracdeneme.com/sozluk/"

    private func postJSON<T: Decodable>(_ endpoint: String, _ body: [String: Any],
                                        completion: @escaping (Result<T, Error>) -> Void) {
        guard let url = URL(string: base + endpoint) else { return }
        var req = URLRequest(url: url, timeoutInterval: 20)
        req.httpMethod = "POST"
        req.addValue("application/json", forHTTPHeaderField: "Content-Type")
        req.httpBody = try? JSONSerialization.data(withJSONObject: body)
        URLSession.shared.dataTask(with: req) { data, _, err in
            if let err = err { completion(.failure(err)); return }
            guard let data = data else { completion(.failure(NSError(domain: "no_data", code: -1))); return }
            do {
                let decoded = try JSONDecoder().decode(T.self, from: data)
                completion(.success(decoded))
            } catch {
                completion(.failure(error))
            }
        }.resume()
    }

    private func getJSON<T: Decodable>(_ endpoint: String,
                                       completion: @escaping (Result<T, Error>) -> Void) {
        guard let url = URL(string: base + endpoint) else { return }
        var req = URLRequest(url: url, timeoutInterval: 20)
        req.httpMethod = "GET"
        URLSession.shared.dataTask(with: req) { data, _, err in
            if let err = err { completion(.failure(err)); return }
            guard let data = data else { completion(.failure(NSError(domain: "no_data", code: -1))); return }
            do {
                let decoded = try JSONDecoder().decode(T.self, from: data)
                completion(.success(decoded))
            } catch {
                completion(.failure(error))
            }
        }.resume()
    }

    // --- API methods ---

    func loginUser(username: String, password: String,
                   completion: @escaping (Result<LoginResponse, Error>) -> Void) {
        postJSON("sozluk_login.php", ["username": username, "password": password], completion: completion)
    }

    func registerUser(username: String, password: String, email: String,
                      completion: @escaping (Result<SimpleResponse, Error>) -> Void) {
        postJSON("sozluk_register.php", ["username": username, "password": password, "email": email], completion: completion)
    }

    func getAllEntries(completion: @escaping (Result<[Entry], Error>) -> Void) {
        getJSON("sozluk_entry_list.php", completion: completion)
    }

    func getEntryById(_ entryId: Int, completion: @escaping (Result<Entry?, Error>) -> Void) {
        getJSON("sozluk_entry_get.php?entry_id=\(entryId)", completion: completion)
    }

    func addEntry(userId: Int, title: String, content: String,
                  completion: @escaping (Result<SimpleResponse, Error>) -> Void) {
        postJSON("sozluk_entry_insert.php", ["user_id": "\(userId)", "title": title, "content": content], completion: completion)
    }

    func addComment(entryId: Int, userId: Int, text: String,
                    completion: @escaping (Result<SimpleResponse, Error>) -> Void) {
        postJSON("sozluk_comment_insert.php", ["entry_id": "\(entryId)", "user_id": "\(userId)", "comment_text": text], completion: completion)
    }

    func getCommentsByEntry(_ entryId: Int,
                            completion: @escaping (Result<[Comment], Error>) -> Void) {

        struct IntOrString: Decodable {
            let value: Int
            init(from decoder: Decoder) throws {
                let c = try decoder.singleValueContainer()
                if let i = try? c.decode(Int.self) { value = i; return }
                if let s = try? c.decode(String.self), let i = Int(s) { value = i; return }
                value = 0
            }
        }

        // ✅ Backend'in şu an verdiği MINIMAL şema
        struct CommentRawMinimal: Decodable {
            let id: IntOrString
            let comment_text: String?
            let created_at: String?
            let username: String?
            let likes: IntOrString?
            let dislikes: IntOrString?
            // entry_id / user_id gelmiyor, opsiyonel bıraktık
        }

        // Opsiyonel: Hata objesi
        struct APIErrorObj: Decodable { let success: Bool?; let message: String? }

        guard let url = URL(string: "\(base)sozluk_comments_by_entry.php?entry_id=\(entryId)") else {
            completion(.failure(NSError(domain: "bad_url", code: -1))); return
        }

        URLSession.shared.dataTask(with: url) { data, _, err in
            if let err = err { completion(.failure(err)); return }
            guard let data = data else {
                completion(.failure(NSError(domain: "no_data", code: -1))); return
            }

            #if DEBUG
            if let body = String(data: data, encoding: .utf8) {
                print("🔎 getCommentsByEntry raw:\n\(body)")
            }
            #endif

            // 1) Dizi olarak decode et
            if let rawList = try? JSONDecoder().decode([CommentRawMinimal].self, from: data) {
                let list: [Comment] = rawList.map { r in
                    // username sayısal görünüyorsa isim yok say
                    let cleanedUsername: String = {
                        if let u = r.username, Int(u) == nil { return u }
                        return "kullanıcı"
                    }()

                    return Comment(
                        id: r.id.value,
                        entry_id: entryId,                 // gelmiyor → paramdan doldur
                        user_id: 0,                        // gelmiyor → 0
                        username: cleanedUsername,
                        comment_text: r.comment_text ?? "",
                        likes: r.likes?.value ?? 0,
                        dislikes: r.dislikes?.value ?? 0,
                        created_at: r.created_at ?? ""
                    )
                }
                completion(.success(list))
                return
            }

            // 2) Hata objesi olabilir
            if let errObj = try? JSONDecoder().decode(APIErrorObj.self, from: data) {
                let msg = errObj.message ?? "Yorumlar yüklenemedi"
                completion(.failure(NSError(domain: "api_error", code: -2,
                                            userInfo: [NSLocalizedDescriptionKey: msg])))
                return
            }

            completion(.failure(NSError(domain: "decode_error", code: -3,
                                        userInfo: [NSLocalizedDescriptionKey: "Beklenmeyen yanıt formatı"])))
        }.resume()
    }




    func vote(commentId: Int, userId: Int, isLike: Bool,
              completion: @escaping (Result<SimpleResponse, Error>) -> Void) {
        postJSON("sozluk_like_comment.php", ["comment_id": commentId, "user_id": userId, "is_like": isLike ? 1 : 0], completion: completion)
    }

    func deleteEntry(entryId: Int, completion: @escaping (Result<SimpleResponse, Error>) -> Void) {
        postJSON("sozluk_entry_delete.php", ["entry_id": entryId], completion: completion)
    }
    func getEntriesByUser(userId: Int,
                              completion: @escaping (Result<[Entry], Error>) -> Void) {

            struct EntryByUserRaw: Decodable {
                let id: String   // bazen "2" string gelebiliyor
                let title: String
                let content: String
                let created_at: String
            }

            guard let url = URL(string: "\(base)sozluk_entry_by_user.php?user_id=\(userId)") else {
                completion(.failure(NSError(domain: "bad_url", code: -1))); return
            }

            var req = URLRequest(url: url, timeoutInterval: 20)
            req.httpMethod = "GET"

            URLSession.shared.dataTask(with: req) { data, _, err in
                if let err = err { completion(.failure(err)); return }
                guard let data = data else {
                    completion(.failure(NSError(domain: "no_data", code: -1))); return
                }
                do {
                    let raw = try JSONDecoder().decode([EntryByUserRaw].self, from: data)
                    let list: [Entry] = raw.map { r in
                        Entry(
                            id: Int(r.id) ?? 0,
                            user_id: userId,          // endpoint zaten kullanıcıya göre filtreli
                            username: nil,            // backend vermiyor; istersen PHP'de JOIN ile ekleyebilirsin
                            title: r.title,
                            content: r.content,
                            created_at: r.created_at
                        )
                    }
                    completion(.success(list))
                } catch {
                    completion(.failure(error))
                }
            }.resume()
        }
}
