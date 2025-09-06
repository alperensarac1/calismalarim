//
//  Toast.swift
//  MemeShareSwiftUI
//
//  Created by Alperen Saraç on 2.09.2025.
//

import Foundation
import SwiftUI

struct Toast: Identifiable,Equatable {
    let id = UUID()
    let message: String
}

struct ToastView: View {
    let message: String
    var body: some View {
        Text(message)
            .padding(.horizontal, 16).padding(.vertical, 10)
            .background(.black.opacity(0.85))
            .foregroundStyle(.white)
            .clipShape(Capsule())
            .shadow(radius: 8)
            .transition(.move(edge: .top).combined(with: .opacity))
    }
}

extension View {
    func toast(_ toast: Binding<Toast?>) -> some View {
        ZStack(alignment: .top) {
            self
            if let t = toast.wrappedValue {
                ToastView(message: t.message)
                    .padding(.top, 12)
                    .onAppear {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 1.2) {
                            withAnimation { toast.wrappedValue = nil }
                        }
                    }
            }
        }
        .animation(.spring(response: 0.35, dampingFraction: 0.85), value: toast.wrappedValue)
    }
}
