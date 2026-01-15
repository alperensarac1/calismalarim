import React, { useState } from 'react';
import { View, Text, TextInput, Pressable, StyleSheet, Alert } from 'react-native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import {RootStackParamList, Routes} from "../navigation/routes";
import {useAuth} from "../store/auth_context";



type Props = NativeStackScreenProps<RootStackParamList, typeof Routes.REGISTER>;

export default function RegisterScreen({ navigation }: Props) {
    const { state, register } = useAuth();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    const onRegister = async () => {
        const u = username.trim();
        const p = password.trim();
        if (!u || !p) return Alert.alert('Uyarı', 'Tüm alanları doldurun');

        const res = await register(u, p);
        if (res.ok) {
            Alert.alert('Başarılı', 'Kayıt başarılı! Giriş yapabilirsiniz');
            navigation.reset({ index: 0, routes: [{ name: Routes.LOGIN }] });
        } else {
            Alert.alert('Hata', res.message || state.error || 'Sunucu hatası');
        }
    };

    return (
        <View style={styles.container}>
            <Text style={styles.title}>Kayıt Ol</Text>

            <TextInput style={styles.input} placeholder="Kullanıcı adı" value={username} onChangeText={setUsername} autoCapitalize="none" />
            <TextInput style={styles.input} placeholder="Şifre" value={password} onChangeText={setPassword} secureTextEntry />

            <Pressable style={[styles.btn, state.isLoading && { opacity: 0.6 }]} onPress={onRegister} disabled={state.isLoading}>
                <Text style={styles.btnText}>{state.isLoading ? '...' : 'Kayıt Ol'}</Text>
            </Pressable>

            <Pressable onPress={() => navigation.reset({ index: 0, routes: [{ name: Routes.LOGIN }] })} disabled={state.isLoading}>
                <Text style={styles.link}>Zaten hesabın var mı? Giriş yap</Text>
            </Pressable>
        </View>
    );
}

const styles = StyleSheet.create({
    container: { flex: 1, padding: 24, justifyContent: 'center' },
    title: { fontSize: 28, fontWeight: '800', marginBottom: 24 },
    input: { borderWidth: 1, borderColor: '#ddd', borderRadius: 12, padding: 12, marginBottom: 12 },
    btn: { backgroundColor: '#6d28d9', padding: 14, borderRadius: 12, marginTop: 8 },
    btnText: { color: 'white', fontWeight: '800', textAlign: 'center' },
    link: { marginTop: 14, color: '#6d28d9', textAlign: 'center' },
});
