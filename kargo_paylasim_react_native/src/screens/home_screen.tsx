import React, { useCallback, useEffect, useState } from "react";
import { Alert, Button, FlatList, RefreshControl, Text, View } from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";
import { Endpoints } from "../api/endpoints";
import { getJSON, postJSON, APIError } from "../api/client";
import type { Address, AddressListData, Shipment, ShipmentListData } from "../api/types";
import {RootStackParamList} from "../navigation/app_navigator";

type Props = NativeStackScreenProps<RootStackParamList, "Home">;

export default function HomeScreen({ navigation }: Props) {
    const [loading, setLoading] = useState(false);
    const [errorText, setErrorText] = useState<string | null>(null);
    const [shipments, setShipments] = useState<Shipment[]>([]);
    const [addresses, setAddresses] = useState<Address[]>([]);

    const refresh = useCallback(async () => {
        setLoading(true);
        setErrorText(null);
        try {
            const s = await getJSON<ShipmentListData>(Endpoints.shipmentList, { type: "all" });
            const a = await getJSON<AddressListData>(Endpoints.addressList);

            if (!s.ok) throw new APIError(s.error ?? "shipment_list failed");
            if (!a.ok) throw new APIError(a.error ?? "address_list failed");

            setShipments(s.data?.items ?? []);
            setAddresses(a.data?.items ?? []);
        } catch (e: any) {
            setErrorText(e?.message ?? String(e));
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        refresh();
    }, [refresh]);

    async function setDefaultAddress(id: number) {
        try {
            const r = await postJSON<boolean>(Endpoints.addressSetDefault, { id });
            if (!r.ok) throw new APIError(r.error ?? "Varsayılan ayarlanamadı");
            setAddresses((prev) => prev.map((x) => ({ ...x, is_default: x.id === id ? 1 : 0 })));
        } catch (e: any) {
            Alert.alert("Hata", e?.message ?? String(e));
        }
    }

    async function deleteAddress(id: number) {
        Alert.alert("Sil", "Adres silinsin mi?", [
            { text: "Vazgeç", style: "cancel" },
            {
                text: "Sil",
                style: "destructive",
                onPress: async () => {
                    try {
                        const r = await postJSON<boolean>(Endpoints.addressDelete, { id });
                        if (!r.ok) throw new APIError(r.error ?? "Adres silinemedi");
                        setAddresses((prev) => prev.filter((x) => x.id !== id));
                    } catch (e: any) {
                        Alert.alert("Hata", e?.message ?? String(e));
                    }
                },
            },
        ]);
    }

    const data = [
        { type: "header", id: "hdr" } as const,
        ...shipments.map((s) => ({ type: "shipment", id: `s-${s.id}`, s } as const)),
        { type: "addrHeader", id: "ah" } as const,
        ...addresses.map((a) => ({ type: "address", id: `a-${a.id}`, a } as const)),
    ];

    return (
        <View style={{ flex: 1 }}>
            <View style={{ padding: 12, flexDirection: "row", gap: 8, justifyContent: "flex-end" }}>
                <Button title="+ Adres" onPress={() => navigation.navigate("CreateAddress")} />
                <Button title="+ Yeni" onPress={() => navigation.navigate("CreateShipment")} />
            </View>

            <FlatList
                data={data}
                keyExtractor={(x) => x.id}
                refreshControl={<RefreshControl refreshing={loading} onRefresh={refresh} />}
                renderItem={({ item }) => {
                    if (item.type === "header") {
                        return errorText ? <Text style={{ color: "red", paddingHorizontal: 12 }}>{errorText}</Text> : null;
                    }
                    if (item.type === "shipment") {
                        const s = item.s;
                        return (
                            <View style={{ padding: 12, borderBottomWidth: 1, borderColor: "#eee" }}>
                                <Text style={{ fontWeight: "700" }}>ID: #{s.id} • {s.status}</Text>
                                <Text style={{ color: "#666" }}>Kod: {s.pickup_code}</Text>
                                {!!s.cargo_company_name && <Text style={{ color: "#666" }}>Kargo: {s.cargo_company_name}</Text>}
                            </View>
                        );
                    }
                    if (item.type === "addrHeader") {
                        return <Text style={{ fontWeight: "700", padding: 12 }}>Adresler</Text>;
                    }
                    // address
                    const a = item.a;
                    return (
                        <View style={{ marginHorizontal: 12, marginVertical: 6, padding: 12, borderWidth: 1, borderColor: "#eee", borderRadius: 12 }}>
                            <View style={{ flexDirection: "row", alignItems: "center" }}>
                                <Text style={{ fontWeight: "700", flex: 1 }}>{a.title}</Text>
                                {a.is_default === 1 && (
                                    <View style={{ paddingHorizontal: 10, paddingVertical: 4, backgroundColor: "rgba(0,128,0,0.15)", borderRadius: 999 }}>
                                        <Text style={{ fontSize: 12 }}>Varsayılan</Text>
                                    </View>
                                )}
                            </View>

                            <Text style={{ color: "#666", marginTop: 6 }}>{a.district} / {a.city}</Text>
                            <Text style={{ color: "#666", marginTop: 4 }} numberOfLines={2}>{a.address_line}</Text>

                            <View style={{ flexDirection: "row", gap: 8, marginTop: 10 }}>
                                {a.is_default !== 1 && <Button title="Varsayılan" onPress={() => setDefaultAddress(a.id)} />}
                                <Button title="Sil" color="red" onPress={() => deleteAddress(a.id)} />
                            </View>
                        </View>
                    );
                }}
            />
        </View>
    );
}
