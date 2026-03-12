//
//  models.swift
//  YardimUygulamaSwift
//
//  Created by Alperen Saraç on 1.03.2026.
//

import Foundation

enum Role: String, Codable { case HASTA, YARDIMCI }

struct User: Codable {
    let id: Int
    let role: Role
    let ad: String?
    let soyad: String?
    let yas: Int?
    let telefon: String?
    let il: String?
    let ilce: String?
}

struct HelpCreateResponse: Codable {
    let id: Int
    let status: String
    let ilce: String?
}

struct ApiOk<T: Codable>: Codable {
    let ok: Bool?
    let error: String?
    let user: User?
    let items: [T]?
    let active: T?
    let request: HelpCreateResponse?
}
struct LoginBody: Codable { let telefon: String; let sifre: String }

struct RegisterBody: Codable {
    let role: Role
    let ad: String
    let soyad: String
    let yas: Int?
    let telefon: String
    let il: String
    let ilce: String
    let sifre: String
}

struct HelpCreateBody: Codable {
    let patient_id: Int
    let servis_adi: String
    let oda_no: String
    let lat: Double
    let lng: Double
}
struct HelpConfirmBody: Codable { let request_id: Int; let patient_id: Int }
struct HelpCancelBody: Codable { let request_id: Int; let patient_id: Int }
struct HelpAcceptBody: Codable { let request_id: Int; let helper_id: Int }

struct OpenHelpItem: Codable {
    let id: Int
    let patient_name: String?
    let patient_age: Int?
    let created_at: String?
}

struct HelpActive: Codable {
    let id: Int
    let status: String
    let remaining_seconds: Int?
}

struct AcceptedHelpItem: Codable {
    let id: Int
    let patient_id: Int
    let patient_name: String?
    let patient_age: Int?
    let patient_phone: String?
    let servis_adi: String?
    let oda_no: String?
    let lat: Double
    let lng: Double
    let remaining_seconds: Int?
}

struct ConfirmedHelpItem: Codable {
    let id: Int
    let patient_name: String?
    let patient_phone: String?
    let servis_adi: String?
    let oda_no: String?
    let confirmed_at: String?
}

/// Empty generic payload helper
struct EmptyDTO: Codable {}
