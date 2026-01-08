import React, { useState } from "react";
import { Alert, Button, SafeAreaView, StyleSheet, Text, TextInput } from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";
import {RootStackParamList, Routes} from "../navigation/routes";
import {SessionManager} from "../entity/session_manager";
import {SozlukApi} from "../api/sozluk_api";


type Props = NativeStackScreenProps<RootStackParamList, typeof Routes.ENTRY_ADD>;

export default function EntryEkleScreen({ navigation }: Props) {
    const [title, setTitle] = useState("");
    const [content, setContent] = useState("");
    const [loading, setLoading] = useState(false);

    const save = async () => {
        if (!title.trim() || !content.trim()) {
            Alert.alert("Uyarı", "Tüm alanları doldurun");
            return;
        }
        setLoading(true);
        try {
            const userId = await SessionManager.getUserId();
            const res = await SozlukApi.addEntry({
                user_id: String(userId),
                title: title.trim(),
                content: content.trim(),
            });

            if (res.success) {
                Alert.alert("Başarılı", "Entry eklendi");
                navigation.goBack();
            } else {
                Alert.alert("Hata", res.message ?? "Hata oluştu");
            }
        } catch {
            Alert.alert("Hata", "Bağlantı hatası");
        } finally {
            setLoading(false);
        }
    };

    return (
        <SafeAreaView style={styles.page}>
            <Text style={styles.h1}>Entry Ekle</Text>

            <TextInput value={title} onChangeText={setTitle} placeholder="Başlık" style={styles.input} />
            <TextInput
                value={content}
                onChangeText={setContent}
                placeholder="İçerik"
                style={[styles.input, styles.textArea]}
                multiline
            />

            <Button title={loading ? "Kaydediliyor..." : "Kaydet"} onPress={save} disabled={loading} />
            <Button title="Geri" onPress={() => navigation.goBack()} />
        </SafeAreaView>
    );
}

const styles = StyleSheet.create({
    page: { flex: 1, padding: 16, gap: 12 },
    h1: { fontSize: 20, fontWeight: "800", textAlign: "center", marginBottom: 4 },
    input: { borderWidth: 1, borderColor: "#bbb", borderRadius: 10, padding: 12 },
    textArea: { minHeight: 140, textAlignVertical: "top" },
});
