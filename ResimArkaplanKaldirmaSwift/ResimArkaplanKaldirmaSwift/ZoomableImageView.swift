//
//  ZoomableImageView.swift
//  ResimArkaplanKaldirmaSwift
//
//  Created by Alperen Saraç on 1.04.2026.
//

import Foundation
import UIKit

final class ZoomableImageView: UIView {

    var onImageTap: ((Int, Int) -> Void)?

    private let imageView = UIImageView()
    private let magnifierView = MagnifierOverlayView()

    private var currentImage: UIImage?

    private var scale: CGFloat = 1
    private var minScale: CGFloat = 1
    private var maxScale: CGFloat = 5

    private var translation = CGPoint.zero
    private var lastTranslation = CGPoint.zero

    override init(frame: CGRect) {
        super.init(frame: frame)
        commonInit()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        commonInit()
    }

    private func commonInit() {
        clipsToBounds = true

        imageView.contentMode = .scaleAspectFit
        imageView.isUserInteractionEnabled = true
        imageView.frame = bounds
        imageView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        addSubview(imageView)

        magnifierView.isHidden = true
        magnifierView.backgroundColor = .clear
        magnifierView.isUserInteractionEnabled = false
        addSubview(magnifierView)

        let tap = UITapGestureRecognizer(target: self, action: #selector(handleTap(_:)))
        let pinch = UIPinchGestureRecognizer(target: self, action: #selector(handlePinch(_:)))
        let pan = UIPanGestureRecognizer(target: self, action: #selector(handlePan(_:)))
        let longPress = UILongPressGestureRecognizer(target: self, action: #selector(handleLongPress(_:)))

        addGestureRecognizer(tap)
        addGestureRecognizer(pinch)
        addGestureRecognizer(pan)
        addGestureRecognizer(longPress)
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        imageView.frame = bounds
        magnifierView.frame = CGRect(x: bounds.width - 200, y: 20, width: 180, height: 180)

        if imageView.transform == .identity {
            resetZoom()
        }
    }

    func setImage(_ image: UIImage?) {
        currentImage = image
        imageView.image = image
        magnifierView.sourceImage = image
    }

    func resetZoom() {
        imageView.transform = .identity
        imageView.center = CGPoint(x: bounds.midX, y: bounds.midY)
        scale = 1
        translation = .zero
        lastTranslation = .zero
    }

    @objc private func handleTap(_ gesture: UITapGestureRecognizer) {
        let location = gesture.location(in: imageView)

        guard let image = currentImage,
              let point = mapViewPointToImagePixel(location, image: image) else { return }

        onImageTap?(point.x, point.y)
    }

    @objc private func handlePinch(_ gesture: UIPinchGestureRecognizer) {
        guard currentImage != nil else { return }

        switch gesture.state {
        case .began, .changed:
            let newScale = (scale * gesture.scale).clamped(to: minScale...maxScale)
            let delta = newScale / scale

            imageView.transform = imageView.transform.scaledBy(x: delta, y: delta)
            scale = newScale
            gesture.scale = 1

        default:
            break
        }
    }

    @objc private func handlePan(_ gesture: UIPanGestureRecognizer) {
        guard currentImage != nil else { return }

        let move = gesture.translation(in: self)

        switch gesture.state {
        case .began, .changed:
            imageView.center = CGPoint(
                x: imageView.center.x + move.x,
                y: imageView.center.y + move.y
            )
            gesture.setTranslation(.zero, in: self)

        default:
            break
        }
    }

    @objc private func handleLongPress(_ gesture: UILongPressGestureRecognizer) {
        guard let image = currentImage else { return }

        let pointInSelf = gesture.location(in: self)
        let pointInImageView = gesture.location(in: imageView)

        switch gesture.state {
        case .began, .changed:
            magnifierView.isHidden = false
            magnifierView.touchPoint = pointInImageView
            magnifierView.sourceImage = image
            magnifierView.setNeedsDisplay()

        default:
            magnifierView.isHidden = true
        }
    }

    private func mapViewPointToImagePixel(_ point: CGPoint, image: UIImage) -> (x: Int, y: Int)? {
        guard let displayedRect = imageFrameInImageView(image: image) else { return nil }

        guard displayedRect.contains(point) else { return nil }

        let relativeX = (point.x - displayedRect.minX) / displayedRect.width
        let relativeY = (point.y - displayedRect.minY) / displayedRect.height

        guard let cgImage = image.cgImage else { return nil }

        let pixelX = Int(relativeX * CGFloat(cgImage.width))
        let pixelY = Int(relativeY * CGFloat(cgImage.height))

        return (pixelX, pixelY)
    }

    private func imageFrameInImageView(image: UIImage) -> CGRect? {
        let imageViewSize = imageView.bounds.size
        let imageSize = image.size

        guard imageSize.width > 0, imageSize.height > 0 else { return nil }

        let scale = min(imageViewSize.width / imageSize.width, imageViewSize.height / imageSize.height)
        let width = imageSize.width * scale
        let height = imageSize.height * scale
        let x = (imageViewSize.width - width) / 2
        let y = (imageViewSize.height - height) / 2

        return CGRect(x: x, y: y, width: width, height: height)
    }
}

private final class MagnifierOverlayView: UIView {

    var sourceImage: UIImage?
    var touchPoint: CGPoint = .zero

    override func draw(_ rect: CGRect) {
        guard let sourceImage else { return }
        guard let ctx = UIGraphicsGetCurrentContext() else { return }

        let radius = rect.width / 2
        let center = CGPoint(x: rect.midX, y: rect.midY)
        let zoom: CGFloat = 2.5

        ctx.saveGState()

        let path = UIBezierPath(ovalIn: rect)
        path.addClip()

        UIColor.white.setFill()
        ctx.fill(rect)

        let drawX = center.x - touchPoint.x * zoom
        let drawY = center.y - touchPoint.y * zoom

        sourceImage.draw(in: CGRect(
            x: drawX,
            y: drawY,
            width: sourceImage.size.width * zoom,
            height: sourceImage.size.height * zoom
        ))

        ctx.restoreGState()

        UIColor.black.withAlphaComponent(0.25).setStroke()
        let outer = UIBezierPath(ovalIn: rect.insetBy(dx: 4, dy: 4))
        outer.lineWidth = 8
        outer.stroke()

        UIColor.white.setStroke()
        let border = UIBezierPath(ovalIn: rect.insetBy(dx: 2, dy: 2))
        border.lineWidth = 3
        border.stroke()

        UIColor.red.setStroke()
        let cross = UIBezierPath()
        cross.move(to: CGPoint(x: center.x - 15, y: center.y))
        cross.addLine(to: CGPoint(x: center.x + 15, y: center.y))
        cross.move(to: CGPoint(x: center.x, y: center.y - 15))
        cross.addLine(to: CGPoint(x: center.x, y: center.y + 15))
        cross.lineWidth = 2
        cross.stroke()
    }
}

private extension CGFloat {
    func clamped(to range: ClosedRange<CGFloat>) -> CGFloat {
        Swift.min(Swift.max(self, range.lowerBound), range.upperBound)
    }
}
