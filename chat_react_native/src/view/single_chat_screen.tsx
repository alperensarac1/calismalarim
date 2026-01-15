import React, { useEffect, useRef, useState } from "react";
import { View, FlatList, KeyboardAvoidingView, Platform, Alert } from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";

import type { RootStackParamList } from "../../App";
import {Mesaj} from "../model/mesaj";
import {apiService} from "../client/api_service";
import {AppConfig} from "../util/app_config";
import MessageBubble from "./message_bubble";
import ChatInputBar from "./chat_input_bar";


type Props = NativeStackScreenProps<RootStackParamList, "SingleChat">;

export default function SingleChatScreen({ route }: Props) {
    const { aliciId } = route.params;

    const [mesajlar, setMesajlar] = useState<Mesaj[]>([]);
    const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);
    const listRef = useRef<FlatList<Mesaj>>(null);

    const load = async () => {
        try {
            const resp = await apiService.mesajlariGetir(AppConfig.kullaniciId, aliciId);
            if (resp.success) setMesajlar(resp.mesajlar || []);
        } catch (e: any) {
            Alert.alert("Hata", String(e?.message ?? e));
        }
    };

    useEffect(() => {
        load();
        timerRef.current = setInterval(load, 15000);
        return () => {
            if (timerRef.current) clearInterval(timerRef.current);
        };
    }, [aliciId]);

    useEffect(() => {
        if (mesajlar.length > 0) {
            requestAnimationFrame(() => listRef.current?.scrollToEnd({ animated: true }));
        }
    }, [mesajlar.length]);

    const onSend = async (text: string, imgBase64: string | null) => {
        const mesajText = text.trim();
        const resimVar = imgBase64 ? 1 : 0;
        if (!mesajText && !imgBase64) return;

        try {
            const resp = await apiService.mesajGonder({
                gonderenId: AppConfig.kullaniciId,
                aliciId,
                mesajText,
                resimVar,
                base64Img: imgBase64,
            });

            if (resp.success) {
                await load();
            } else {
                Alert.alert("Hata", resp.error || "Mesaj gönderilemedi");
            }
        } catch (e: any) {
            Alert.alert("Hata", String(e?.message ?? e));
        }
    };

    return (
        <KeyboardAvoidingView
            style={{ flex: 1 }}
            behavior={Platform.OS === "ios" ? "padding" : undefined}
            keyboardVerticalOffset={Platform.OS === "ios" ? 80 : 0}
        >
            <View style={{ flex: 1 }}>
                <FlatList
                    ref={listRef}
                    data={mesajlar}
                    keyExtractor={(x) => String(x.id)}
                    renderItem={({ item }) => <MessageBubble msg={item} benimId={AppConfig.kullaniciId} />}
                    contentContainerStyle={{ padding: 12, paddingBottom: 90 }}
                />
                <ChatInputBar onSend={onSend} />
            </View>
        </KeyboardAvoidingView>
    );
}
