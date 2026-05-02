import SwiftUI
import MapKit

struct CustomerMapView: UIViewRepresentable {
    let points: [CustomerMapPoint]

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    func makeUIView(context: Context) -> MKMapView {
        let mapView = MKMapView(frame: .zero)
        mapView.delegate = context.coordinator
        mapView.showsCompass = true
        mapView.showsScale = false
        mapView.pointOfInterestFilter = .includingAll

        // Varsayılan İstanbul konumu
        let defaultCenter = CLLocationCoordinate2D(latitude: 41.0082, longitude: 28.9784)
        let defaultRegion = MKCoordinateRegion(
            center: defaultCenter,
            span: MKCoordinateSpan(latitudeDelta: 0.2, longitudeDelta: 0.2)
        )
        mapView.setRegion(defaultRegion, animated: false)

        return mapView
    }

    func updateUIView(_ mapView: MKMapView, context: Context) {
        // Önce eski annotation'ları temizle
        mapView.removeAnnotations(mapView.annotations)

        let annotations = points.map { point -> CustomerAnnotation in
            let annotation = CustomerAnnotation()
            annotation.point = point
            annotation.coordinate = point.coordinate
            annotation.title = point.title
            annotation.subtitle = point.subtitle
            return annotation
        }

        mapView.addAnnotations(annotations)
        updateCamera(on: mapView, with: points)
    }

    private func updateCamera(on mapView: MKMapView, with points: [CustomerMapPoint]) {
        guard !points.isEmpty else {
            let defaultCenter = CLLocationCoordinate2D(latitude: 41.0082, longitude: 28.9784)
            let defaultRegion = MKCoordinateRegion(
                center: defaultCenter,
                span: MKCoordinateSpan(latitudeDelta: 0.2, longitudeDelta: 0.2)
            )
            mapView.setRegion(defaultRegion, animated: true)
            return
        }

        if points.count == 1, let first = points.first {
            let region = MKCoordinateRegion(
                center: first.coordinate,
                span: MKCoordinateSpan(latitudeDelta: 0.02, longitudeDelta: 0.02)
            )
            mapView.setRegion(region, animated: true)
            return
        }

        var rect = MKMapRect.null

        for point in points {
            let mapPoint = MKMapPoint(point.coordinate)
            let pointRect = MKMapRect(
                x: mapPoint.x,
                y: mapPoint.y,
                width: 0.1,
                height: 0.1
            )
            rect = rect.union(pointRect)
        }

        let padding = UIEdgeInsets(top: 70, left: 50, bottom: 70, right: 50)
        mapView.setVisibleMapRect(rect, edgePadding: padding, animated: true)
    }

    final class Coordinator: NSObject, MKMapViewDelegate {
        func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
            guard let annotation = annotation as? CustomerAnnotation else {
                return nil
            }

            let identifier = "CustomerMapAnnotation"
            var view = mapView.dequeueReusableAnnotationView(withIdentifier: identifier)

            if view == nil {
                view = MKMarkerAnnotationView(annotation: annotation, reuseIdentifier: identifier)
            } else {
                view?.annotation = annotation
            }

            if let markerView = view as? MKMarkerAnnotationView {
                markerView.canShowCallout = true
                markerView.titleVisibility = .visible
                markerView.subtitleVisibility = .visible

                switch annotation.point?.type {
                case .pickup:
                    markerView.markerTintColor = .systemGreen
                    markerView.glyphImage = UIImage(systemName: "mappin.circle.fill")
                case .dropoff:
                    markerView.markerTintColor = .systemRed
                    markerView.glyphImage = UIImage(systemName: "flag.circle.fill")
                case .driver:
                    markerView.markerTintColor = .systemOrange
                    markerView.glyphImage = UIImage(systemName: "car.fill")
                case .none:
                    markerView.markerTintColor = .systemBlue
                    markerView.glyphImage = UIImage(systemName: "circle.fill")
                }
            }

            return view
        }
    }
}

final class CustomerAnnotation: NSObject, MKAnnotation {
    var point: CustomerMapPoint?
    dynamic var coordinate: CLLocationCoordinate2D = .init()
    var title: String?
    var subtitle: String?
}
