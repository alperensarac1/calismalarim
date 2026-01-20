import React, { useEffect, useState } from "react";
import { ActivityIndicator, FlatList, Pressable, Text, View } from "react-native";

import { CartDto, CartItemDto } from "../api/types";
import { useNavigation } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import {RootStackParamList} from "../navigation/app_navigator";
import {cartApi} from "../api/cart_api";
import {orderApi} from "../api/order_api";
import axios from "axios";
export default function CartScreen() {
    const nav = useNavigation<NativeStackNavigationProp<RootStackParamList>>();

    const [cart, setCart] = useState<CartDto | null>(null);
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState<string | null>(null);

    async function load() {
        setErr(null);
        setLoading(true);
        try {
            const c = await cartApi.getCart();
            setCart(c);
        } catch (e: any) {
            setErr(e?.message ?? "Hata");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        load();
    }, []);

    async function inc(it: CartItemDto) {
        await cartApi.updateItem(it.item_id, { quantity: it.quantity + 1 });
        load();
    }
    async function dec(it: CartItemDto) {
        const q = Math.max(1, it.quantity - 1);
        await cartApi.updateItem(it.item_id, { quantity: q });
        load();
    }
    async function del(it: CartItemDto) {
        await cartApi.deleteItem(it.item_id);
        load();
    }

    async function checkout() {
        setLoading(true);
        try {
            const resp = await orderApi.checkout({
                address_name: "Ev",
                address_line1: "Test Mah Test Sok No:1",
                address_line2: null, // opsiyonel
                city: "Istanbul",
                district: "Kadikoy",
                postal_code: "34000",
            });

            nav.navigate("OrderDetail", { id: resp.order_id });
        } catch (e: any) {
            setErr(e?.message ?? "Hata");
        } finally {
            setLoading(false);
        }
    }

    return (
        <View style={{ flex: 1, padding: 12 }}>
            <Text style={{ fontSize: 20, fontWeight: "700", marginBottom: 8 }}>Sepet</Text>

            {err ? <Text style={{ color: "red", marginBottom: 6 }}>{err}</Text> : null}
            {loading && !cart ? <ActivityIndicator /> : null}

            <FlatList
                data={cart?.items ?? []}
                keyExtractor={(x) => String(x.item_id)}
                contentContainerStyle={{ gap: 10, paddingBottom: 20 }}
                renderItem={({ item }) => (
                    <View style={{ borderWidth: 1, borderRadius: 12, padding: 10 }}>
                        <Text style={{ fontWeight: "700" }}>{item.name}</Text>
                        <Text style={{ marginTop: 4 }}>₺{item.sale_price}</Text>

                        <View style={{ flexDirection: "row", gap: 10, marginTop: 10, alignItems: "center" }}>
                            <Pressable onPress={() => dec(item)} style={{ padding: 10, borderWidth: 1, borderRadius: 10 }}>
                                <Text>-</Text>
                            </Pressable>
                            <Text style={{ fontWeight: "700" }}>{item.quantity}</Text>
                            <Pressable onPress={() => inc(item)} style={{ padding: 10, borderWidth: 1, borderRadius: 10 }}>
                                <Text>+</Text>
                            </Pressable>

                            <View style={{ flex: 1 }} />
                            <Pressable onPress={() => del(item)} style={{ padding: 10 }}>
                                <Text style={{ color: "red" }}>Sil</Text>
                            </Pressable>
                        </View>
                    </View>
                )}
            />

            <View style={{ borderTopWidth: 1, paddingTop: 10 }}>
                <Text>Ürün: {cart?.total_items ?? 0}</Text>
                <Text style={{ fontWeight: "800" }}>Toplam: ₺{(cart?.total ?? 0).toFixed(2)}</Text>

                <Pressable
                    onPress={checkout}
                    disabled={!cart || cart.items.length === 0 || loading}
                    style={{
                        marginTop: 10,
                        padding: 12,
                        backgroundColor: !cart || cart.items.length === 0 ? "#999" : "#111",
                        borderRadius: 10,
                    }}
                >
                    <Text style={{ color: "#fff", textAlign: "center" }}>{loading ? "..." : "Checkout"}</Text>
                </Pressable>
            </View>
        </View>
    );
}
