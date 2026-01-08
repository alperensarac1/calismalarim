import React, { useEffect, useMemo, useRef, useState } from "react";
import { ActivityIndicator, Alert, FlatList, SafeAreaView, StyleSheet, Text, View, Button } from "react-native";
import { useNavigation } from "@react-navigation/native";
import { NativeStackNavigationProp } from "@react-navigation/native-stack";
import {RootStackParamList, Routes} from "../navigation/routes";
import {Entry} from "../models/entry";
import {SessionManager} from "../entity/session_manager";
import {SozlukApi} from "../api/sozluk_api";
import EntryRow from "../component/entry_row";
import SearchField from "../component/search_field";



type Nav = NativeStackNavigationProp<RootStackParamList>;

export default function ProfilScreen() {
    const navigation = useNavigation<Nav>();
    const [all, setAll] = useState<Entry[]>([]);
    const [q, setQ] = useState("");
    const [loading, setLoading] = useState(true);
    const [username, setUsername] = useState("Profil");
    const [userId, setUserId] = useState(-1);

    const t = useRef<ReturnType<typeof setTimeout> | null>(null);
    const [dq, setDq] = useState("");
    useEffect(() => {
        if (t.current) clearTimeout(t.current);
        t.current = setTimeout(() => setDq(q), 300);
        return () => { if (t.current) clearTimeout(t.current); };
    }, [q]);

    const entries = useMemo(() => {
        if (!dq.trim()) return all;
        const s = dq.toLowerCase();
        return all.filter(e => e.title.toLowerCase().includes(s));
    }, [all, dq]);

    const load = async () => {
        setLoading(true);
        try {
            const uid = await SessionManager.getUserId();
            const uname = await SessionManager.getUsername();
            setUserId(uid);
            setUsername(uname ?? "Bilinmeyen Kullanıcı");

            const list = await SozlukApi.getEntriesByUser(uid);
            setAll(list ?? []);
        } catch {
            Alert.alert("Hata", "Bağlantı hatası");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { load(); }, []);

    const logout = async () => {
        Alert.alert("Çıkış Yap", "Oturumu kapatmak istiyor musunuz?", [
            { text: "İptal", style: "cancel" },
            {
                text: "Evet",
                onPress: async () => {
                    await SessionManager.clearSession();
                    navigation.reset({ index: 0, routes: [{ name: Routes.LOGIN }] });
                },
            },
        ]);
    };

    const deleteEntry = async (entryId: number) => {
        Alert.alert("Sil", "Entry silinsin mi?", [
            { text: "İptal", style: "cancel" },
            {
                text: "Sil",
                style: "destructive",
                onPress: async () => {
                    try {
                        const res = await SozlukApi.deleteEntry({ entry_id: entryId });
                        if (res.success) {
                            await load();
                        } else {
                            Alert.alert("Hata", res.message ?? "Silinemedi");
                        }
                    } catch {
                        Alert.alert("Hata", "Bağlantı hatası");
                    }
                },
            },
        ]);
    };

    return (
        <SafeAreaView style={styles.page}>
            <View style={styles.headerRow}>
                <Text style={styles.h1}>{username}</Text>
                <Button title="Çıkış" onPress={logout} />
            </View>

            <SearchField value={q} onChangeText={setQ} />
            <View style={{ height: 12 }} />

            {loading ? (
                <View style={styles.center}><ActivityIndicator /></View>
            ) : (
                <FlatList
                    data={entries}
                    keyExtractor={(e) => String(e.id)}
                    ItemSeparatorComponent={() => <View style={{ height: 8 }} />}
                    renderItem={({ item }) => (
                        <View style={styles.row}>
                            <View style={{ flex: 1 }}>
                                <EntryRow entry={item} onPress={() => navigation.navigate(Routes.ENTRY_DETAIL, { id: item.id })} />
                            </View>
                            <View style={{ width: 10 }} />
                            <Button title="Sil" onPress={() => deleteEntry(item.id)} />
                        </View>
                    )}
                    ListEmptyComponent={<Text>Henüz entry yok</Text>}
                    contentContainerStyle={{ paddingBottom: 24 }}
                />
            )}
        </SafeAreaView>
    );
}

const styles = StyleSheet.create({
    page: { flex: 1, padding: 16 },
    center: { flex: 1, alignItems: "center", justifyContent: "center" },
    headerRow: { flexDirection: "row", alignItems: "center", justifyContent: "space-between", marginBottom: 10 },
    h1: { fontSize: 18, fontWeight: "800" },
    row: { flexDirection: "row", alignItems: "center" },
});
