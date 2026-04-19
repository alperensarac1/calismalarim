//
//  ZoomableImageEditor.swift
//  ResimArkaplanKaldirmaSwiftUI
//
//  Created by Alperen Saraç on 30.03.2026.
//

import Foundation
import SwiftUI

struct ZoomableImageEditor: View {
    
    let image: UIImage?
    let onImageTap: (Int, Int) -> Void
    
    @State private var scale: CGFloat = 1
    @State private var lastScale: CGFloat = 1
    
    @State private var offset: CGSize = .zero
    @State private var lastOffset: CGSize = .zero
    
    @State private var magnifierVisible = false
    @State private var magnifierTouch: CGPoint = .zero
    
    var body: some View {
        GeometryReader { geo in
            ZStack {
                Color.gray.opacity(0.2)
                
                if let image {
                    let uiImage = image
                    let imageSize = uiImage.size
                    let fitScale = min(
                        geo.size.width / imageSize.width,
                        geo.size.height / imageSize.height
                    )
                    
                    let displayedWidth = imageSize.width * fitScale * scale
                    let displayedHeight = imageSize.height * fitScale * scale
                    
                    let imageOriginX = (geo.size.width - displayedWidth) / 2 + offset.width
                    let imageOriginY = (geo.size.height - displayedHeight) / 2 + offset.height
                    
                    Image(uiImage: uiImage)
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .scaleEffect(scale)
                        .offset(offset)
                        .gesture(
                            SimultaneousGesture(
                                MagnificationGesture()
                                    .onChanged { value in
                                        scale = lastScale * value
                                    }
                                    .onEnded { _ in
                                        lastScale = scale
                                    },
                                
                                DragGesture()
                                    .onChanged { value in
                                        offset = CGSize(
                                            width: lastOffset.width + value.translation.width,
                                            height: lastOffset.height + value.translation.height
                                        )
                                    }
                                    .onEnded { _ in
                                        lastOffset = offset
                                    }
                            )
                        )
                        .highPriorityGesture(
                            TapGesture()
                                .onEnded {
                                    // boş, aşağıdaki overlay dokunma alanı kullanılıyor
                                }
                        )
                    
                    Color.clear
                        .contentShape(Rectangle())
                        .gesture(
                            DragGesture(minimumDistance: 0)
                                .onChanged { value in
                                    magnifierTouch = value.location
                                }
                                .onEnded { value in
                                    let location = value.location
                                    
                                    if magnifierVisible == false {
                                        let mapped = mapTouchToImagePixel(
                                            location: location,
                                            imageSize: imageSize,
                                            displayedWidth: displayedWidth,
                                            displayedHeight: displayedHeight,
                                            imageOriginX: imageOriginX,
                                            imageOriginY: imageOriginY
                                        )
                                        
                                        if let mapped {
                                            onImageTap(mapped.x, mapped.y)
                                        }
                                    }
                                    
                                    magnifierVisible = false
                                }
                        )
                        .simultaneousGesture(
                            LongPressGesture(minimumDuration: 0.35)
                                .onEnded { _ in
                                    magnifierVisible = true
                                }
                        )
                    
                    if magnifierVisible {
                        MagnifierView(
                            image: uiImage,
                            touchLocation: magnifierTouch,
                            editorSize: geo.size,
                            imageSize: imageSize,
                            displayedWidth: displayedWidth,
                            displayedHeight: displayedHeight,
                            imageOriginX: imageOriginX,
                            imageOriginY: imageOriginY
                        )
                    }
                } else {
                    Text("Fotoğraf seçilmedi")
                        .foregroundColor(.secondary)
                }
            }
            .clipped()
        }
    }
    
    private func mapTouchToImagePixel(
        location: CGPoint,
        imageSize: CGSize,
        displayedWidth: CGFloat,
        displayedHeight: CGFloat,
        imageOriginX: CGFloat,
        imageOriginY: CGFloat
    ) -> (x: Int, y: Int)? {
        
        let localX = location.x - imageOriginX
        let localY = location.y - imageOriginY
        
        guard localX >= 0, localY >= 0,
              localX <= displayedWidth,
              localY <= displayedHeight else {
            return nil
        }
        
        let pixelX = Int((localX / displayedWidth) * imageSize.width)
        let pixelY = Int((localY / displayedHeight) * imageSize.height)
        
        return (pixelX, pixelY)
    }
}
