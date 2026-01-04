import Foundation

final class Prefs {
    private let noKey = "student_no"
    static let shared = Prefs()
    private init() {}
    func setStudentNo(_ no: String) { UserDefaults.standard.set(no, forKey: noKey) }
    func getStudentNo() -> String? { UserDefaults.standard.string(forKey: noKey) }
    func hasStudentNo() -> Bool { getStudentNo()?.isEmpty == false }
}
