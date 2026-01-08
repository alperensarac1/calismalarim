import React, { useState } from "react";
import { Alert, Button, SafeAreaView, StyleSheet, Text, TextInput } from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";
import {RootStackParamList, Routes} from "../navigation/routes";
import {SozlukApi} from "../api/sozluk_api";


type Props = NativeStackScreenProps<RootStackParamList, typeof Routes.REGISTER>;

export default function RegisterScreen({ navigation }: Props) {
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [loading, setLoading] = useState(false);

    const register = async () => {
        if (!username.trim() || !email.trim() || !password.trim()) {
            Alert.alert("Uyarı", "Lütfen tüm alanları doldurun");
            return;
        }
        setLoading(true);
        try {
            const res = await SozlukApi.registerUser({
                username: username.trim(),
                email: email.trim(),
                password: password.trim(),
            });

            if (res.success) {
                Alert.alert("Başarılı", "Kayıt başarılı. Giriş yapabilirsiniz.");
                navigation.goBack();
            } else {
                Alert.alert("Hata", res.message ?? "Kayıt başarısız");
            }
        } catch {
            Alert.alert("Hata", "Bağlantı hatası");
        } finally {
            setLoading(false);
        }
    };

    return (
        <SafeAreaView style={styles.page}>
            <Text style={styles.h1}>Kayıt Ol</Text>

            <TextInput value={username} onChangeText={setUsername} placeholder="Kullanıcı adı" style={styles.input} />
            <TextInput value={email} onChangeText={setEmail} placeholder="E-posta" style={styles.input} autoCapitalize="none" />
            <TextInput value={password} onChangeText={setPassword} placeholder="Şifre" secureTextEntry style={styles.input} />

            <Button title={loading ? "Kayıt yapılıyor..." : "Kayıt Ol"} onPress={register} disabled={loading} />
            <Button title="Zaten hesabın var mı? Giriş yap" onPress={() => navigation.goBack()} />
        </SafeAreaView>
    );
}

const styles = StyleSheet.create({
    page: { flex: 1, padding: 16, justifyContent: "center", gap: 12 },
    h1: { fontSize: 22, fontWeight: "800", textAlign: "center", marginBottom: 8 },
    input: { borderWidth: 1, borderColor: "#bbb", borderRadius: 10, padding: 12 },
});
