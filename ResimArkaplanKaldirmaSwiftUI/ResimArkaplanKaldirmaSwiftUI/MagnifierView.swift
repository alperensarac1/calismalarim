//
//  MagnifierView.swift
//  ResimArkaplanKaldirmaSwiftUI
//
//  Created by Alperen Saraç on 30.03.2026.
//
import SwiftUI
import Foundation
struct MagnifierView: View {
    let image: UIImage
    let touchLocation: CGPoint
    let editorSize: CGSize
    let imageSize: CGSize
    let displayedWidth: CGFloat
    let displayedHeight: CGFloat
    let imageOriginX: CGFloat
    let imageOriginY: CGFloat
    
    private let magnifierSize: CGFloat = 180
    private let zoom: CGFloat = 2.5
    
    var body: some View {
        let localX = touchLocation.x - imageOriginX
        let localY = touchLocation.y - imageOriginY
        
        let relativeX = localX / displayedWidth
        let relativeY = localY / displayedHeight
        
        let cropX = relativeX * imageSize.width
        let cropY = relativeY * imageSize.height
        
        ZStack {
            Circle()
                .fill(Color.white)
                .frame(width: magnifierSize, height: magnifierSize)
            
            Image(uiImage: image)
                .resizable()
                .frame(
                    width: imageSize.width * zoom,
                    height: imageSize.height * zoom
                )
                .offset(
                    x: -(cropX * zoom) + magnifierSize / 2,
                    y: -(cropY * zoom) + magnifierSize / 2
                )
                .clipShape(Circle())
            
            Circle()
                .stroke(Color.black.opacity(0.25), lineWidth: 10)
                .frame(width: magnifierSize, height: magnifierSize)
            
            Circle()
                .stroke(Color.white, lineWidth: 4)
                .frame(width: magnifierSize, height: magnifierSize)
            
            Rectangle()
                .fill(Color.red)
                .frame(width: 30, height: 2)
            
            Rectangle()
                .fill(Color.red)
                .frame(width: 2, height: 30)
        }
        .position(x: editorSize.width - magnifierSize / 2 - 20,
                  y: magnifierSize / 2 + 20)
    }
}
