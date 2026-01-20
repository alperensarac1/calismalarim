import React, { useEffect, useMemo, useState } from "react";
import {ActivityIndicator, FlatList, Image, Pressable, Text, TextInput, View} from "react-native";
import { CategoryDto, ProductListDto } from "../api/types";
import { useNavigation } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import {productApi} from "../api/product_api";
import {RootStackParamList} from "../navigation/app_navigator";


export default function HomeScreen() {
    const nav = useNavigation<NativeStackNavigationProp<RootStackParamList>>();

    const [categories, setCategories] = useState<CategoryDto[]>([]);
    const [items, setItems] = useState<ProductListDto[]>([]);
    const [page, setPage] = useState(1);
    const [total, setTotal] = useState(0);
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState<string | null>(null);

    const [q, setQ] = useState("");
    const [cat, setCat] = useState<number | null>(null);
    const [discount, setDiscount] = useState(false);
    const [sort, setSort] = useState<"newest" | "price_asc" | "price_desc">("newest");

    const hasMore = useMemo(() => (total === 0 ? true : items.length < total), [items.length, total]);

    useEffect(() => {
        (async () => {
            try {
                const cs = await productApi.getCategories();
                setCategories(cs);
            } catch {}
        })();
        reload();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    async function reload() {
        setErr(null);
        setLoading(true);
        try {
            const resp = await productApi.getProducts({
                cat: cat ?? undefined,
                q: q.trim() || undefined,
                discount: discount ? 1 : 0,
                sort,
                page: 1,
                per: 12,
            });
            setItems(resp.items);
            setTotal(resp.total);
            setPage(2);
        } catch (e: any) {
            setErr(e?.message ?? "Hata");
        } finally {
            setLoading(false);
        }
    }

    async function loadNext() {
        if (loading || !hasMore) return;
        setLoading(true);
        try {
            const resp = await productApi.getProducts({
                cat: cat ?? undefined,
                q: q.trim() || undefined,
                discount: discount ? 1 : 0,
                sort,
                page,
                per: 12,
            });
            // append + distinct
            const merged = [...items, ...resp.items];
            const map = new Map<number, ProductListDto>();
            merged.forEach((p) => map.set(p.id, p));
            setItems(Array.from(map.values()));
            setTotal(resp.total);
            setPage(page + 1);
        } catch (e: any) {
            setErr(e?.message ?? "Hata");
        } finally {
            setLoading(false);
        }
    }

    return (
        <View style={{ flex: 1, padding: 12 }}>
            <TextInput
                placeholder="Ara"
                value={q}
                onChangeText={setQ}
                style={{ borderWidth: 1, padding: 10, borderRadius: 10, marginBottom: 8 }}
            />

            <Pressable
                onPress={reload}
                style={{ padding: 10, backgroundColor: "#111", borderRadius: 10, marginBottom: 10 }}
            >
                <Text style={{ color: "#fff", textAlign: "center" }}>Ara / Yenile</Text>
            </Pressable>

            <FlatList
                data={[{ id: -1, name: "Tümü" } as CategoryDto, ...categories]}
                horizontal
                keyExtractor={(x) => String(x.id)}
                showsHorizontalScrollIndicator={false}
                style={{ marginBottom: 10 }}
                renderItem={({ item }) => (
                    <Pressable
                        onPress={() => {
                            setCat(item.id === -1 ? null : item.id);
                            // filtre değişince reload
                            setTimeout(reload, 0);
                        }}
                        style={{
                            paddingVertical: 8,
                            paddingHorizontal: 12,
                            borderWidth: 1,
                            borderRadius: 999,
                            marginRight: 8,
                            width:75,
                            height:40
                        }}
                    >
                        <Text>{item.name}</Text>
                    </Pressable>
                )}
            />

            {err ? <Text style={{ color: "red", marginBottom: 6 }}>{err}</Text> : null}
            {loading ? <ActivityIndicator style={{ marginBottom: 6 }} /> : null}

            <FlatList
                data={items}
                keyExtractor={(x) => String(x.id)}
                numColumns={2}
                columnWrapperStyle={{ gap: 10 }}
                contentContainerStyle={{ gap: 10, paddingBottom: 20 }}
                onEndReachedThreshold={0.3}
                onEndReached={loadNext}
                renderItem={({ item }) => (
                    <Pressable
                        onPress={() => nav.navigate("ProductDetail", { id: item.id })}
                        style={{ flex: 1, borderWidth: 1, borderRadius: 12, padding: 10 }}
                    >
                        {item.image_url ? (
                            <Image
                                source={{ uri: item.image_url }}
                                style={{ width: "100%", height: 110, borderRadius: 10, marginBottom: 8 }}
                                resizeMode="cover"
                            />
                        ) : (
                            <View style={{ width: "100%", height: 110, borderRadius: 10, marginBottom: 8, backgroundColor: "#eee" }} />
                        )}

                        <Text numberOfLines={2} style={{ fontWeight: "700" }}>{item.name}</Text>

                        <Text style={{ marginTop: 6, fontWeight: "800" }}>₺{item.price})</Text>

                        {item.discount_percent ? (
                            <Text style={{ marginTop: 4, color: "green" }}>
                                İndirim: %{item.discount_percent}
                            </Text>
                        ) : null}

                        <Text style={{ marginTop: 4, opacity: 0.7 }}>
                            Stok: {item.stock_qty} {item.is_active === 1 ? "" : "(Pasif)"}
                        </Text>
                    </Pressable>
                )}
            />
        </View>
    );
}
