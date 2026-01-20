import React, { useEffect, useState } from "react";
import { ActivityIndicator, Alert, Pressable, Text, View } from "react-native";

import { useNavigation } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import {RootStackParamList} from "../navigation/app_navigator";
import {authApi, UserDto} from "../api/auth_api";
import {tokenStore} from "../api/token_store";


export default function SettingsScreen() {
    const nav = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
    const [me, setMe] = useState<UserDto | null>(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        (async () => {
            setLoading(true);
            try {
                const u = await authApi.me();
                setMe(u);
            } catch (e: any) {
                // token yok / expired olabilir
            } finally {
                setLoading(false);
            }
        })();
    }, []);

    async function logout() {
        await tokenStore.clear();
        nav.replace("Login");
    }

    return (
        <View style={{ flex: 1, padding: 16 }}>
            <Text style={{ fontSize: 20, fontWeight: "800", marginBottom: 12 }}>Ayarlar</Text>

            {loading ? <ActivityIndicator /> : null}

            {me ? (
                <View style={{ borderWidth: 1, borderRadius: 12, padding: 12, marginBottom: 12 }}>
                    <Text style={{ fontWeight: "800" }}>{me.name}</Text>
                    <Text>{me.email}</Text>
                    <Text style={{ opacity: 0.7 }}>ID: {me.id}</Text>
                </View>
            ) : (
                <Text style={{ opacity: 0.7, marginBottom: 12 }}>Kullanıcı bilgisi alınamadı</Text>
            )}

            <Pressable
                onPress={() => Alert.alert("Çıkış", "Çıkmak istiyor musun?", [
                    { text: "İptal", style: "cancel" },
                    { text: "Çıkış", style: "destructive", onPress: logout },
                ])}
                style={{ padding: 12, backgroundColor: "#111", borderRadius: 12 }}
            >
                <Text style={{ color: "#fff", textAlign: "center", fontWeight: "700" }}>Çıkış Yap</Text>
            </Pressable>
        </View>
    );
}
