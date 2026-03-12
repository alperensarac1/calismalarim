//
//  Models.swift
//  YardimUygulamaSwiftUI
//
//  Created by Alperen Saraç on 28.02.2026.
//

import Foundation

enum Role: String, Codable {
    case HASTA
    case YARDIMCI
}

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

struct ApiOk<T: Codable>: Codable {
    let ok: Bool?
    let error: String?
    let user: User?
    let items: [T]?
    let active: T?
}

struct LoginBody: Codable {
    let telefon: String
    let sifre: String
}

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

struct HelpConfirmBody: Codable {
    let request_id: Int
    let patient_id: Int
}
struct HelpCancelBody: Codable {
    let request_id: Int
    let patient_id: Int
}
struct HelpAcceptBody: Codable {
    let request_id: Int
    let helper_id: Int
}

struct OpenHelpItem: Codable, Identifiable {
    let id: Int
    let patient_name: String?
    let patient_age: Int?
    let created_at: String?
}

struct HelpActive: Codable, Identifiable {
    let id: Int
    let status: String
    let remaining_seconds: Int?
}

struct AcceptedHelpItem: Codable, Identifiable {
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

struct ConfirmedHelpItem: Codable, Identifiable {
    let id: Int
    let patient_name: String?
    let patient_phone: String?
    let servis_adi: String?
    let oda_no: String?
    let confirmed_at: String?
}
