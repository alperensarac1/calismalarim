import { useCallback, useMemo, useRef, useState } from "react";
import * as ImagePicker from "expo-image-picker";
import * as MediaLibrary from "expo-media-library";
import * as FileSystem from "expo-file-system/legacy";
import {
    base64ToBytes,
    decodeImageBytes,
    encodePngBase64,
} from "./imageCodec";
import { removeConnectedRegionByColor } from "./imageProcessor";
import type { IntPoint, RgbaImage } from "./types";

export function useEditor() {
    const [originalImage, setOriginalImage] = useState<RgbaImage | null>(null);
    const [workingImage, setWorkingImage] = useState<RgbaImage | null>(null);

    const [tolerance, setTolerance] = useState(60);
    const [infoText, setInfoText] = useState(
        "Önce fotoğraf seçin. Sonra silmek istediğiniz bölgeye dokunun."
    );
    const [isProcessing, setIsProcessing] = useState(false);

    const [hasActivePreview, setHasActivePreview] = useState(false);
    const previewBaseRef = useRef<RgbaImage | null>(null);
    const lastTappedRef = useRef<IntPoint | null>(null);

    const undoStackRef = useRef<RgbaImage[]>([]);
    const previewRequestIdRef = useRef(0);

    const workingPngBase64 = useMemo(() => {
        if (!workingImage) return null;
        return encodePngBase64(workingImage);
    }, [workingImage]);

    const canUndo = undoStackRef.current.length > 0;

    const setIdleMessage = useCallback((text: string) => {
        setIsProcessing(false);
        setInfoText(text);
    }, []);

    const saveUndoSnapshot = useCallback((image: RgbaImage) => {
        const snapshot: RgbaImage = {
            width: image.width,
            height: image.height,
            data: new Uint8Array(image.data),
        };

        if (undoStackRef.current.length >= 10) {
            undoStackRef.current.shift();
        }
        undoStackRef.current.push(snapshot);
    }, []);

    const commitActivePreviewIfNeeded = useCallback(() => {
        if (!hasActivePreview) return;
        setHasActivePreview(false);
        previewBaseRef.current = null;
        lastTappedRef.current = null;
    }, [hasActivePreview]);

    const renderPreviewFromActiveState = useCallback(
        (nextTolerance?: number) => {
            const base = previewBaseRef.current;
            const point = lastTappedRef.current;
            if (!base || !point) return;

            const requestId = ++previewRequestIdRef.current;
            const toleranceValue = nextTolerance ?? tolerance;

            setIsProcessing(true);
            setInfoText("Canlı önizleme güncelleniyor...");

            setTimeout(() => {
                try {
                    const result = removeConnectedRegionByColor(
                        {
                            width: base.width,
                            height: base.height,
                            data: new Uint8Array(base.data),
                        },
                        point.x,
                        point.y,
                        toleranceValue
                    );

                    if (requestId !== previewRequestIdRef.current) return;

                    setWorkingImage(result);
                    setIsProcessing(false);
                    setInfoText(`Canlı önizleme aktif. Tolerans: ${Math.round(toleranceValue)}`);
                } catch (e) {
                    if (requestId !== previewRequestIdRef.current) return;
                    setIsProcessing(false);
                    setInfoText("Önizleme oluşturulamadı.");
                }
            }, 0);
        },
        [tolerance]
    );

    const pickImage = useCallback(async () => {
        setIsProcessing(true);
        setInfoText("Fotoğraf yükleniyor...");

        const pickResult = await ImagePicker.launchImageLibraryAsync({
            mediaTypes: ["images"],
            quality: 1,
            base64: true,
        });

        if (pickResult.canceled || !pickResult.assets?.length) {
            setIdleMessage("Fotoğraf seçilmedi.");
            return;
        }

        try {
            const asset = pickResult.assets[0];
            if (!asset.base64) {
                setIdleMessage("Görsel base64 alınamadı.");
                return;
            }

            const bytes = base64ToBytes(asset.base64);
            const decoded = decodeImageBytes(bytes);

            previewRequestIdRef.current += 1;
            undoStackRef.current = [];
            previewBaseRef.current = null;
            lastTappedRef.current = null;

            setOriginalImage(decoded);
            setWorkingImage(decoded);
            setHasActivePreview(false);
            setIsProcessing(false);
            setInfoText("Fotoğraf yüklendi. Silmek istediğiniz bölgeye dokunun.");
        } catch (e) {
            setIdleMessage("Resim yüklenemedi.");
        }
    }, [setIdleMessage]);

    const onToleranceChange = useCallback(
        (value: number) => {
            setTolerance(value);
            if (previewBaseRef.current && lastTappedRef.current) {
                renderPreviewFromActiveState(value);
            }
        },
        [renderPreviewFromActiveState]
    );

    const onImageTap = useCallback(
        (x: number, y: number) => {
            const current = workingImage;
            if (!current) return;
            if (x < 0 || y < 0 || x >= current.width || y >= current.height) return;

            commitActivePreviewIfNeeded();
            saveUndoSnapshot(current);

            previewBaseRef.current = {
                width: current.width,
                height: current.height,
                data: new Uint8Array(current.data),
            };
            lastTappedRef.current = { x, y };
            setHasActivePreview(true);
            setInfoText("Canlı önizleme hazırlanıyor...");

            renderPreviewFromActiveState();
        },
        [workingImage, commitActivePreviewIfNeeded, saveUndoSnapshot, renderPreviewFromActiveState]
    );

    const undo = useCallback(() => {
        previewRequestIdRef.current += 1;
        const stack = undoStackRef.current;
        if (!stack.length) return;

        const previous = stack.pop()!;
        previewBaseRef.current = null;
        lastTappedRef.current = null;

        setWorkingImage(previous);
        setHasActivePreview(false);
        setIsProcessing(false);
        setInfoText("Son işlem geri alındı.");
    }, []);

    const reset = useCallback(() => {
        previewRequestIdRef.current += 1;
        if (!originalImage) return;

        undoStackRef.current = [];
        previewBaseRef.current = null;
        lastTappedRef.current = null;

        setWorkingImage({
            width: originalImage.width,
            height: originalImage.height,
            data: new Uint8Array(originalImage.data),
        });
        setHasActivePreview(false);
        setIsProcessing(false);
        setInfoText("Görsel sıfırlandı.");
    }, [originalImage]);

    const savePngToGallery = useCallback(async () => {
        commitActivePreviewIfNeeded();

        if (!workingPngBase64) return;

        const permission = await MediaLibrary.requestPermissionsAsync();
        if (!permission.granted) {
            setInfoText("Galeri izni verilmedi.");
            return;
        }

        try {
            setIsProcessing(true);
            setInfoText("PNG kaydediliyor...");

            const fileUri =
                FileSystem.cacheDirectory +
                `bg_removed_${Date.now()}.png`;

            await FileSystem.writeAsStringAsync(fileUri, workingPngBase64, {
                encoding: FileSystem.EncodingType.Base64,
            });

            await MediaLibrary.saveToLibraryAsync(fileUri);

            setIsProcessing(false);
            setInfoText("PNG galeriye kaydedildi.");
        } catch (e) {
            setIsProcessing(false);
            setInfoText("PNG kaydetme başarısız oldu.");
        }
    }, [commitActivePreviewIfNeeded, workingPngBase64]);

    return {
        originalImage,
        workingImage,
        workingPngBase64,
        tolerance,
        infoText,
        isProcessing,
        canUndo,
        hasActivePreview,
        pickImage,
        onToleranceChange,
        onImageTap,
        undo,
        reset,
        savePngToGallery,
    };
}
