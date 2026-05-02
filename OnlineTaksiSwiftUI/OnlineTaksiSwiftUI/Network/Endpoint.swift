//
//  Endpoints.swift
//  OnlineTaksiSwiftUI
//
//  Created by Alperen Saraç on 23.04.2026.
//

import Foundation

enum Endpoint {
    case register
    case login
    case createRide
    case driverLocation
    case driverOnlineStatus
    case availableRides
    case acceptRide(Int)
    case activeRides
    case updateRideStatus(Int)

    var path: String {
        switch self {
        case .register:
            return "auth/register"
        case .login:
            return "auth/login"
        case .createRide:
            return "customer/rides"
        case .driverLocation:
            return "driver/location"
        case .driverOnlineStatus:
            return "driver/online-status"
        case .availableRides:
            return "driver/available-rides"
        case .acceptRide(let rideId):
            return "driver/rides/\(rideId)/accept"
        case .activeRides:
            return "driver/rides/my-active"
        case .updateRideStatus(let rideId):
            return "driver/rides/\(rideId)/status"
        }
    }
}
