import Foundation

final class SozlukDao {
    private let base = "https://alperensaracdeneme.com/sozluk/"
    static let shared = SozlukDao()
    private init() {}

    func register(username: String, password: String, email: String,
                  completion: @escaping (Result<SimpleResponse, Error>) -> Void) {
        SozlukApiService.shared.registerUser(
            username: username,
            password: password,
            email: email,
            completion: completion
        )
    }

    func login(username: String, password: String,
               completion: @escaping (Result<LoginResponse, Error>) -> Void) {
        SozlukApiService.shared.loginUser(
            username: username,
            password: password,
            completion: completion
        )
    }

    func addEntry(userId: Int, title: String, content: String,
                  completion: @escaping (Result<SimpleResponse, Error>) -> Void) {
        SozlukApiService.shared.addEntry(
            userId: userId,
            title: title,
            content: content,
            completion: completion
        )
    }

    func getAllEntries(completion: @escaping (Result<[Entry], Error>) -> Void) {

        struct EntryListRaw: Decodable {
            let id: String            // "2" gibi gelebiliyor
            let title: String
            let content: String
            let created_at: String
            let username: String?     // "1" ya da gerçek isim
        }

        guard let url = URL(string: "\(base)sozluk_entry_list.php") else {
            completion(.failure(NSError(domain: "bad_url", code: -1)))
            return
        }

        var req = URLRequest(url: url, timeoutInterval: 20)
        req.httpMethod = "GET"

        URLSession.shared.dataTask(with: req) { data, _, err in
            if let err = err {
                completion(.failure(err))
                return
            }
            guard let data = data else {
                completion(.failure(NSError(domain: "no_data", code: -1)))
                return
            }
            do {
                let raw = try JSONDecoder().decode([EntryListRaw].self, from: data)

                let list: [Entry] = raw.map { r in
                    let cleanedUsername: String? = {
                        if let u = r.username, Int(u) == nil { return u }
                        return nil
                    }()

                    return Entry(
                        id: Int(r.id) ?? 0,
                        user_id: nil,
                        username: cleanedUsername,
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

    func getEntriesByUser(userId: Int,
                          completion: @escaping (Result<[Entry], Error>) -> Void) {
        SozlukApiService.shared.getEntriesByUser(
            userId: userId,
            completion: completion
        )
    }

    func addComment(entryId: Int, userId: Int, commentText: String,
                    completion: @escaping (Result<SimpleResponse, Error>) -> Void) {
        SozlukApiService.shared.addComment(
            entryId: entryId,
            userId: userId,
            text: commentText,
            completion: completion
        )
    }

    func getCommentsByEntry(entryId: Int,
                            completion: @escaping (Result<[Comment], Error>) -> Void) {
        SozlukApiService.shared.getCommentsByEntry(
            entryId,
            completion: completion
        )
    }

    func voteComment(commentId: Int, userId: Int, isLike: Bool,
                     completion: @escaping (Result<SimpleResponse, Error>) -> Void) {
        SozlukApiService.shared.vote(
            commentId: commentId,
            userId: userId,
            isLike: isLike,
            completion: completion
        )
    }


    func deleteEntry(entryId: Int,
                     completion: @escaping (Result<SimpleResponse, Error>) -> Void) {
        SozlukApiService.shared.deleteEntry(
            entryId: entryId,
            completion: completion
        )
    }

    func getEntryById(entryId: Int,
                      completion: @escaping (Result<Entry?, Error>) -> Void) {
        SozlukApiService.shared.getEntryById(
            entryId,
            completion: completion
        )
    }
}
