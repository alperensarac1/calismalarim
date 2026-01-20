import React, { useState } from "react";
import { Alert, Pressable, Text, TextInput, View } from "react-native";
import type { NativeStackScreenProps } from "@react-navigation/native-stack";
import {RootStackParamList} from "../navigation/app_navigator";
import {tokenStore} from "../api/token_store";
import {authApi} from "../api/auth_api";


type Props = NativeStackScreenProps<RootStackParamList, "Register">;

export default function RegisterScreen({ navigation }: Props) {
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [pass, setPass] = useState("");
    const [loading, setLoading] = useState(false);

    async function onRegister() {
        setLoading(true);
        try {
            const resp = await authApi.register({ name: name.trim(), email: email.trim(), password: pass });
            if (!resp.token) throw new Error("Token gelmedi");
            await tokenStore.set(resp.token);
            navigation.replace("Main");
        } catch (e: any) {
            Alert.alert("Hata", e?.message ?? "Kayıt başarısız");
        } finally {
            setLoading(false);
        }
    }

    return (
        <View style={{ flex: 1, padding: 16, justifyContent: "center" }}>
    <Text style={{ fontSize: 22, fontWeight: "800", marginBottom: 14 }}>Kayıt Ol</Text>

    <TextInput
    placeholder="Ad Soyad"
    value={name}
    onChangeText={setName}
    style={{ borderWidth: 1, padding: 12, borderRadius: 12, marginBottom: 10 }}
    />

    <TextInput
    placeholder="E-posta"
    value={email}
    onChangeText={setEmail}
    autoCapitalize="none"
    keyboardType="email-address"
    style={{ borderWidth: 1, padding: 12, borderRadius: 12, marginBottom: 10 }}
    />

    <TextInput
    placeholder="Şifre"
    value={pass}
    onChangeText={setPass}
    secureTextEntry
    style={{ borderWidth: 1, padding: 12, borderRadius: 12, marginBottom: 14 }}
    />

    <Pressable
    onPress={onRegister}
    disabled={loading}
    style={{ padding: 12, backgroundColor: "#111", borderRadius: 12 }}
>
    <Text style={{ color: "#fff", textAlign: "center", fontWeight: "700" }}>
    {loading ? "Kaydediliyor..." : "Kayıt Ol"}
    </Text>
    </Pressable>

    <Pressable onPress={() => navigation.goBack()} style={{ padding: 12 }}>
    <Text style={{ textAlign: "center" }}>Girişe dön</Text>
    </Pressable>
    </View>
);
}
