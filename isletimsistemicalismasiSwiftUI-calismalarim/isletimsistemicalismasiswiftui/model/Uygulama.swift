import Foundation
import UIKit

import SwiftUI

struct Uygulama: Identifiable {
    var id = UUID()
    var uygulamaAdi: String
    var uygulamaResmi: String
    var uygulamaGecisId: AnyView? // UIView yerine SwiftUI uyumlu hale getirildi
    var copKutusundaMi: Bool
}

