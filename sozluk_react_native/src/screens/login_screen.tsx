import React, { useState } from "react";
import { Alert, Button, SafeAreaView, StyleSheet, Text, TextInput, View } from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";
import {RootStackParamList, Routes} from "../navigation/routes";
import {SozlukApi} from "../api/sozluk_api";
import {SessionManager} from "../entity/session_manager";


type Props = NativeStackScreenProps<RootStackParamList, typeof Routes.LOGIN>;

export default function LoginScreen({ navigation }: Props) {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [loading, setLoading] = useState(false);

    const login = async () => {
        if (!username.trim() || !password.trim()) {
            Alert.alert("Uyarı", "Lütfen tüm alanları doldurun");
            return;
        }
        setLoading(true);
        try {
            const res = await SozlukApi.loginUser({ username: username.trim(), password: password.trim() });
            if (res.success && res.user_id) {
                await SessionManager.saveUserSession(res.user_id, username.trim());
                navigation.reset({ index: 0, routes: [{ name: Routes.TABS }] });
            } else {
                Alert.alert("Hata", res.message ?? "Giriş başarısız");
            }
        } catch {
            Alert.alert("Hata", "Bağlantı hatası");
        } finally {
            setLoading(false);
        }
    };

    return (
        <SafeAreaView style={styles.page}>
            <Text style={styles.h1}>Giriş Yap</Text>

            <TextInput value={username} onChangeText={setUsername} placeholder="Kullanıcı adı" style={styles.input} />
            <TextInput value={password} onChangeText={setPassword} placeholder="Şifre" secureTextEntry style={styles.input} />

            <Button title={loading ? "Giriş yapılıyor..." : "Giriş Yap"} onPress={login} disabled={loading} />

            <View style={{ height: 12 }} />
            <Button title="Hesabın yok mu? Kayıt ol" onPress={() => navigation.navigate(Routes.REGISTER)} />
        </SafeAreaView>
    );
}

const styles = StyleSheet.create({
    page: { flex: 1, padding: 16, justifyContent: "center", gap: 12 },
    h1: { fontSize: 22, fontWeight: "800", textAlign: "center", marginBottom: 8 },
    input: { borderWidth: 1, borderColor: "#bbb", borderRadius: 10, padding: 12 },
});
