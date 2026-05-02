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
} from "react-native";
import { AppRoute } from "../../App";
import { AuthRepository } from "../repositories/authRepository";
import { SessionManager } from "../core/sessionManager";

type Props = {
    onRoute: (route: AppRoute) => void;
};

export function RegisterScreen({ onRoute }: Props) {
    const [fullName, setFullName] = useState("");
    const [phone, setPhone] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const [loading, setLoading] = useState(false);

    async function register() {
        if (!fullName.trim() || !phone.trim() || !password.trim()) {
            Alert.alert("Bilgi", "Ad soyad, telefon ve şifre zorunlu");
            return;
        }

        try {
            setLoading(true);

            const response = await AuthRepository.registerCustomer({
                fullName: fullName.trim(),
                phone: phone.trim(),
                email: email.trim() || null,
                password: password.trim(),
            });

            await SessionManager.saveAuth({
                token: response.access_token,
                userId: response.user_id,
                fullName: response.full_name,
                role: response.role,
            });

            onRoute("customerHome");
        } catch (e) {
            Alert.alert("Hata", e instanceof Error ? e.message : "Kayıt başarısız");
        } finally {
            setLoading(false);
        }
    }

    return (
        <KeyboardAvoidingView
            style={styles.container}
            behavior={Platform.OS === "ios" ? "padding" : undefined}
        >
            <Text style={styles.title}>Müşteri Kayıt</Text>

            <TextInput
                value={fullName}
                onChangeText={setFullName}
                placeholder="Ad Soyad"
                style={styles.input}
            />

            <TextInput
                value={phone}
                onChangeText={setPhone}
                placeholder="Telefon"
                keyboardType="phone-pad"
                style={styles.input}
            />

            <TextInput
                value={email}
                onChangeText={setEmail}
                placeholder="Email opsiyonel"
                keyboardType="email-address"
                autoCapitalize="none"
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
                onPress={register}
                disabled={loading}
            >
                {loading ? <ActivityIndicator /> : <Text style={styles.primaryText}>Kayıt Ol</Text>}
            </TouchableOpacity>

            <TouchableOpacity onPress={() => onRoute("login")} disabled={loading}>
                <Text style={styles.linkText}>Geri Dön</Text>
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