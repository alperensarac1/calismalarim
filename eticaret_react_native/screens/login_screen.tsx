import React, { useState } from "react";
import { Alert, Pressable, Text, TextInput, View } from "react-native";

import type { NativeStackScreenProps } from "@react-navigation/native-stack";
import {RootStackParamList} from "../navigation/app_navigator";
import {authApi} from "../api/auth_api";
import {tokenStore} from "../api/token_store";

type Props = NativeStackScreenProps<RootStackParamList, "Login">;

export default function LoginScreen({ navigation }: Props) {
    const [email, setEmail] = useState("");
    const [pass, setPass] = useState("");
    const [loading, setLoading] = useState(false);

    async function onLogin() {
        setLoading(true);
        try {
            const resp = await authApi.login({ email: email.trim(), password: pass });
            if (!resp.token) throw new Error("Token gelmedi");
            await tokenStore.set(resp.token);
            navigation.replace("Main");
        } catch (e: any) {
            Alert.alert("Hata", e?.message ?? "Giriş başarısız");
        } finally {
            setLoading(false);
        }
    }

    return (
        <View style={{ flex: 1, padding: 16, justifyContent: "center" }}>
            <Text style={{ fontSize: 22, fontWeight: "800", marginBottom: 14 }}>Giriş</Text>

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
                onPress={onLogin}
                disabled={loading}
                style={{ padding: 12, backgroundColor: "#111", borderRadius: 12 }}
            >
                <Text style={{ color: "#fff", textAlign: "center", fontWeight: "700" }}>
                    {loading ? "Giriş..." : "Giriş Yap"}
                </Text>
            </Pressable>

            <Pressable onPress={() => navigation.navigate("Register")} style={{ padding: 12 }}>
                <Text style={{ textAlign: "center" }}>Kayıt ol</Text>
            </Pressable>
        </View>
    );
}
