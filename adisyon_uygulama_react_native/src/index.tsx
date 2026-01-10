import React, { useEffect } from "react";
import { View, Text, Pressable, FlatList, StyleSheet, ActivityIndicator } from "react-native";
import { useRouter } from "expo-router";
import {useMasalarStore} from "./store/masalar_store";
import {fiyatYaz} from "./utils/extension";

export default function MainScreen() {
    const router = useRouter();
    // @ts-ignore
    const { masalar, loading, masalariYukle } = useMasalarStore();

    useEffect(() => {
        masalariYukle();
    }, []);

    const acikMasalar = masalar.filter((m: { acik_mi: number; }) => m.acik_mi === 1);

    return (
        <View style={styles.root}>
            <View style={styles.row}>
                <View style={{ flex: 1 }}>
                    <Text style={styles.redTitle}>{acikMasalar.length} adet masa açık</Text>

                    {loading ? (
                        <ActivityIndicator />
                    ) : (
                        <FlatList
                            data={acikMasalar}
                            keyExtractor={(m) => String(m.id)}
                            ItemSeparatorComponent={() => <View style={{ height: 8 }} />}
                            renderItem={({ item }) => (
                                <Pressable style={styles.card} onPress={() => router.push(`/masa/${item.id}`)}>
                                    <Text style={styles.cardTitle}>Masa {item.id}</Text>
                                    <Text>Tutar: {fiyatYaz(item.toplam_fiyat)}</Text>
                                </Pressable>
                            )}
                        />
                    )}
                </View>

                <View style={{ flex: 2 }}>
                    <FlatList
                        data={masalar}
                        keyExtractor={(m) => String(m.id)}
                        numColumns={3}
                        columnWrapperStyle={{ gap: 8 }}
                        contentContainerStyle={{ gap: 8 }}
                        renderItem={({ item }) => (
                            <Pressable style={styles.gridCard} onPress={() => router.push(`/masa/${item.id}`)}>
                                <Text style={{ fontWeight: "700" }}>Masa {item.id}</Text>
                                <Text style={{ color: "#444" }}>{fiyatYaz(item.toplam_fiyat)}</Text>
                                <Text style={{ color: "#777" }}>{item.sure}</Text>
                            </Pressable>
                        )}
                    />
                </View>
            </View>

            <View style={styles.bottomRow}>
                <Pressable style={styles.btn} onPress={masalariYukle}>
                    <Text style={styles.btnText}>Yenile</Text>
                </Pressable>
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    root: { flex: 1, backgroundColor: "#FFBABA", padding: 10 },
    row: { flex: 1, flexDirection: "row", gap: 10 },

    redTitle: {
        textAlign: "center",
        color: "red",
        fontWeight: "700",
        marginBottom: 8,
        paddingVertical: 6,
        backgroundColor: "rgba(255,255,255,0.5)",
        borderRadius: 10,
    },

    card: { backgroundColor: "white", padding: 12, borderRadius: 14 },
    cardTitle: { fontWeight: "700", marginBottom: 4 },

    gridCard: {
        flex: 1,
        backgroundColor: "white",
        padding: 12,
        borderRadius: 16,
        minHeight: 90,
    },

    bottomRow: { flexDirection: "row", gap: 8, paddingTop: 8 },
    btn: { flex: 1, backgroundColor: "#4f46e5", padding: 12, borderRadius: 12, alignItems: "center" },
    btnText: { color: "white", fontWeight: "700" },
});
