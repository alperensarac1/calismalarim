import Foundation
import UIKit

struct Device {
    static var id: String {
        UIDevice.current.identifierForVendor?.uuidString ?? "unknown"
    }
    static var info: String {
        let dev = UIDevice.current
        return "\(dev.systemName) \(dev.systemVersion) / \(dev.model)"
    }
}
