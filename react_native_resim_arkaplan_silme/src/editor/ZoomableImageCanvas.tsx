import React, { useMemo, useRef, useState } from "react";
import {
    LayoutChangeEvent,
    NativeSyntheticEvent,
    StyleSheet,
    Text,
    View,
    Image,
    ViewProps,
} from "react-native";

type Props = {
    imageBase64: string | null;
    imageWidth: number;
    imageHeight: number;
    onTapPixel: (x: number, y: number) => void;
};

type Size = { width: number; height: number };
type Point = { x: number; y: number };

function clamp(value: number, min: number, max: number) {
    return Math.max(min, Math.min(value, max));
}

function distance(a: Point, b: Point) {
    const dx = a.x - b.x;
    const dy = a.y - b.y;
    return Math.sqrt(dx * dx + dy * dy);
}

export default function ZoomableImageCanvas({
                                                imageBase64,
                                                imageWidth,
                                                imageHeight,
                                                onTapPixel,
                                            }: Props) {
    const [viewSize, setViewSize] = useState<Size>({ width: 0, height: 0 });
    const [scale, setScale] = useState(1);
    const [offset, setOffset] = useState<Point>({ x: 0, y: 0 });

    const [magnifierVisible, setMagnifierVisible] = useState(false);
    const [touchPoint, setTouchPoint] = useState<Point>({ x: 0, y: 0 });

    const longPressTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
    const gestureStartRef = useRef<{
        mode: "none" | "pan" | "pinch";
        startPoint: Point;
        startOffset: Point;
        startScale: number;
        startDistance: number;
        moved: boolean;
    }>({
        mode: "none",
        startPoint: { x: 0, y: 0 },
        startOffset: { x: 0, y: 0 },
        startScale: 1,
        startDistance: 0,
        moved: false,
    });

    const sourceUri = useMemo(
        () => (imageBase64 ? `data:image/png;base64,${imageBase64}` : null),
        [imageBase64]
    );

    const fitRect = useMemo(() => {
        if (!viewSize.width || !viewSize.height || !imageWidth || !imageHeight) {
            return { left: 0, top: 0, width: 0, height: 0 };
        }

        const fitScale = Math.min(
            viewSize.width / imageWidth,
            viewSize.height / imageHeight
        );

        const width = imageWidth * fitScale * scale;
        const height = imageHeight * fitScale * scale;
        const left = (viewSize.width - width) / 2 + offset.x;
        const top = (viewSize.height - height) / 2 + offset.y;

        return { left, top, width, height };
    }, [viewSize, imageWidth, imageHeight, scale, offset]);

    const onLayout = (e: LayoutChangeEvent) => {
        const { width, height } = e.nativeEvent.layout;
        setViewSize({ width, height });
    };

    const mapTouchToPixel = (p: Point): Point | null => {
        const { left, top, width, height } = fitRect;
        if (width <= 0 || height <= 0) return null;

        const localX = p.x - left;
        const localY = p.y - top;

        if (localX < 0 || localY < 0 || localX > width || localY > height) {
            return null;
        }

        const px = Math.floor((localX / width) * imageWidth);
        const py = Math.floor((localY / height) * imageHeight);

        if (px < 0 || py < 0 || px >= imageWidth || py >= imageHeight) {
            return null;
        }

        return { x: px, y: py };
    };

    const responder: ViewProps = {
        onStartShouldSetResponder: () => true,
        onMoveShouldSetResponder: () => true,

        onResponderGrant: (e) => {
            const touches = e.nativeEvent.touches;

            if (touches.length === 1) {
                const p = {
                    x: touches[0].locationX,
                    y: touches[0].locationY,
                };

                gestureStartRef.current = {
                    mode: "pan",
                    startPoint: p,
                    startOffset: offset,
                    startScale: scale,
                    startDistance: 0,
                    moved: false,
                };

                longPressTimer.current && clearTimeout(longPressTimer.current);
                longPressTimer.current = setTimeout(() => {
                    setMagnifierVisible(true);
                    setTouchPoint(p);
                }, 350);
            } else if (touches.length >= 2) {
                longPressTimer.current && clearTimeout(longPressTimer.current);

                const p1 = {
                    x: touches[0].locationX,
                    y: touches[0].locationY,
                };
                const p2 = {
                    x: touches[1].locationX,
                    y: touches[1].locationY,
                };

                gestureStartRef.current = {
                    mode: "pinch",
                    startPoint: p1,
                    startOffset: offset,
                    startScale: scale,
                    startDistance: distance(p1, p2),
                    moved: true,
                };
            }
        },

        onResponderMove: (e) => {
            const touches = e.nativeEvent.touches;

            if (touches.length >= 2) {
                longPressTimer.current && clearTimeout(longPressTimer.current);
                setMagnifierVisible(false);

                const p1 = {
                    x: touches[0].locationX,
                    y: touches[0].locationY,
                };
                const p2 = {
                    x: touches[1].locationX,
                    y: touches[1].locationY,
                };

                const currentDistance = distance(p1, p2);
                const startDistance = gestureStartRef.current.startDistance || currentDistance;
                const startScale = gestureStartRef.current.startScale || 1;
                const nextScale = clamp((currentDistance / startDistance) * startScale, 1, 5);

                setScale(nextScale);
                gestureStartRef.current.moved = true;
                return;
            }

            if (touches.length === 1) {
                const p = {
                    x: touches[0].locationX,
                    y: touches[0].locationY,
                };

                if (magnifierVisible) {
                    setTouchPoint(p);
                    return;
                }

                const dx = p.x - gestureStartRef.current.startPoint.x;
                const dy = p.y - gestureStartRef.current.startPoint.y;

                if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
                    gestureStartRef.current.moved = true;
                    longPressTimer.current && clearTimeout(longPressTimer.current);

                    setOffset({
                        x: gestureStartRef.current.startOffset.x + dx,
                        y: gestureStartRef.current.startOffset.y + dy,
                    });
                }
            }
        },

        onResponderRelease: (e) => {
            longPressTimer.current && clearTimeout(longPressTimer.current);

            const changed = e.nativeEvent.changedTouches?.[0];
            const endPoint = changed
                ? { x: changed.locationX, y: changed.locationY }
                : touchPoint;

            const wasMoved = gestureStartRef.current.moved;
            const wasMagnifier = magnifierVisible;

            setMagnifierVisible(false);

            if (!wasMoved && !wasMagnifier) {
                const mapped = mapTouchToPixel(endPoint);
                if (mapped) {
                    onTapPixel(mapped.x, mapped.y);
                }
            }

            gestureStartRef.current.mode = "none";
        },

        onResponderTerminate: () => {
            longPressTimer.current && clearTimeout(longPressTimer.current);
            setMagnifierVisible(false);
            gestureStartRef.current.mode = "none";
        },
    };

    const magnifierPixel = mapTouchToPixel(touchPoint);
    const magnifierSize = 170;
    const magnifierZoom = 2.5;

    return (
        <View style={styles.root} onLayout={onLayout} {...responder}>
            {!sourceUri ? (
                <View style={styles.empty}>
                    <Text style={styles.emptyText}>Fotoğraf seçilmedi</Text>
                </View>
            ) : (
                <>
                    <Image
                        source={{ uri: sourceUri }}
                        resizeMode="stretch"
                        style={{
                            position: "absolute",
                            left: fitRect.left,
                            top: fitRect.top,
                            width: fitRect.width,
                            height: fitRect.height,
                        }}
                    />

                    {magnifierVisible && magnifierPixel && sourceUri ? (
                        <View style={styles.magnifierWrap}>
                            <View
                                style={[
                                    styles.magnifier,
                                    { width: magnifierSize, height: magnifierSize, borderRadius: magnifierSize / 2 },
                                ]}
                            >
                                <Image
                                    source={{ uri: sourceUri }}
                                    resizeMode="stretch"
                                    style={{
                                        position: "absolute",
                                        width: imageWidth * magnifierZoom,
                                        height: imageHeight * magnifierZoom,
                                        left: -magnifierPixel.x * magnifierZoom + magnifierSize / 2,
                                        top: -magnifierPixel.y * magnifierZoom + magnifierSize / 2,
                                    }}
                                />
                                <View style={styles.crossH} />
                                <View style={styles.crossV} />
                            </View>
                        </View>
                    ) : null}
                </>
            )}
        </View>
    );
}

const styles = StyleSheet.create({
    root: {
        flex: 1,
        backgroundColor: "#d9d9d9",
        overflow: "hidden",
        borderRadius: 14,
    },
    empty: {
        flex: 1,
        alignItems: "center",
        justifyContent: "center",
    },
    emptyText: {
        color: "#666",
        fontSize: 16,
    },
    magnifierWrap: {
        position: "absolute",
        top: 16,
        right: 16,
    },
    magnifier: {
        overflow: "hidden",
        borderWidth: 4,
        borderColor: "#fff",
        backgroundColor: "#fff",
    },
    crossH: {
        position: "absolute",
        left: "50%",
        top: "50%",
        width: 28,
        height: 2,
        marginLeft: -14,
        marginTop: -1,
        backgroundColor: "red",
    },
    crossV: {
        position: "absolute",
        left: "50%",
        top: "50%",
        width: 2,
        height: 28,
        marginLeft: -1,
        marginTop: -14,
        backgroundColor: "red",
    },
});
