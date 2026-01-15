import React, { useState } from "react";
import { View, Text, TextInput, Pressable, ActivityIndicator, Alert } from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";

import type { RootStackParamList } from "../../App";
import {apiService} from "../client/api_service";
import {AppConfig} from "../util/app_config";
import {PrefManager} from "../util/pref_manager";


type Props = NativeStackScreenProps<RootStackParamList, "Register">;

export default function RegisterScreen({ navigation }: Props) {
    const [ad, setAd] = useState("");
    const [numara, setNumara] = useState("");
    const [loading, setLoading] = useState(false);
    const [hata, setHata] = useState<string | null>(null);

    const kayitOl = async () => {
        if (!ad.trim() || !numara.trim()) {
            setHata("Boş alan bırakma!");
            return;
        }

        setLoading(true);
        setHata(null);

        try {
            const resp = await apiService.kullaniciKayit(ad.trim(), numara.trim());
            if (resp.success && resp.id) {
                AppConfig.kullaniciId = resp.id;
                await PrefManager.kaydetKullaniciId(resp.id);

                Alert.alert("Başarılı", "Kayıt başarılı!");
                navigation.reset({ index: 0, routes: [{ name: "Chats" }] });
            } else {
                setHata(resp.error || "Kayıt başarısız");
            }
        } catch (e: any) {
            setHata(`Hata: ${String(e?.message ?? e)}`);
        } finally {
            setLoading(false);
        }
    };

    return (
        <View style={{ flex: 1, padding: 16, gap: 12 }}>
            <TextInput
                placeholder="Ad"
                value={ad}
                onChangeText={setAd}
                style={{ borderWidth: 1, borderRadius: 10, padding: 12 }}
            />
            <TextInput
                placeholder="Numara"
                value={numara}
                onChangeText={setNumara}
                keyboardType="phone-pad"
                style={{ borderWidth: 1, borderRadius: 10, padding: 12 }}
            />

            {loading ? (
                <ActivityIndicator />
            ) : (
                <Pressable
                    onPress={kayitOl}
                    style={{ backgroundColor: "#6D28D9", padding: 14, borderRadius: 12, alignItems: "center" }}
                >
                    <Text style={{ color: "white", fontWeight: "600" }}>Kayıt Ol</Text>
                </Pressable>
            )}

            {hata ? <Text style={{ color: "red" }}>{hata}</Text> : null}
        </View>
    );
}
