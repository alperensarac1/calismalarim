import React, { useEffect, useRef, useState } from "react";
import {
    FlatList,
    StyleSheet,
    Text,
    TextInput,
    TouchableOpacity,
    View,
} from "react-native";

import { CameraView, useCameraPermissions } from "expo-camera";
import { AppConfig } from "../config/AppConfig";
import { ChatMessageModel } from "../models/ChatMessageModel";
import { LiveSocketService } from "../services/LiveSocketService";

type Props = {
    onBack: () => void;
};

export function BroadcasterScreen({ onBack }: Props) {
    const [permission, requestPermission] = useCameraPermissions();

    const [statusText, setStatusText] = useState("Sunucuya bağlanıyor...");
    const [broadcastTitle, setBroadcastTitle] = useState("");
    const [roomId, setRoomId] = useState<string | null>(null);
    const [viewerCount, setViewerCount] = useState(0);
    const [messageText, setMessageText] = useState("");
    const [chatMessages, setChatMessages] = useState<ChatMessageModel[]>([]);

    const socketRef = useRef<LiveSocketService | null>(null);
    const cameraRef = useRef<CameraView | null>(null);
    const frameTimerRef = useRef<NodeJS.Timeout | null>(null);

    useEffect(() => {
        if (!permission?.granted) {
            requestPermission();
        }
    }, [permission]);

    useEffect(() => {
        const socket = new LiveSocketService(AppConfig.SERVER_URL, {
            onConnected: () => {
                setStatusText("Sunucuya bağlandı. Başlık yazıp yayını başlat.");
            },

            onMessage: message => {
                try {
                    const data = JSON.parse(message);

                    if (data.type === "room_created") {
                        setRoomId(data.room_id);
                        setStatusText("Yayın başladı");
                    }

                    if (data.type === "viewer_count") {
                        setViewerCount(data.viewer_count);
                    }

                    if (data.type === "chat_message") {
                        const chat: ChatMessageModel = {
                            roomId: data.room_id,
                            username: data.username,
                            message: data.message,
                            createdAt: data.created_at,
                        };

                        setChatMessages(prev => [...prev, chat]);
                    }

                    if (data.type === "error") {
                        setStatusText(data.message);
                    }
                } catch {
                    setStatusText("Sunucudan gelen veri okunamadı");
                }
            },

            onError: error => {
                setStatusText(`Hata: ${error}`);
            },

            onDisconnected: () => {
                setStatusText("Bağlantı kapandı");
            },
        });

        socketRef.current = socket;
        socket.connect();

        return () => {
            stopFrameSender();
            socket.disconnect();
            socketRef.current = null;
        };
    }, []);

    useEffect(() => {
        if (roomId) {
            startFrameSender();
        }

        return () => {
            stopFrameSender();
        };
    }, [roomId]);

    function startBroadcast() {
        const title = broadcastTitle.trim();

        if (!title) {
            setStatusText("Yayın başlığı yazmalısın");
            return;
        }

        socketRef.current?.sendJson({
            type: "create_room",
            title,
            broadcaster_name: "Expo Yayıncı",
        });

        setStatusText("Oda oluşturuluyor...");
    }

    function sendChatMessage() {
        const message = messageText.trim();

        if (!message) return;

        socketRef.current?.sendJson({
            type: "chat_message",
            message,
        });

        setMessageText("");
    }

    function startFrameSender() {
        stopFrameSender();

        frameTimerRef.current = setInterval(async () => {
            try {
                if (!cameraRef.current) return;
                if (!roomId) return;

                const photo = await cameraRef.current.takePictureAsync({
                    quality: 0.35,
                    base64: true,
                    skipProcessing: true,
                });

                if (!photo?.base64) return;

                socketRef.current?.sendJson({
                    type: "video_frame",
                    frame: photo.base64,
                });
            } catch {
                // Kamera hazır değilse sessiz geçiyoruz.
            }
        }, 300);
    }

    function stopFrameSender() {
        if (frameTimerRef.current) {
            clearInterval(frameTimerRef.current);
            frameTimerRef.current = null;
        }
    }

    if (!permission) {
        return (
            <View style={styles.centerContainer}>
                <Text>Kamera izni kontrol ediliyor...</Text>
            </View>
        );
    }

    if (!permission.granted) {
        return (
            <View style={styles.centerContainer}>
                <Text style={styles.permissionText}>Kamera izni gerekli</Text>

                <TouchableOpacity style={styles.button} onPress={requestPermission}>
                    <Text style={styles.buttonText}>İzin Ver</Text>
                </TouchableOpacity>

                <TouchableOpacity style={styles.grayButton} onPress={onBack}>
                    <Text style={styles.buttonText}>Geri</Text>
                </TouchableOpacity>
            </View>
        );
    }

    return (
        <View style={styles.container}>
            <TouchableOpacity style={styles.backButton} onPress={onBack}>
                <Text style={styles.backButtonText}>Geri</Text>
            </TouchableOpacity>

            <Text style={styles.status}>{statusText}</Text>

            <TextInput
                style={styles.titleInput}
                value={broadcastTitle}
                onChangeText={setBroadcastTitle}
                editable={!roomId}
                placeholder="Yayın başlığı yaz..."
                placeholderTextColor="#94A3B8"
            />

            <TouchableOpacity
                style={[styles.button, roomId ? styles.disabledButton : null]}
                disabled={!!roomId}
                onPress={startBroadcast}
            >
                <Text style={styles.buttonText}>
                    {roomId ? "Yayın Başladı" : "Yayını Başlat"}
                </Text>
            </TouchableOpacity>

            <Text style={styles.infoText}>İzleyici: {viewerCount}</Text>

            <View style={styles.cameraContainer}>
                <CameraView
                    ref={cameraRef}
                    style={styles.camera}
                    facing="front"
                />
            </View>

            <Text style={styles.chatTitle}>Canlı Sohbet</Text>

            <FlatList
                style={styles.chatList}
                data={chatMessages}
                keyExtractor={(_, index) => index.toString()}
                renderItem={({ item }) => (
                    <View style={styles.chatItem}>
                        <Text style={styles.chatUsername}>{item.username}</Text>
                        <Text style={styles.chatMessage}>{item.message}</Text>
                    </View>
                )}
            />

            <View style={styles.messageRow}>
                <TextInput
                    style={styles.messageInput}
                    value={messageText}
                    onChangeText={setMessageText}
                    placeholder="Mesaj yaz..."
                    placeholderTextColor="#94A3B8"
                />

                <TouchableOpacity style={styles.sendButton} onPress={sendChatMessage}>
                    <Text style={styles.sendButtonText}>Gönder</Text>
                </TouchableOpacity>
            </View>

            <TouchableOpacity style={styles.stopButton} onPress={onBack}>
                <Text style={styles.buttonText}>Yayını Bitir</Text>
            </TouchableOpacity>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 12,
        backgroundColor: "#020617",
    },
    centerContainer: {
        flex: 1,
        padding: 24,
        justifyContent: "center",
        alignItems: "center",
        backgroundColor: "#F8FAFC",
    },
    permissionText: {
        fontSize: 18,
        fontWeight: "700",
        marginBottom: 16,
    },
    backButton: {
        alignSelf: "flex-start",
        backgroundColor: "#334155",
        paddingVertical: 8,
        paddingHorizontal: 14,
        borderRadius: 10,
        marginBottom: 8,
    },
    backButtonText: {
        color: "#FFFFFF",
        fontWeight: "700",
    },
    status: {
        color: "#FFFFFF",
        marginBottom: 8,
    },
    titleInput: {
        backgroundColor: "#1E293B",
        color: "#FFFFFF",
        borderRadius: 10,
        paddingHorizontal: 12,
        paddingVertical: 10,
        marginBottom: 8,
    },
    button: {
        backgroundColor: "#2563EB",
        padding: 14,
        borderRadius: 12,
        marginBottom: 8,
    },
    grayButton: {
        backgroundColor: "#334155",
        padding: 14,
        borderRadius: 12,
        marginTop: 8,
    },
    disabledButton: {
        backgroundColor: "#475569",
    },
    buttonText: {
        color: "#FFFFFF",
        fontWeight: "800",
        textAlign: "center",
    },
    infoText: {
        color: "#E2E8F0",
        marginBottom: 8,
    },
    cameraContainer: {
        width: "100%",
        height: 240,
        backgroundColor: "#111827",
        borderRadius: 12,
        overflow: "hidden",
        marginBottom: 12,
    },
    camera: {
        flex: 1,
    },
    chatTitle: {
        color: "#FFFFFF",
        fontSize: 17,
        fontWeight: "800",
        marginBottom: 8,
    },
    chatList: {
        flex: 1,
        marginBottom: 8,
    },
    chatItem: {
        paddingVertical: 5,
    },
    chatUsername: {
        color: "#93C5FD",
        fontWeight: "700",
        fontSize: 13,
    },
    chatMessage: {
        color: "#FFFFFF",
        fontSize: 14,
    },
    messageRow: {
        flexDirection: "row",
        gap: 8,
    },
    messageInput: {
        flex: 1,
        backgroundColor: "#1E293B",
        color: "#FFFFFF",
        borderRadius: 10,
        paddingHorizontal: 12,
        paddingVertical: 10,
    },
    sendButton: {
        backgroundColor: "#2563EB",
        borderRadius: 10,
        justifyContent: "center",
        paddingHorizontal: 14,
    },
    sendButtonText: {
        color: "#FFFFFF",
        fontWeight: "800",
    },
    stopButton: {
        backgroundColor: "#DC2626",
        padding: 14,
        borderRadius: 12,
        marginTop: 8,
    },
});