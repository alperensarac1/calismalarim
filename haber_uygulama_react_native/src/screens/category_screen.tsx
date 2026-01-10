import React from "react";
import { View, Text, StyleSheet, FlatList, Pressable } from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";
import { useQuery } from "@tanstack/react-query";
import {RootStackParamList} from "../navigation/routes";
import {getSon3Haber} from "../api/haber_api";
import {HaberModel} from "../model/haber_model";



type Props = NativeStackScreenProps<RootStackParamList, "Category">;

export default function CategoryScreen({ navigation, route }: Props) {
    const { kategoriId, kategoriAdi } = route.params;

    // En doğrusu: getHaberler() endpoint'in varsa onu kullanıp filtrelemek.
    // Şimdilik: son3 gibi değil; sen getHaberler endpointini gönder, burayı netleştireyim.
    const allQ = useQuery({ queryKey: ["allForCategory"], queryFn: getSon3Haber });

    const list = (allQ.data ?? []).filter((h: HaberModel) => h.tur_id === kategoriId);

    return (
        <View style={styles.container}>
            <Text style={styles.h1}>Kategori: {kategoriAdi ?? kategoriId}</Text>

            <FlatList
                data={list}
                keyExtractor={(i) => String(i.id)}
                ItemSeparatorComponent={() => <View style={{ height: 8 }} />}
                renderItem={({ item }) => (
                    <Pressable
                        onPress={() => navigation.navigate("Detail", { haber: item })}
                        style={styles.card}
                    >
                        <Text style={styles.title} numberOfLines={2}>{item.baslik}</Text>
                        <Text style={styles.snip} numberOfLines={3}>
                            {item.icerik}
                        </Text>
                    </Pressable>
                )}
                ListEmptyComponent={<Text style={{ color: "#777" }}>Yükleniyor / boş...</Text>}
            />
        </View>
    );
}

const styles = StyleSheet.create({
    container: { flex: 1, paddingHorizontal: 24, paddingVertical: 16 },
    h1: { fontSize: 24, fontWeight: "800", color: "#C65555", marginBottom: 12 },
    card: { backgroundColor: "#fff", padding: 16, borderRadius: 12, elevation: 2 },
    title: { fontWeight: "800" },
    snip: { marginTop: 6, color: "#777" },
});
