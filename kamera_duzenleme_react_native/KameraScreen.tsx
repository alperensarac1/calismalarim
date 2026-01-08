import React, { JSX, useEffect, useRef, useState } from "react";
import {
    ActivityIndicator,
    Image,
    Modal,
    SafeAreaView,
    StyleSheet,
    Text,
    TextInput,
    TouchableOpacity,
    View,
} from "react-native";

import { CameraView, CameraType, useCameraPermissions } from "expo-camera";
import * as MediaLibrary from "expo-media-library";
import ViewShot from "react-native-view-shot";

/** react-native-view-shot paketi ViewShotRef export etmediği için handle tipini kendimiz tanımlıyoruz */
type ViewShotHandle = {
    capture: (options?: {
        format?: "png" | "jpg" | "webm";
        quality?: number;
        result?: "tmpfile" | "base64" | "data-uri";
    }) => Promise<string>;
};

type TakePictureResult = { uri: string };

export default function KameraScreen(): JSX.Element {
    const cameraRef = useRef<CameraView | null>(null);
    const shotRef = useRef<ViewShotHandle | null>(null);

    const [cameraPermission, requestCameraPermission] = useCameraPermissions();
    const [mediaGranted, setMediaGranted] = useState<boolean>(false);

    const [showCamera, setShowCamera] = useState<boolean>(false);
    const [photoUri, setPhotoUri] = useState<string | null>(null);

    const [userText, setUserText] = useState<string>("");
    const [previewUri, setPreviewUri] = useState<string | null>(null);
    const [previewVisible, setPreviewVisible] = useState<boolean>(false);

    const [saving, setSaving] = useState<boolean>(false);

    useEffect(() => {
        (async () => {
            const perm = await MediaLibrary.requestPermissionsAsync();
            setMediaGranted(perm.status === "granted");
        })();
    }, []);

    const openCamera = async (): Promise<void> => {
        if (!cameraPermission?.granted) {
            const res = await requestCameraPermission();
            if (!res.granted) return;
        }
        setShowCamera(true);
    };

    const takePhoto = async (): Promise<void> => {
        try {
            const cam = cameraRef.current;
            if (!cam) return;

            // Expo CameraView tipleri sürüme göre değişebiliyor.
            // Bu cast ile TS tarafını sakinleştiriyoruz.
            const pic = (await (cam as unknown as {
                takePictureAsync: (opts?: any) => Promise<TakePictureResult>;
            }).takePictureAsync({
                quality: 1,
                skipProcessing: false,
            })) as TakePictureResult;

            if (pic?.uri) {
                setPhotoUri(pic.uri);
                setPreviewUri(null);
                setShowCamera(false);
            }
        } catch (e) {
            console.log("takePhoto error:", e);
        }
    };

    const buildPreview = async (): Promise<void> => {
        if (!photoUri || !shotRef.current) return;

        setSaving(true);
        try {
            const uri = await shotRef.current.capture({
                format: "png",
                quality: 1,
                result: "tmpfile",
            });

            if (uri) {
                setPreviewUri(uri);
                setPreviewVisible(true);
            }
        } catch (e) {
            console.log("preview capture error:", e);
        } finally {
            setSaving(false);
        }
    };

    const saveToGallery = async (): Promise<void> => {
        if (!previewUri) return;

        setSaving(true);
        try {
            // Android: önce direkt kaydetmeyi dene
            await MediaLibrary.createAssetAsync(previewUri);
            setPreviewVisible(false);
        } catch (e) {
            console.log("save error:", e);
        } finally {
            setSaving(false);
        }
    };


    return (
        <SafeAreaView style={styles.root}>
            <Text style={styles.title}>Kamera Düzenleme (RN TS)</Text>

            {/* Kamera Modal */}
            <Modal visible={showCamera} animationType="slide">
                <SafeAreaView style={styles.cameraRoot}>
                    <CameraView
                        ref={(r) => {
                            cameraRef.current = r; // ✅ VOID döndürüyor
                        }}
                        style={styles.cameraView}
                        facing={"back" as CameraType}
                    />

                    <View style={styles.cameraBar}>
                        <TouchableOpacity
                            style={[styles.btn, styles.btnDark]}
                            onPress={() => setShowCamera(false)}
                            disabled={saving}
                        >
                            <Text style={styles.btnText}>İptal</Text>
                        </TouchableOpacity>

                        <TouchableOpacity
                            style={[styles.btn, styles.btnPrimary]}
                            onPress={takePhoto}
                            disabled={saving}
                        >
                            <Text style={styles.btnText}>Çek</Text>
                        </TouchableOpacity>
                    </View>
                </SafeAreaView>
            </Modal>

            {/* Önizleme Modal */}
            <Modal visible={previewVisible} transparent animationType="fade">
                <View style={styles.modalBackdrop}>
                    <View style={styles.modalCard}>
                        <Text style={styles.modalTitle}>Kaydetmeden Önce Önizleme</Text>

                        {previewUri ? (
                            <Image source={{ uri: previewUri }} style={styles.previewImage} />
                        ) : (
                            <View style={[styles.previewImage, styles.center]}>
                                <ActivityIndicator />
                            </View>
                        )}

                        <View style={styles.modalActions}>
                            <TouchableOpacity
                                style={[styles.btn, styles.btnDark]}
                                onPress={() => setPreviewVisible(false)}
                                disabled={saving}
                            >
                                <Text style={styles.btnText}>İptal</Text>
                            </TouchableOpacity>

                            <TouchableOpacity
                                style={[styles.btn, styles.btnPrimary]}
                                onPress={saveToGallery}
                                disabled={saving}
                            >
                                {saving ? (
                                    <ActivityIndicator color="#fff" />
                                ) : (
                                    <Text style={styles.btnText}>Evet, Kaydet</Text>
                                )}
                            </TouchableOpacity>
                        </View>
                    </View>
                </View>
            </Modal>

            {/* Foto + Metin Overlay: bunu ViewShot yakalıyor */}
            <ViewShot
                ref={(r) => {
                    shotRef.current = r as unknown as ViewShotHandle; // ✅ VOID döndürüyor
                }}
                style={styles.canvas}
                options={{ format: "png", quality: 1, result: "tmpfile" }}
            >
                <View style={styles.imageBox}>
                    {!photoUri ? (
                        <Text style={{ color: "#888" }}>Fotoğraf Yok</Text>
                    ) : (
                        <>
                            <Image source={{ uri: photoUri }} style={styles.image} />

                            {!!userText.trim() && (
                                <Text style={styles.overlayText} numberOfLines={3}>
                                    {userText}
                                </Text>
                            )}
                        </>
                    )}
                </View>
            </ViewShot>

            <TextInput
                value={userText}
                onChangeText={setUserText}
                placeholder="Fotoğrafa yazılacak metin"
                style={styles.input}
            />

            <View style={styles.row}>
                <TouchableOpacity
                    style={[styles.btn, styles.btnDark]}
                    onPress={openCamera}
                    disabled={saving}
                >
                    <Text style={styles.btnText}>Fotoğraf Çek</Text>
                </TouchableOpacity>

                <TouchableOpacity
                    style={[
                        styles.btn,
                        styles.btnPrimary,
                        !photoUri || saving ? styles.disabled : null,
                    ]}
                    onPress={buildPreview}
                    disabled={!photoUri || saving}
                >
                    {saving ? (
                        <ActivityIndicator color="#fff" />
                    ) : (
                        <Text style={styles.btnText}>Kaydet</Text>
                    )}
                </TouchableOpacity>
            </View>
        </SafeAreaView>
    );
}

