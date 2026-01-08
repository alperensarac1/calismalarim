import React, { useEffect, useMemo, useRef, useState } from "react";
import { ActivityIndicator, FlatList, SafeAreaView, StyleSheet, View } from "react-native";
import { useNavigation } from "@react-navigation/native";
import { NativeStackNavigationProp } from "@react-navigation/native-stack";
import {RootStackParamList, Routes} from "../navigation/routes";
import {Entry} from "../models/entry";
import {SozlukApi} from "../api/sozluk_api";
import SearchField from "../component/search_field";
import EntryRow from "../component/entry_row";


type Nav = NativeStackNavigationProp<RootStackParamList>;

export default function GundemScreen() {
    const navigation = useNavigation<Nav>();
    const [all, setAll] = useState<Entry[]>([]);
    const [q, setQ] = useState("");
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState<string | null>(null);

    // debounce
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
        setErr(null);
        try {
            const list = await SozlukApi.getAllEntries();
            // Kotlin'deki gibi id desc
            setAll([...list].sort((a, b) => b.id - a.id));
        } catch {
            setErr("Bağlantı hatası");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { load(); }, []);

    return (
        <SafeAreaView style={styles.page}>
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
                        <EntryRow
                            entry={item}
                            onPress={() => navigation.navigate(Routes.ENTRY_DETAIL, { id: item.id })}
                        />
                    )}
                    ListEmptyComponent={
                        err ? <View style={styles.center}><EntryEmpty /></View> : <View style={styles.center}><EntryEmpty /></View>
                    }
                    contentContainerStyle={{ paddingBottom: 24 }}
                />
            )}

            {/* FAB */}
            <View style={styles.fab}>
                <View style={styles.fabBtn}>
                    <EntryFab onPress={() => navigation.navigate(Routes.ENTRY_ADD)} />
                </View>
            </View>
        </SafeAreaView>
    );
}

function EntryEmpty() {
    return <View />;
}

function EntryFab({ onPress }: { onPress: () => void }) {
    return (
        <View style={{ borderRadius: 999, overflow: "hidden" }}>
            <View style={{ backgroundColor: "#111", paddingHorizontal: 16, paddingVertical: 12 }}>
                {/* basit */}
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    page: { flex: 1, padding: 16 },
    center: { flex: 1, alignItems: "center", justifyContent: "center" },
    fab: { position: "absolute", right: 16, bottom: 16 },
    fabBtn: { borderRadius: 999, overflow: "hidden" },
});
