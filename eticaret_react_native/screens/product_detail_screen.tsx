import React, { useEffect, useState } from "react";
import {ActivityIndicator, Image, Pressable, Text, View} from "react-native";

import { ProductDto } from "../api/types";
import type { NativeStackScreenProps } from "@react-navigation/native-stack";
import {RootStackParamList} from "../navigation/app_navigator";
import {productApi} from "../api/product_api";
import {cartApi} from "../api/cart_api";


type Props = NativeStackScreenProps<RootStackParamList, "ProductDetail">;

export default function ProductDetailScreen({ route }: Props) {
    const { id } = route.params;
    const [p, setP] = useState<ProductDto | null>(null);
    const [qty, setQty] = useState(1);
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState<string | null>(null);

    useEffect(() => {
        (async () => {
            setLoading(true);
            try {
                const pr = await productApi.getProduct(id);
                setP(pr);
            } catch (e: any) {
                setErr(e?.message ?? "Hata");
            } finally {
                setLoading(false);
            }
        })();
    }, [id]);

    async function add() {
        setErr(null);
        setLoading(true);
        try {
            await cartApi.addToCart({ product_id: id, quantity: qty });
        } catch (e: any) {
            setErr(e?.message ?? "Hata");
        } finally {
            setLoading(false);
        }
    }

    if (loading && !p) return <View style={{ flex: 1, justifyContent: "center" }}><ActivityIndicator /></View>;

    return (
        <View style={{ flex: 1, padding: 12 }}>
            <Image
                source={{ uri: p?.image_url  ?? ""}}
                style={{ width: "100%", height: 110, borderRadius: 10, marginBottom: 8 }}
                resizeMode="cover"
            />
            {err ? <Text style={{ color: "red", marginBottom: 8 }}>{err}</Text> : null}

            <Text style={{ fontSize: 18, fontWeight: "700" }}>{p?.name}</Text>
            <Text style={{ marginTop: 6, fontWeight: "800" }}>₺{p?.price}</Text>
            {p?.discount_percent ? <Text style={{ color: "green" }}>İndirim: %{p.discount_percent}</Text> : null}
            <Text style={{ opacity: 0.7 }}>Stok: {p?.stock_qty}</Text>

            <View style={{ flexDirection: "row", gap: 10, marginTop: 16 }}>
                <Pressable onPress={() => setQty(Math.max(1, qty - 1))} style={{ padding: 10, borderWidth: 1, borderRadius: 10 }}>
                    <Text>-</Text>
                </Pressable>
                <Text style={{ paddingTop: 10, fontWeight: "700" }}>{qty}</Text>
                <Pressable onPress={() => setQty(Math.min(99, qty + 1))} style={{ padding: 10, borderWidth: 1, borderRadius: 10 }}>
                    <Text>+</Text>
                </Pressable>
            </View>

            <Pressable
                onPress={add}
                style={{ marginTop: 16, padding: 12, backgroundColor: "#111", borderRadius: 10 }}
            >
                <Text style={{ color: "#fff", textAlign: "center" }}>
                    {loading ? "Ekleniyor..." : "Sepete Ekle"}
                </Text>
            </Pressable>
        </View>
    );
}
