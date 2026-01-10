import React, { useEffect, useMemo, useState } from "react";
import { View, Text, Pressable, FlatList, StyleSheet, Image, ActivityIndicator } from "react-native";
import { useLocalSearchParams, useRouter } from "expo-router";
import {useMasaDetayStore} from "../store/masa_detay_store";
import {fiyatYaz} from "../utils/extension";


export default function MasaDetayScreen() {
    const router = useRouter();
    const { id } = useLocalSearchParams<{ id: string }>();
    const masaId = Number(id);

    // @ts-ignore
    const vm = useMasaDetayStore();
    const [seciliKategoriIndex, setSeciliKategoriIndex] = useState(0); // 0 = Tümü

    useEffect(() => {
        vm.init(masaId);
    }, [masaId]);

    const filtreliUrunler = useMemo(() => {
        if (seciliKategoriIndex === 0) return vm.tumUrunler;
        const secKat = vm.kategoriler[seciliKategoriIndex - 1];
        return vm.tumUrunler.filter((u: { urunKategori: { id: any; }; }) => u.urunKategori?.id === secKat?.id);
    }, [vm.tumUrunler, vm.kategoriler, seciliKategoriIndex]);

    return (
        <View style={styles.root}>
            {/* HEADER */}
            <View style={styles.header}>
                <Pressable onPress={() => router.back()} style={{ paddingRight: 16 }}>
                    <Text style={{ fontSize: 24 }}>←</Text>
                </Pressable>
                <Text style={{ fontSize: 20, fontWeight: "700" }}>
                    {vm.masa ? `Masa ${vm.masa.id}` : "Masa"}
                </Text>
            </View>

            <View style={styles.bodyRow}>
                {/* SOL: adisyon */}
                <View style={styles.left}>
                    <Text style={styles.leftTitle}>Masa Ürünleri</Text>

                    {vm.loading ? (
                        <ActivityIndicator />
                    ) : vm.urunler.length === 0 ? (
                        <Text style={{ color: "#777" }}>Henüz ürün eklenmemiş.</Text>
                    ) : (
                        <FlatList
                            data={vm.urunler}
                            keyExtractor={(x) => String(x.urun_id)}
                            ItemSeparatorComponent={() => <View style={{ height: 6 }} />}
                            style={{ flex: 1 }}
                            renderItem={({ item }) => (
                                <Text style={{ color: "#444" }}>
                                    {item.urun_ad} (adet: {item.adet})
                                </Text>
                            )}
                        />
                    )}

                    <View style={{ height: 12 }} />
                    <View style={{ height: 1, backgroundColor: "#ddd" }} />
                    <View style={{ height: 12 }} />

                    <Text style={{ fontSize: 18, fontWeight: "700" }}>
                        Toplam: {fiyatYaz(vm.toplamFiyat)}
                    </Text>

                    <Pressable
                        style={styles.payBtn}
                        onPress={async () => {
                            await vm.odemeAl();
                            // istersen ödeme sonrası geri:
                            // router.back();
                        }}
                    >
                        <Text style={{ color: "white", fontWeight: "800" }}>Ödeme Al</Text>
                    </Pressable>
                </View>

                {/* SAĞ: kategori + ürünler */}
                <View style={styles.right}>
                    {/* Kategori bar */}
                    <View style={styles.katBar}>
                        <Pressable onPress={() => setSeciliKategoriIndex(0)} style={styles.katItem}>
                            <Text style={[styles.katText, seciliKategoriIndex === 0 && styles.katSelected]}>Tümü</Text>
                        </Pressable>

                        <FlatList
                            horizontal
                            data={vm.kategoriler}
                            keyExtractor={(k) => String(k.id)}
                            renderItem={({ item, index }) => {
                                const idx = index + 1;
                                return (
                                    <Pressable onPress={() => setSeciliKategoriIndex(idx)} style={styles.katItem}>
                                        <Text style={[styles.katText, seciliKategoriIndex === idx && styles.katSelected]}>
                                            {item.kategori_ad}
                                        </Text>
                                    </Pressable>
                                );
                            }}
                            showsHorizontalScrollIndicator={false}
                        />
                    </View>

                    {/* Ürün grid */}
                    {vm.loading ? (
                        <ActivityIndicator />
                    ) : filtreliUrunler.length === 0 ? (
                        <Text style={{ color: "#777", textAlign: "center", marginTop: 16 }}>
                            Bu kategoride ürün yok.
                        </Text>
                    ) : (
                        <FlatList
                            data={filtreliUrunler}
                            keyExtractor={(u) => String(u.id)}
                            numColumns={3}
                            columnWrapperStyle={{ gap: 8 }}
                            contentContainerStyle={{ gap: 8, paddingBottom: 16 }}
                            renderItem={({ item }) => (
                                <View style={styles.urunCard}>
                                    <Image
                                        source={{ uri: item.urun_resim }}
                                        style={styles.urunImg}
                                        resizeMode="cover"
                                    />
                                    <Text style={styles.urunAd} numberOfLines={2}>{item.urun_ad}</Text>
                                    <Text style={{ color: "#444" }}>{item.urun_fiyat.toFixed(2)} TL</Text>

                                    <View style={styles.counterRow}>
                                        <Pressable style={styles.counterBtn} onPress={() => vm.urunEkle(item.id, 1)}>
                                            <Text style={styles.counterBtnText}>+</Text>
                                        </Pressable>

                                        <Text style={styles.counterVal}>{item.urun_adet}</Text>

                                        <Pressable
                                            style={styles.counterBtn}
                                            onPress={() => item.urun_adet > 0 && vm.urunCikar(item.id)}
                                        >
                                            <Text style={styles.counterBtnText}>−</Text>
                                        </Pressable>
                                    </View>
                                </View>
                            )}
                        />
                    )}
                </View>
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    root: { flex: 1, backgroundColor: "#FFBABA" },
    header: { height: 60, backgroundColor: "white", flexDirection: "row", alignItems: "center", paddingHorizontal: 12 },

    bodyRow: { flex: 1, flexDirection: "row", padding: 10, gap: 10 },
    left: { width: 220, backgroundColor: "white", borderRadius: 14, padding: 10 },
    leftTitle: { fontSize: 18, fontWeight: "800", marginBottom: 8 },

    payBtn: { marginTop: 10, backgroundColor: "#111827", padding: 12, borderRadius: 12, alignItems: "center" },

    right: { flex: 1 },
    katBar: { height: 56, flexDirection: "row", alignItems: "center", gap: 10, paddingHorizontal: 4 },
    katItem: { paddingHorizontal: 10, paddingVertical: 8, backgroundColor: "rgba(255,255,255,0.6)", borderRadius: 16 },
    katText: { fontWeight: "700" },
    katSelected: { color: "red" },

    urunCard: { flex: 1, backgroundColor: "white", borderRadius: 14, padding: 8, minHeight: 220 },
    urunImg: { width: "100%", height: 100, borderRadius: 10, backgroundColor: "#eee" },
    urunAd: { marginTop: 6, fontWeight: "800", textAlign: "center" },

    counterRow: { marginTop: 10, flexDirection: "row", justifyContent: "center", alignItems: "center", gap: 10 },
    counterBtn: { width: 32, height: 32, borderRadius: 10, backgroundColor: "#4f46e5", alignItems: "center", justifyContent: "center" },
    counterBtnText: { color: "white", fontWeight: "900", fontSize: 18 },
    counterVal: { width: 28, textAlign: "center", fontWeight: "800", fontSize: 16 },
});
