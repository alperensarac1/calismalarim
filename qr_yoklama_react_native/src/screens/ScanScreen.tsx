import React, { useEffect, useMemo, useRef, useState } from "react";
import { View, Text, Pressable, ActivityIndicator } from "react-native";
import { CameraView, useCameraPermissions } from "expo-camera";
import { AttendanceService } from "../service/attendanceService";
import { AppToast } from "../ui/toast";
import { ATTENDANCE_URL } from "../util/constants";
import { getExamUrl } from "../service/examService";

type Props = {
    route: { params: { studentNo: string } };
    navigation: any;
};

export default function ScanScreen({ route, navigation }: Props) {
    const studentNo = route.params.studentNo;
    const attendance = useMemo(() => new AttendanceService(), []);

    const [permission, requestPermission] = useCameraPermissions();
    const [isSending, setIsSending] = useState(false);
    const [scanEnabled, setScanEnabled] = useState(true);

    const lastTextRef = useRef<string | null>(null);
    const lastTsRef = useRef<number>(0);
    const DEBOUNCE_MS = 1200;

    useEffect(() => {
        if (!permission) return;
        if (!permission.granted) requestPermission();
    }, [permission, requestPermission]);

    // Web ekranından dönünce tekrar aç
    useEffect(() => {
        const unsub = navigation.addListener("focus", () => setScanEnabled(true));
        return unsub;
    }, [navigation]);

    const onBarcodeScanned = async (data: string) => {
        if (!scanEnabled || isSending) return;

        const now = Date.now();
        if (lastTextRef.current === data && now - lastTsRef.current < DEBOUNCE_MS) return;

        lastTextRef.current = data;
        lastTsRef.current = now;

        setIsSending(true);
        setScanEnabled(false);

        try {
            await attendance.sendByQR(studentNo, data);
            AppToast.success("Yoklama alındı ✅");
        } catch (e: any) {
            AppToast.error(String(e?.message ?? e));
        } finally {
            setIsSending(false);
            setScanEnabled(true);
        }
    };

    const openAttendance = () => {
        setScanEnabled(false);
        const url = `${ATTENDANCE_URL}?student_no=${encodeURIComponent(studentNo)}`;
        navigation.push("Web", { title: "Yoklama", url });
    };

    const openExam = async () => {
        setIsSending(true);
        setScanEnabled(false);
        try {
            const url = await getExamUrl(studentNo);
            navigation.push("Web", { title: "Sınav Yeri", url });
        } catch (e: any) {
            AppToast.error(String(e?.message ?? e));
        } finally {
            setIsSending(false);
            setScanEnabled(true);
        }
    };

    if (!permission) return null;

    if (!permission.granted) {
        return (
            <View style={{ flex: 1, alignItems: "center", justifyContent: "center", padding: 24 }}>
                <Text style={{ marginBottom: 12 }}>Kamera izni gerekli.</Text>
                <Pressable
                    onPress={requestPermission}
                    style={{ padding: 12, backgroundColor: "#6D28D9", borderRadius: 10 }}
                >
                    <Text style={{ color: "white", fontWeight: "600" }}>İzin Ver</Text>
                </Pressable>
            </View>
        );
    }

    return (
        <View style={{ flex: 1 }}>
           <CameraView
                style={{ position: "absolute", left: 0, right: 0, top: 0, bottom: 0 }}
                // ✅ scanEnabled false iken native event kapansın
                onBarcodeScanned={scanEnabled ? (evt) => onBarcodeScanned(evt.data) : undefined}
            />
            <View style={{ position: "absolute", top: 50, left: 0, right: 0, alignItems: "center" }}>
                <Pressable
                    onPress={openExam}
                    disabled={isSending}
                    style={{
                        paddingHorizontal: 18,
                        paddingVertical: 10,
                        borderRadius: 999,
                        backgroundColor: "#6D28D9",
                        opacity: isSending ? 0.6 : 1,
                    }}
                >
                    <Text style={{ color: "white", fontWeight: "600" }}>Sınav Yeri Sorgula</Text>
                </Pressable>
            </View>

            <View style={{ position: "absolute", left: 24, bottom: 24 }}>
                <Pressable
                    onPress={openAttendance}
                    disabled={isSending}
                    style={{
                        width: 56,
                        height: 56,
                        borderRadius: 28,
                        backgroundColor: "#6D28D9",
                        alignItems: "center",
                        justifyContent: "center",
                        opacity: isSending ? 0.6 : 1,
                    }}
                >
                    <Text style={{ color: "white", fontSize: 18 }}>≡</Text>
                </Pressable>
            </View>

            {isSending ? (
                <View
                    style={{
                        position: "absolute",
                        left: 0,
                        right: 0,
                        top: 0,
                        bottom: 0,
                        backgroundColor: "rgba(0,0,0,0.25)",
                        alignItems: "center",
                        justifyContent: "center",
                    }}
                >
                    <ActivityIndicator size="large" />
                </View>
            ) : null}
        </View>
    );
}
