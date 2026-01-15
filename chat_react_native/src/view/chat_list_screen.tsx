import React, { useEffect, useRef, useState } from "react";
import { View, Text, FlatList, Pressable, Alert } from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";

import type { RootStackParamList } from "../../App";
import {KonusulanKisi} from "../model/konusulan_kisi";
import {AppConfig} from "../util/app_config";
import {apiService} from "../client/api_service";
import NewPersonModal from "./new_person_modal";


type Props = NativeStackScreenProps<RootStackParamList, "Chats">;

export default function ChatListScreen({ navigation }: Props) {
    const [kisiler, setKisiler] = useState<KonusulanKisi[]>([]);
    const [hata, setHata] = useState<string | null>(null);
    const [modal, setModal] = useState(false);

    const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);

    const load = async () => {
        try {
            const resp = await apiService.konusulanKisiler(AppConfig.kullaniciId);
            if (resp.success) {
                setKisiler(resp.kisiler || []);
                setHata(null);
            } else {
                setHata("Liste alınamadı");
            }
        } catch (e: any) {
            setHata(`Sunucu hatası: ${String(e?.message ?? e)}`);
        }
    };

    useEffect(() => {
        load();
        timerRef.current = setInterval(load, 15000);
        return () => {
            if (timerRef.current) clearInterval(timerRef.current);
        };
    }, []);

    useEffect(() => {
        if (hata) Alert.alert("Hata", hata);
    }, [hata]);

    const openByNumara = async (numara: string) => {
        try {
            const resp = await apiService.kullanicilariGetir();
            if (resp.success) {
                const kisi = (resp.kullanicilar || []).find((k) => k.numara === numara);
                if (kisi) {
                    navigation.navigate("SingleChat", { aliciId: kisi.id, aliciAd: kisi.ad });
                } else {
                    Alert.alert("Uyarı", "Bu numara kayıtlı değil");
                }
            }
        } catch (e: any) {
            Alert.alert("Hata", `Sunucu hatası: ${String(e?.message ?? e)}`);
        }
    };

    const renderItem = ({ item }: { item: KonusulanKisi }) => (
        <Pressable
            onPress={() => openByNumara(item.numara)}
            style={{ padding: 14, borderBottomWidth: 1, borderColor: "#eee" }}
        >
            <Text style={{ fontWeight: "700" }}>{item.ad}</Text>
            <Text numberOfLines={1} style={{ color: "#555" }}>
                {item.son_mesaj}
            </Text>
            <Text style={{ fontSize: 12, color: "#777" }}>{item.tarih}</Text>
        </Pressable>
    );

    return (
        <View style={{ flex: 1 }}>
            <FlatList
                data={kisiler}
                keyExtractor={(x) => String(x.id)}
                renderItem={renderItem}
                contentContainerStyle={{ paddingBottom: 90 }}
            />

            <Pressable
                onPress={() => setModal(true)}
                style={{
                    position: "absolute",
                    right: 16,
                    bottom: 24,
                    backgroundColor: "#6D28D9",
                    width: 56,
                    height: 56,
                    borderRadius: 28,
                    alignItems: "center",
                    justifyContent: "center",
                }}
            >
                <Text style={{ color: "white", fontSize: 28, marginTop: -2 }}>+</Text>
            </Pressable>

            <NewPersonModal
                visible={modal}
                onClose={() => setModal(false)}
                onConfirm={(numara) => {
                    setModal(false);
                    openByNumara(numara);
                }}
            />
        </View>
    );
}
