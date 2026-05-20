import React, { useEffect, useRef, useState } from "react";
import {
    FlatList,
    Image,
    StyleSheet,
    Text,
    TextInput,
    TouchableOpacity,
    View,
} from "react-native";

import { AppConfig } from "../config/AppConfig";
import { ChatMessageModel } from "../models/ChatMessageModel";
import { RoomModel } from "../models/RoomModel";
import { LiveSocketService } from "../services/LiveSocketService";

type Props = {
    room: RoomModel;
    onBack: () => void;
};

export function ViewerScreen({ room, onBack }: Props) {
    const [statusText, setStatusText] = useState("Bağlanıyor...");
    const [viewerCount, setViewerCount] = useState(room.viewerCount);
    const [frameBase64, setFrameBase64] = useState<string | null>(null);
    const [messageText, setMessageText] = useState("");
    const [chatMessages, setChatMessages] = useState<ChatMessageModel[]>([]);

    const socketRef = useRef<LiveSocketService | null>(null);

    useEffect(() => {
        const socket = new LiveSocketService(AppConfig.SERVER_URL, {
            onConnected: () => {
                setStatusText("Sunucuya bağlandı, odaya giriliyor...");

                socket.sendJson({
                    type: "join_room",
                    room_id: room.roomId,
                    username: "Expo İzleyici",
                });
            },

            onMessage: message => {
                try {
                    const data = JSON.parse(message);

                    if (data.type === "joined_room") {
                        setStatusText("Yayına bağlandı");
                    }

                    if (data.type === "viewer_count") {
                        setViewerCount(data.viewer_count);
                    }

                    if (data.type === "video_frame") {
                        setFrameBase64(data.frame);
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

                    if (data.type === "stream_ended") {
                        setStatusText("Yayın sona erdi");
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
            socket.disconnect();
            socketRef.current = null;
        };
    }, [room.roomId]);

    function sendChatMessage() {
        const message = messageText.trim();

        if (!message) return;

        socketRef.current?.sendJson({
            type: "chat_message",
            message,
        });

        setMessageText("");
    }

    return (
        <View style={styles.container}>
            <TouchableOpacity style={styles.backButton} onPress={onBack}>
                <Text style={styles.backButtonText}>Geri</Text>
            </TouchableOpacity>

            <Text style={styles.title}>{room.title}</Text>

            <Text style={styles.infoText}>İzleyici: {viewerCount}</Text>
            <Text style={styles.status}>{statusText}</Text>

            <View style={styles.videoContainer}>
                {frameBase64 ? (
                    <Image
                        style={styles.videoImage}
                        source={{
                            uri: `data:image/jpeg;base64,${frameBase64}`,
                        }}
                        resizeMode="contain"
                    />
                ) : (
                    <Text style={styles.waitingText}>Görüntü bekleniyor...</Text>
                )}
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
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 12,
        backgroundColor: "#020617",
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
    title: {
        color: "#FFFFFF",
        fontSize: 22,
        fontWeight: "800",
        marginBottom: 4,
    },
    infoText: {
        color: "#E2E8F0",
        marginBottom: 2,
    },
    status: {
        color: "#CBD5E1",
        marginBottom: 8,
    },
    videoContainer: {
        width: "100%",
        height: 260,
        backgroundColor: "#111827",
        borderRadius: 12,
        justifyContent: "center",
        alignItems: "center",
        overflow: "hidden",
        marginBottom: 12,
    },
    videoImage: {
        width: "100%",
        height: "100%",
    },
    waitingText: {
        color: "#94A3B8",
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
});