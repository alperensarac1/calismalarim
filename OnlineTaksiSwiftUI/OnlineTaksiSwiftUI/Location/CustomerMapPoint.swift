//
//  CustomerMapPoint.swift
//  OnlineTaksiSwiftUI
//
//  Created by Alperen Saraç on 23.04.2026.
//

import Foundation
import CoreLocation

struct CustomerMapPoint: Identifiable, Equatable {
    let id: String
    let title: String
    let subtitle: String?
    let latitude: Double
    let longitude: Double
    let type: PointType

    enum PointType {
        case pickup
        case dropoff
        case driver
    }

    var coordinate: CLLocationCoordinate2D {
        CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
    }
}
