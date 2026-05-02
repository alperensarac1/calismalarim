//
//  SocketEventParser.swift
//  OnlineTaksiSwift
//
//  Created by Alperen Saraç on 24.04.2026.
//

import Foundation

enum SocketEventParser {

    static func getEventName(_ message: String) -> String? {
        guard let data = message.data(using: .utf8),
              let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            return nil
        }

        return json["event"] as? String
    }

    static func parseRideStatus(_ message: String) -> String? {
        guard let data = message.data(using: .utf8),
              let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let event = json["event"] as? String,
              ["RIDE_ACCEPTED", "RIDE_STATUS_CHANGED", "RIDE_CANCELLED"].contains(event),
              let body = json["data"] as? [String: Any],
              let status = body["status"] as? String else {
            return nil
        }

        return status
    }

    static func parseDriverLocation(_ message: String) -> DriverLocationEvent? {
        guard let data = message.data(using: .utf8),
              let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let event = json["event"] as? String,
              event == "DRIVER_LOCATION",
              let body = json["data"] as? [String: Any],
              let rideId = body["ride_id"] as? Int,
              let driverId = body["driver_id"] as? Int,
              let lat = body["lat"] as? Double,
              let lng = body["lng"] as? Double else {
            return nil
        }

        return DriverLocationEvent(
            rideId: rideId,
            driverId: driverId,
            lat: lat,
            lng: lng
        )
    }
}
