// src/screens/PrestigeShopScreen.tsx
import React, { useMemo } from "react";
import { View, Text, StyleSheet, FlatList, Pressable } from "react-native";
import type { NativeStackScreenProps } from "@react-navigation/native-stack";
import {useGameStore} from "../data/game_store";
import {PerkUi} from "../model/perk_ui";
import {RootStackParamList} from "../../App";



export default function PrestigeShopScreen({ navigation }: NativeStackScreenProps<RootStackParamList, "Shop">) {
    const perks = useGameStore(s => s.perks);
    const buyPerk = useGameStore(s => s.buyPerk);

    const items = useMemo<PerkUi[]>(() => {
        return [
            { key: "gprod", title: "Altın Çırpıcı", desc: "%5 üretim çarpanı / seviye (CPS & tap)", baseCost: 1, scaling: 1.6, level: perks.gprod, maxLevel: Number.MAX_SAFE_INTEGER },
            { key: "crit", title: "Uğurlu Tılsım", desc: "%1 pasif crit şansı / seviye (tap x3)", baseCost: 2, scaling: 1.7, level: perks.crit, maxLevel: Number.MAX_SAFE_INTEGER },
            { key: "discount", title: "Toplu Alım", desc: "Upgrade fiyatlarında %2 indirim / seviye (maks %50)", baseCost: 3, scaling: 1.8, level: perks.discount, maxLevel: 25 },
            { key: "tapTop", title: "Turbo Tap", desc: "Kalıcı +1 tap gücü / seviye", baseCost: 2, scaling: 1.5, level: perks.tapTop, maxLevel: Number.MAX_SAFE_INTEGER },
        ];
    }, [perks.crit, perks.discount, perks.gprod, perks.tapTop]);

    return (
        <View style={styles.root}>
            <View style={styles.headerRow}>
                <Pressable onPress={() => navigation.goBack()} style={styles.backBtn}>
                    <Text style={styles.backText}>Geri</Text>
                </Pressable>
                <Text style={styles.title}>Prestige Mağazası</Text>
                <View style={{ width: 60 }} />
            </View>

            <Text style={styles.points}>Prestige Puanı: {perks.points}</Text>

            <FlatList
                data={items}
                keyExtractor={(p) => p.key}
                contentContainerStyle={{ padding: 16, paddingBottom: 24 }}
                renderItem={({ item }) => {
                    const cost = costForNext(item.baseCost, item.scaling, item.level);
                    const can = perks.points >= cost && item.level < (item.maxLevel ?? Number.MAX_SAFE_INTEGER);

                    return (
                        <View style={styles.card}>
                            <Text style={styles.cardTitle}>
                                {item.title} (Lv {item.level})
                            </Text>
                            <Text style={styles.cardDesc}>{item.desc}</Text>

                            <View style={styles.row}>
                                <Text style={styles.cost}>Maliyet: {cost}</Text>
                                <Pressable
                                    onPress={() => buyPerk(item.key, cost, item.maxLevel)}
                                    disabled={!can}
                                    style={[styles.buy, !can && styles.disabled]}
                                >
                                    <Text style={styles.buyText}>Satın Al</Text>
                                </Pressable>
                            </View>
                        </View>
                    );
                }}
            />
        </View>
    );
}

function costForNext(base: number, scaling: number, level: number): number {
    const v = Math.floor(base * Math.pow(scaling, level));
    return Math.max(v, base);
}

const styles = StyleSheet.create({
    root: { flex: 1, backgroundColor: "#FFF3E5" },
    headerRow: {
        paddingTop: 14,
        paddingBottom: 10,
        paddingHorizontal: 12,
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
    },
    backBtn: { width: 60, paddingVertical: 6 },
    backText: { fontWeight: "800" },
    title: { fontSize: 16, fontWeight: "900" },

    points: { paddingHorizontal: 16, paddingBottom: 6, fontSize: 16, fontWeight: "700" },

    card: {
        backgroundColor: "#fff",
        borderRadius: 14,
        padding: 12,
        marginVertical: 6,
        shadowColor: "#000",
        shadowOpacity: 0.05,
        shadowRadius: 6,
        elevation: 1,
    },
    cardTitle: { fontSize: 15, fontWeight: "800" },
    cardDesc: { marginTop: 4, color: "#444" },
    row: { marginTop: 10, flexDirection: "row", alignItems: "center", justifyContent: "space-between" },
    cost: { fontWeight: "800" },
    buy: { paddingVertical: 8, paddingHorizontal: 14, borderRadius: 10, borderWidth: 1, borderColor: "#222" },
    buyText: { fontWeight: "900" },
    disabled: { opacity: 0.4 },
});