const styles = StyleSheet.create({
    root: { flex: 1, padding: 16, backgroundColor: "#fff" },
    title: { fontSize: 18, fontWeight: "800", marginBottom: 12 },

    cameraRoot: { flex: 1, backgroundColor: "#000" },
    cameraView: { flex: 1 },
    cameraBar: {
        flexDirection: "row",
        justifyContent: "space-between",
        padding: 16,
        backgroundColor: "#000",
    },

    canvas: { alignSelf: "center" },
    imageBox: {
        width: 300,
        height: 300,
        borderWidth: 2,
        borderColor: "#bbb",
        alignItems: "center",
        justifyContent: "center",
        overflow: "hidden",
        backgroundColor: "#fafafa",
    },
    image: { width: "100%", height: "100%", resizeMode: "cover" },

    overlayText: {
        position: "absolute",
        left: 16,
        right: 16,
        bottom: 16,
        color: "#fff",
        fontSize: 20,
        fontWeight: "800",
        textShadowColor: "rgba(0,0,0,0.95)",
        textShadowOffset: { width: 1, height: 1 },
        textShadowRadius: 4,
    },

    input: {
        marginTop: 16,
        borderWidth: 1,
        borderColor: "#ddd",
        borderRadius: 12,
        paddingHorizontal: 12,
        paddingVertical: 10,
    },

    row: {
        flexDirection: "row",
        justifyContent: "center",
        gap: 12,
        marginTop: 16,
    },

    btn: {
        paddingHorizontal: 16,
        paddingVertical: 12,
        borderRadius: 12,
        minWidth: 130,
        alignItems: "center",
    },
    btnPrimary: { backgroundColor: "#2563eb" },
    btnDark: { backgroundColor: "#444" },
    btnText: { color: "#fff", fontWeight: "800" },
    disabled: { opacity: 0.5 },

    modalBackdrop: {
        flex: 1,
        backgroundColor: "rgba(0,0,0,0.55)",
        alignItems: "center",
        justifyContent: "center",
        padding: 16,
    },
    modalCard: {
        width: "100%",
        maxWidth: 380,
        backgroundColor: "#fff",
        borderRadius: 16,
        padding: 16,
    },
    modalTitle: { fontSize: 16, fontWeight: "900", marginBottom: 12 },
    previewImage: { width: "100%", height: 300, borderRadius: 12, backgroundColor: "#eee" },
    modalActions: { flexDirection: "row", justifyContent: "flex-end", gap: 12, marginTop: 12 },
    center: { alignItems: "center", justifyContent: "center" },
});
