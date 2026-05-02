//
//  Endpoint.swift
//  OnlineTaksiSwift
//
//  Created by Alperen Saraç on 24.04.2026.
//

import Foundation

enum Endpoint {
    case login
    case register
    case createRide
    case driverLocation
    case driverOnlineStatus
    case availableRides
    case acceptRide(Int)
    case activeRides
    case updateRideStatus(Int)

    var path: String {
        switch self {
        case .login:
            return "auth/login"
        case .register:
            return "auth/register"
        case .createRide:
            return "customer/rides"
        case .driverLocation:
            return "driver/location"
        case .driverOnlineStatus:
            return "driver/online-status"
        case .availableRides:
            return "driver/available-rides"
        case .acceptRide(let id):
            return "driver/rides/\(id)/accept"
        case .activeRides:
            return "driver/rides/my-active"
        case .updateRideStatus(let id):
            return "driver/rides/\(id)/status"
        }
    }
}
