//
//  CustomerMapAnnotation.swift
//  OnlineTaksiSwift
//
//  Created by Alperen Saraç on 24.04.2026.
//

import Foundation
import MapKit

final class CustomerMapAnnotation: NSObject, MKAnnotation {
    enum AnnotationType {
        case pickup
        case dropoff
        case driver
    }

    let type: AnnotationType
    dynamic var coordinate: CLLocationCoordinate2D
    var title: String?
    var subtitle: String?

    init(
        type: AnnotationType,
        coordinate: CLLocationCoordinate2D,
        title: String?,
        subtitle: String?
    ) {
        self.type = type
        self.coordinate = coordinate
        self.title = title
        self.subtitle = subtitle
    }
}
