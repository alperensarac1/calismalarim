import React, {useState} from 'react';
import {
    Alert,
    KeyboardAvoidingView,
    Platform,
    ScrollView,
    StyleSheet,
    Text,
    View,
} from 'react-native';
import {NativeStackScreenProps} from '@react-navigation/native-stack';

import {ApiService} from '../../core/apiService';
import {AppColors} from '../../core/appColors';
import {SessionManager} from '../../core/sessionManager';
import {AppButton} from '../../components/AppButton';
import {AppTextField} from '../../components/AppTextField';
import {RootStackParamList} from '../../navigation/routes';

type Props = NativeStackScreenProps<RootStackParamList, 'Login'>;

export function LoginScreen({navigation}: Props) {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    const [loading, setLoading] = useState(false);

    function isValidEmail(value: string): boolean {
        return /^\S+@\S+\.\S+$/.test(value);
    }

    function showMessage(message: string) {
        Alert.alert('Uyarı', message);
    }

    async function login() {
        const cleanEmail = email.trim();
        const cleanPassword = password.trim();

        if (cleanEmail.length === 0) {
            showMessage('E-posta zorunludur.');
            return;
        }

        if (!isValidEmail(cleanEmail)) {
            showMessage('Geçerli bir e-posta giriniz.');
            return;
        }

        if (cleanPassword.length === 0) {
            showMessage('Şifre zorunludur.');
            return;
        }

        if (cleanPassword.length < 6) {
            showMessage('Şifre en az 6 karakter olmalıdır.');
            return;
        }

        try {
            setLoading(true);

            const response = await ApiService.login({
                email: cleanEmail,
                password: cleanPassword,
            });

            setLoading(false);

            if (!response.success) {
                showMessage(response.message);
                return;
            }

            if (!response.data) {
                showMessage('Kullanıcı bilgisi alınamadı.');
                return;
            }

            await SessionManager.saveUser(response.data);

            navigation.reset({
                index: 0,
                routes: [{name: 'Home'}],
            });
        } catch (error) {
            setLoading(false);

            const message =
                error instanceof Error ? error.message : 'Bilinmeyen hata oluştu.';

            showMessage(message);
        }
    }

    function goRegister() {
        navigation.navigate('Register');
    }

    return (
        <KeyboardAvoidingView
            style={styles.root}
            behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
            <ScrollView
                contentContainerStyle={styles.scrollContent}
                keyboardShouldPersistTaps="handled">
                <View style={styles.header}>
                    <Text style={styles.icon}>🎟️</Text>

                    <Text style={styles.title}>Etkinlik Bileti</Text>

                    <Text style={styles.subtitle}>
                        Etkinlikleri keşfet, biletini QR kodla kullan.
                    </Text>
                </View>

                <View style={styles.card}>
                    <Text style={styles.cardTitle}>Giriş Yap</Text>

                    <View style={styles.inputGap}>
                        <AppTextField
                            value={email}
                            placeholder="E-posta"
                            keyboardType="email-address"
                            onChangeText={setEmail}
                        />
                    </View>

                    <View style={styles.inputGap}>
                        <AppTextField
                            value={password}
                            placeholder="Şifre"
                            secureTextEntry
                            returnKeyType="done"
                            onChangeText={setPassword}
                        />
                    </View>

                    <AppButton
                        title="Giriş Yap"
                        loading={loading}
                        backgroundColor={AppColors.blue}
                        onPress={login}
                        style={styles.button}
                    />

                    <Text onPress={loading ? undefined : goRegister} style={styles.link}>
                        Hesabın yok mu? Kayıt ol
                    </Text>
                </View>
            </ScrollView>
        </KeyboardAvoidingView>
    );
}

const styles = StyleSheet.create({
    root: {
        flex: 1,
        backgroundColor: AppColors.background,
    },
    scrollContent: {
        flexGrow: 1,
        justifyContent: 'center',
        padding: 24,
    },
    header: {
        alignItems: 'center',
        marginBottom: 28,
    },
    icon: {
        fontSize: 58,
        marginBottom: 10,
    },
    title: {
        fontSize: 32,
        fontWeight: '800',
        color: AppColors.darkText,
        textAlign: 'center',
    },
    subtitle: {
        marginTop: 8,
        fontSize: 15,
        color: AppColors.grayText,
        textAlign: 'center',
        lineHeight: 21,
    },
    card: {
        width: '100%',
        backgroundColor: AppColors.cardBackground,
        borderRadius: 22,
        padding: 20,
        shadowColor: '#000000',
        shadowOpacity: 0.08,
        shadowRadius: 14,
        shadowOffset: {
            width: 0,
            height: 6,
        },
        elevation: 5,
    },
    cardTitle: {
        fontSize: 24,
        fontWeight: '800',
        color: AppColors.darkText,
        marginBottom: 18,
    },
    inputGap: {
        marginBottom: 12,
    },
    button: {
        marginTop: 6,
    },
    link: {
        marginTop: 16,
        textAlign: 'center',
        color: AppColors.blue,
        fontSize: 15,
        fontWeight: '700',
    },
});