import React, { useState } from 'react';
import { View, Text, TextInput, Pressable, StyleSheet, Alert } from 'react-native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import {RootStackParamList, Routes} from "../navigation/routes";



type Props = NativeStackScreenProps<RootStackParamList, typeof Routes.LOGIN>;

export default function LoginScreen({ navigation }: Props) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    const onLogin = async () => {
        if (!username.trim() || !password.trim()) {
            Alert.alert('Uyarı', 'Tüm alanları doldurun');
            return;
        }

        // TODO: API çağrısı -> userId döndürdüğünü varsayalım
        const userId = 123;

        // popUpTo(LOGIN) inclusive => stack reset
        navigation.reset({
            index: 0,
            routes: [{ name: Routes.HOME, params: { userId } }],
        });
    };

    return (
        <View style={styles.container}>
            <Text style={styles.title}>Giriş Yap</Text>

            <TextInput
                style={styles.input}
                placeholder="Kullanıcı adı"
                value={username}
                onChangeText={setUsername}
                autoCapitalize="none"
            />

            <TextInput
                style={styles.input}
                placeholder="Şifre"
                value={password}
                onChangeText={setPassword}
                secureTextEntry
            />

            <Pressable style={styles.btn} onPress={onLogin}>
                <Text style={styles.btnText}>Giriş Yap</Text>
            </Pressable>

            <Pressable onPress={() => navigation.navigate(Routes.REGISTER)}>
                <Text style={styles.link}>Hesabın yok mu? Kayıt ol</Text>
            </Pressable>
        </View>
    );
}

const styles = StyleSheet.create({
    container: { flex: 1, padding: 24, justifyContent: 'center' },
    title: { fontSize: 28, fontWeight: '700', marginBottom: 24 },
    input: {
        borderWidth: 1, borderColor: '#ddd', borderRadius: 12,
        padding: 12, marginBottom: 12,
    },
    btn: { backgroundColor: '#6d28d9', padding: 14, borderRadius: 12, marginTop: 8 },
    btnText: { color: 'white', fontWeight: '700', textAlign: 'center' },
    link: { marginTop: 14, color: '#6d28d9', textAlign: 'center' },
});
