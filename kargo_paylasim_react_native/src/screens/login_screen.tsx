import React, { useState } from "react";
import { Alert, Button, Text, TextInput, View } from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";

import { Endpoints } from "../api/endpoints";
import { postJSON, APIError } from "../api/client";
import type { ApiResp, LoginData } from "../api/types";
import {RootStackParamList} from "../navigation/app_navigator";
import {tokenStore} from "../storage/token_store";

type Props = NativeStackScreenProps<RootStackParamList, "Login">;

export default function LoginScreen({ navigation }: Props) {
    const [phone, setPhone] = useState("");
    const [password, setPassword] = useState("");
    const [loading, setLoading] = useState(false);

    async function onLogin() {
        if (!phone.trim() || !password) {
            Alert.alert("Uyarı", "Telefon ve şifre zorunlu.");
            return;
        }

        setLoading(true);
        try {
            const res = await postJSON<LoginData>(Endpoints.login, { phone: phone.trim(), password });
            if (!res.ok || !res.data) throw new APIError(res.error ?? "Invalid credentials");

            await tokenStore.set(res.data.token);

            // en basit: stack'i resetle
            navigation.reset({ index: 0, routes: [{ name: "Home" }] });
        } catch (e: any) {
            Alert.alert("Hata", e?.message ?? String(e));
        } finally {
            setLoading(false);
        }
    }

    return (
        <View style={{ padding: 16, gap: 10 }}>
            <TextInput
                placeholder="Telefon (05xx... veya +905xx...)"
                value={phone}
                onChangeText={setPhone}
                keyboardType="phone-pad"
                style={{ borderWidth: 1, borderColor: "#ccc", padding: 12, borderRadius: 10 }}
            />
            <TextInput
                placeholder="Şifre"
                value={password}
                onChangeText={setPassword}
                secureTextEntry
                style={{ borderWidth: 1, borderColor: "#ccc", padding: 12, borderRadius: 10 }}
            />

            <Button title={loading ? "..." : "Giriş Yap"} onPress={onLogin} disabled={loading} />
            <Button title="Kayıt Ol" onPress={() => navigation.navigate("Register")} />
        </View>
    );
}
