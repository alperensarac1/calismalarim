import React, { useEffect, useRef, useState } from "react";
import {
    View,
    Text,
    StyleSheet,
} from "react-native";
import TrackPlayer, {
    Capability,
    State,
    usePlaybackState,
} from "react-native-track-player";
import { NativeStackScreenProps } from "@react-navigation/native-stack";

import RadioSocketManager from "../network/RadioSocketManager";
import {RootStackParamList} from "../../App";

type Props = NativeStackScreenProps<RootStackParamList, "RadioPlayer">;

export default function RadioPlayerScreen({ route }: Props) {
    const { room } = route.params;

    const [musicTitle, setMusicTitle] = useState("Çalan müzik bekleniyor...");
    const [statusText, setStatusText] = useState("Odaya bağlanılıyor...");

    const currentMusicUrlRef = useRef<string | null>(null);
    const syncIntervalRef = useRef<NodeJS.Timeout | null>(null);

    useEffect(() => {
        setupPlayer();

        RadioSocketManager.onMessage = message => {
            handleSocketMessage(message);
        };

        RadioSocketManager.onError = error => {
            setStatusText(`Bağlantı hatası: ${error}`);
        };

        RadioSocketManager.connect();
        RadioSocketManager.joinRoom(room.id);

        syncIntervalRef.current = setInterval(() => {
            RadioSocketManager.requestSync(room.id);
        }, 5000);

        return () => {
            if (syncIntervalRef.current) {
                clearInterval(syncIntervalRef.current);
            }

            TrackPlayer.stop();
            TrackPlayer.reset();
        };
    }, []);

    async function setupPlayer() {
        try {
            await TrackPlayer.setupPlayer();

            await TrackPlayer.updateOptions({
                capabilities: [
                    Capability.Play,
                    Capability.Pause,
                ],
            });
        } catch (error) {
            // setupPlayer ikinci kez çağrılırsa hata verebilir.
            console.log("Player setup uyarısı:", error);
        }
    }

    async function handleSocketMessage(message: string) {
        try {
            const json = JSON.parse(message);

            if (json.type === "PLAYBACK_STATE") {
                if (json.roomId !== room.id) return;

                await playOrSyncMusic({
                    title: json.title ?? "Bilinmeyen müzik",
                    musicUrl: json.musicUrl,
                    positionSeconds: Number(json.positionSeconds ?? 0),
                });
            }

            if (json.type === "NO_MUSIC") {
                await TrackPlayer.pause();

                setMusicTitle("Bu odada şu an müzik yok");
                setStatusText("Bekleniyor...");
            }
        } catch (error) {
            console.log("Player mesaj hatası:", error);
        }
    }

    async function playOrSyncMusic(params: {
        title: string;
        musicUrl: string;
        positionSeconds: number;
    }) {
        const { title, musicUrl, positionSeconds } = params;

        setMusicTitle(title);
        setStatusText("Dinleniyor...");

        if (currentMusicUrlRef.current !== musicUrl) {
            currentMusicUrlRef.current = musicUrl;

            await TrackPlayer.reset();

            await TrackPlayer.add({
                id: musicUrl,
                url: musicUrl,
                title,
                artist: "SyncRadio",
            });

            await TrackPlayer.seekTo(positionSeconds);
            await TrackPlayer.play();

            return;
        }

        const currentPosition = await TrackPlayer.getPosition();
        const difference = Math.abs(currentPosition - positionSeconds);

        if (difference > 1.2) {
            await TrackPlayer.seekTo(positionSeconds);
        }

        const state = await TrackPlayer.getState();

        if (state !== State.Playing) {
            await TrackPlayer.play();
        }
    }

    return (
        <View style={styles.container}>
            <Text style={styles.roomName}>{room.roomName}</Text>

            <Text style={styles.musicTitle}>{musicTitle}</Text>

            <Text style={styles.status}>{statusText}</Text>

            <View style={styles.radioCircle}>
                <Text style={styles.radioIcon}>📻</Text>
            </View>

            <Text style={styles.info}>
                Bu ekran sadece dinleyici modudur.
            </Text>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 24,
        alignItems: "center",
        backgroundColor: "#F6F6F6",
    },
    roomName: {
        fontSize: 28,
        fontWeight: "800",
        marginTop: 30,
        marginBottom: 20,
        textAlign: "center",
    },
    musicTitle: {
        fontSize: 18,
        fontWeight: "600",
        textAlign: "center",
        marginBottom: 10,
    },
    status: {
        fontSize: 14,
        color: "#666",
        marginBottom: 60,
    },
    radioCircle: {
        width: 140,
        height: 140,
        borderRadius: 70,
        backgroundColor: "white",
        alignItems: "center",
        justifyContent: "center",
        elevation: 4,
        marginBottom: 30,
    },
    radioIcon: {
        fontSize: 64,
    },
    info: {
        fontSize: 13,
        color: "#777",
        textAlign: "center",
    },
});