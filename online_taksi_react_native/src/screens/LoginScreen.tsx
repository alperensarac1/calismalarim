import React, { useState } from "react";
import {
    ActivityIndicator,
    Alert,
    KeyboardAvoidingView,
    Platform,
    StyleSheet,
    Text,
    TextInput,
    TouchableOpacity,
    View,
} from "react-native";
import { AppRoute } from "../../App";
import { AuthRepository } from "../repositories/authRepository";
import { SessionManager } from "../core/sessionManager";

type Props = {
    onRoute: (route: AppRoute) => void;
};

export function LoginScreen({ onRoute }: Props) {
    const [phone, setPhone] = useState("");
    const [password, setPassword] = useState("");

    const [loading, setLoading] = useState(false);

    async function login() {
        if (!phone.trim() || !password.trim()) {
            Alert.alert("Bilgi", "Telefon ve şifre zorunlu");
            return;
        }

        try {
            setLoading(true);

            const response = await AuthRepository.login(phone.trim(), password.trim());

            await SessionManager.saveAuth({
                token: response.access_token,
                userId: response.user_id,
                fullName: response.full_name,
                role: response.role,
            });

            onRoute(response.role === "driver" ? "driverHome" : "customerHome");
        } catch (e) {
            Alert.alert("Hata", e instanceof Error ? e.message : "Giriş başarısız");
        } finally {
            setLoading(false);
        }
    }

    return (
        <KeyboardAvoidingView
            style={styles.container}
            behavior={Platform.OS === "ios" ? "padding" : undefined}
        >
            <Text style={styles.title}>onlinetaksi Giriş</Text>

            <TextInput
                value={phone}
                onChangeText={setPhone}
                placeholder="Telefon"
                keyboardType="phone-pad"
                style={styles.input}
            />

            <TextInput
                value={password}
                onChangeText={setPassword}
                placeholder="Şifre"
                secureTextEntry
                style={styles.input}
            />

            <TouchableOpacity
                style={[styles.primaryButton, loading && styles.disabledButton]}
                onPress={login}
                disabled={loading}
            >
                {loading ? <ActivityIndicator /> : <Text style={styles.primaryText}>Giriş Yap</Text>}
            </TouchableOpacity>

            <TouchableOpacity onPress={() => onRoute("register")} disabled={loading}>
                <Text style={styles.linkText}>Hesabın yok mu? Kayıt ol</Text>
            </TouchableOpacity>
        </KeyboardAvoidingView>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 24,
        justifyContent: "center",
    },
    title: {
        fontSize: 28,
        fontWeight: "800",
        marginBottom: 24,
    },
    input: {
        borderWidth: 1,
        borderColor: "#CCCCCC",
        borderRadius: 12,
        padding: 14,
        marginBottom: 12,
    },
    primaryButton: {
        backgroundColor: "#111827",
        borderRadius: 12,
        padding: 15,
        alignItems: "center",
        marginTop: 8,
    },
    disabledButton: {
        opacity: 0.6,
    },
    primaryText: {
        color: "white",
        fontWeight: "700",
    },
    linkText: {
        marginTop: 16,
        textAlign: "center",
        fontWeight: "700",
    },
});