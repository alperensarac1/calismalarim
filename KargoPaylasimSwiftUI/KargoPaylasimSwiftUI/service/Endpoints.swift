//
//  Endpoints.swift
//  KargoPaylasimSwiftUI
//
//  Created by Alperen Saraç on 30.01.2026.
//

import Foundation

enum Endpoint {
    static let baseURL = URL(string: "https://alperensaracdeneme.com/cargo/")!

    case login
    case register
    case me

    case addressList
    case addressDetail(id: Int)
    case addressCreate
    case addressUpdate
    case addressDelete
    case addressSetDefault

    case receiverLookup

    case shipmentList(type: String)
    case shipmentDetail(id: Int)
    case shipmentCreate
    case shipmentDelete
    case shipmentCancel
    case shipmentRegenerateCode

    var url: URL {
        switch self {
        case .login: return Endpoint.baseURL.appendingPathComponent("user_login.php")
        case .register: return Endpoint.baseURL.appendingPathComponent("user_register.php")
        case .me: return Endpoint.baseURL.appendingPathComponent("user_me.php")

        case .addressList: return Endpoint.baseURL.appendingPathComponent("address_list.php")
        case .addressDetail: return Endpoint.baseURL.appendingPathComponent("address_detail.php")
        case .addressCreate: return Endpoint.baseURL.appendingPathComponent("address_create.php")
        case .addressUpdate: return Endpoint.baseURL.appendingPathComponent("address_update.php")
        case .addressDelete: return Endpoint.baseURL.appendingPathComponent("address_delete.php")
        case .addressSetDefault: return Endpoint.baseURL.appendingPathComponent("address_set_default.php")

        case .receiverLookup: return Endpoint.baseURL.appendingPathComponent("receiver_lookup.php")

        case .shipmentList: return Endpoint.baseURL.appendingPathComponent("shipment_list.php")
        case .shipmentDetail: return Endpoint.baseURL.appendingPathComponent("shipment_detail.php")
        case .shipmentCreate: return Endpoint.baseURL.appendingPathComponent("shipment_create.php")
        case .shipmentDelete: return Endpoint.baseURL.appendingPathComponent("shipment_delete.php")
        case .shipmentCancel: return Endpoint.baseURL.appendingPathComponent("shipment_cancel.php")
        case .shipmentRegenerateCode: return Endpoint.baseURL.appendingPathComponent("shipment_regenerate_code.php")
        }
    }
}

