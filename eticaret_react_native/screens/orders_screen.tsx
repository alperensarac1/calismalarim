import React, { useEffect, useState } from "react";
import { ActivityIndicator, FlatList, Pressable, Text, View } from "react-native";

import { OrderSummaryDto } from "../api/types";
import { useNavigation } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import {RootStackParamList} from "../navigation/app_navigator";
import {orderApi} from "../api/order_api";


export default function OrdersScreen() {
    const nav = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
    const [items, setItems] = useState<OrderSummaryDto[]>([]);
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState<string | null>(null);

    useEffect(() => {
        (async () => {
            setLoading(true);
            try {
                const list = await orderApi.getOrders();
                setItems(list);
            } catch (e: any) {
                setErr(e?.message ?? "Hata");
            } finally {
                setLoading(false);
            }
        })();
    }, []);

    return (
        <View style={{ flex: 1, padding: 12 }}>
            <Text style={{ fontSize: 20, fontWeight: "700", marginBottom: 8 }}>Siparişler</Text>
            {err ? <Text style={{ color: "red" }}>{err}</Text> : null}
            {loading ? <ActivityIndicator /> : null}

            <FlatList
                data={items}
                keyExtractor={(x) => String(x.id)}
                contentContainerStyle={{ gap: 10, paddingBottom: 20 }}
                renderItem={({ item }) => (
                    <Pressable
                        onPress={() => nav.navigate("OrderDetail", { id: item.id })}
                        style={{ borderWidth: 1, borderRadius: 12, padding: 10 }}
                    >
                        <Text style={{ fontWeight: "700" }}>Sipariş #{item.id}</Text>
                        <Text>Durum: {item.status}</Text>
                        <Text>Toplam: {item.currency} {item.total_amount.toFixed(2)}</Text>
                        <Text style={{ opacity: 0.7 }}>{item.created_at}</Text>
                    </Pressable>
                )}
            />
        </View>
    );
}
