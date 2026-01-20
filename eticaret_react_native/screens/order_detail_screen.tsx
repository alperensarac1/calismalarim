import React, { useEffect, useState } from "react";
import { ActivityIndicator, FlatList, Text, View } from "react-native";
import { OrderDetailDto } from "../api/types";
import type { NativeStackScreenProps } from "@react-navigation/native-stack";
import {RootStackParamList} from "../navigation/app_navigator";
import {orderApi} from "../api/order_api";

type Props = NativeStackScreenProps<RootStackParamList, "OrderDetail">;

export default function OrderDetailScreen({ route }: Props) {
    const { id } = route.params;
    const [d, setD] = useState<OrderDetailDto | null>(null);
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState<string | null>(null);

    useEffect(() => {
        (async () => {
            setLoading(true);
            try {
                const detail = await orderApi.getOrderDetail(id);
                setD(detail);
            } catch (e: any) {
                setErr(e?.message ?? "Hata");
            } finally {
                setLoading(false);
            }
        })();
    }, [id]);

    if (loading && !d) return <View style={{ flex: 1, justifyContent: "center" }}><ActivityIndicator /></View>;

    return (
        <View style={{ flex: 1, padding: 12 }}>
            {err ? <Text style={{ color: "red" }}>{err}</Text> : null}
            <Text style={{ fontSize: 20, fontWeight: "700" }}>Sipariş #{id}</Text>
            {d ? (
                <>
                    <Text>Durum: {d.status}</Text>
                    <Text style={{ fontWeight: "800" }}>Toplam: {d.currency} {d.total_amount.toFixed(2)}</Text>
                    <Text style={{ marginTop: 8, fontWeight: "700" }}>Ürünler</Text>

                    <FlatList
                        data={d.items}
                        keyExtractor={(_, i) => String(i)}
                        contentContainerStyle={{ gap: 10, paddingBottom: 20, marginTop: 8 }}
                        renderItem={({ item }) => (
                            <View style={{ borderWidth: 1, borderRadius: 12, padding: 10 }}>
                                <Text style={{ fontWeight: "700" }}>{item.name}</Text>
                                <Text>Adet: {item.quantity}</Text>
                                <Text>Birim: ₺{item.unit_price.toFixed(2)}</Text>
                                <Text>Satır: ₺{item.line_total.toFixed(2)}</Text>
                            </View>
                        )}
                    />
                </>
            ) : (
                <Text>Detay yok</Text>
            )}
        </View>
    );
}
