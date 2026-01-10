import React, { useMemo, useState } from "react";
import { View, Text, StyleSheet, ScrollView, FlatList, TextInput, Pressable, Alert } from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {getSon3Haber, getYorumlar, insertYorum} from "../api/haber_api";
import {RootStackParamList} from "../navigation/routes";
import HaberMedia from "../components/haber_media";
import HaberCard from "../components/haber_card";


type Props = NativeStackScreenProps<RootStackParamList, "Detail">;

export default function DetailScreen({ navigation, route }: Props) {
    const { haber } = route.params;

    const qc = useQueryClient();
    const yorumQ = useQuery({ queryKey: ["yorumlar", haber.id], queryFn: () => getYorumlar(haber.id) });
    const sonQ = useQuery({ queryKey: ["son3"], queryFn: getSon3Haber });

    const [rumuz, setRumuz] = useState("");
    const [yorum, setYorum] = useState("");

    const addMut = useMutation({
        mutationFn: () => insertYorum({ haber_id: haber.id, takma_ad: rumuz.trim(), yorum_metni: yorum.trim() }),
        onSuccess: async (resp) => {
            if (resp.success) {
                setRumuz("");
                setYorum("");
                await qc.invalidateQueries({ queryKey: ["yorumlar", haber.id] });
            } else {
                Alert.alert("Hata", resp.error ?? "Yorum eklenemedi");
            }
        },
        onError: () => Alert.alert("Hata", "Yorum eklenemedi"),
    });

    return (
        <ScrollView>
            <Text style={styles.title}>{haber.baslik}</Text>

            <HaberMedia mediaType={haber.media_type} url={haber.media_url} height={200} />

            <View style={styles.meta}>
                <Text>{haber.ad} {haber.soyad} - {haber.unvan}</Text>
                <Text>{haber.yayinlanma_tarihi}</Text>
            </View>

            <Text style={styles.content}>{haber.icerik}</Text>

            <Text style={styles.section}>Son Haberler</Text>
            <FlatList
                data={sonQ.data ?? []}
                horizontal
                keyExtractor={(i) => String(i.id)}
                ItemSeparatorComponent={() => <View style={{ width: 8 }} />}
                renderItem={({ item }) => (
                    <HaberCard
                        haber={item}
                        onPress={() => navigation.replace("Detail", { haber: item })}
                    />
                )}
                contentContainerStyle={{ paddingHorizontal: 16 }}
                showsHorizontalScrollIndicator={false}
            />

            <Text style={[styles.section, { color: "#6640A3" }]}>Yorum Yaz</Text>
            <View style={styles.form}>
                <TextInput value={rumuz} onChangeText={setRumuz} placeholder="Rumuz" style={styles.input} />
                <TextInput
                    value={yorum}
                    onChangeText={setYorum}
                    placeholder="Yorumunuz"
                    style={[styles.input, { height: 90 }]}
                    multiline
                />
                <Pressable
                    style={styles.btn}
                    onPress={() => {
                        if (!rumuz.trim() || !yorum.trim()) {
                            Alert.alert("Uyarı", "İlgili alanlar boş bırakılamaz");
                            return;
                        }
                        addMut.mutate();
                    }}
                >
                    <Text style={styles.btnText}>{addMut.isPending ? "Gönderiliyor..." : "GÖNDER"}</Text>
                </Pressable>
            </View>

            <Text style={[styles.section, { color: "#6640A3" }]}>Yorumlar</Text>
            <View style={{ paddingHorizontal: 16, paddingBottom: 24 }}>
                {(yorumQ.data ?? []).map((y) => (
                    <View key={y.id} style={styles.commentCard}>
                        <Text>{y.yorum_metni}</Text>
                        <Text style={styles.commentAuthor}>— {y.takma_ad}</Text>
                    </View>
                ))}
                {(!yorumQ.data || yorumQ.data.length === 0) && <Text style={{ color: "#777" }}>Yorum yok / yükleniyor...</Text>}
            </View>
        </ScrollView>
    );
}

const styles = StyleSheet.create({
    title: { fontSize: 24, fontWeight: "800", color: "#707070", padding: 16 },
    meta: { paddingHorizontal: 16, paddingVertical: 10, gap: 4 },
    content: { padding: 16, fontSize: 16, fontWeight: "700" },
    section: { fontSize: 24, fontWeight: "800", color: "#707070", padding: 16 },
    form: { paddingHorizontal: 16, gap: 8 },
    input: { borderWidth: 1, borderColor: "#ddd", borderRadius: 10, padding: 12, backgroundColor: "#fff" },
    btn: { backgroundColor: "#6640A3", padding: 14, borderRadius: 10, alignItems: "center" },
    btnText: { color: "#fff", fontWeight: "800" },
    commentCard: { backgroundColor: "#fff", borderRadius: 12, padding: 14, marginTop: 10, elevation: 2 },
    commentAuthor: { marginTop: 6, fontStyle: "italic", color: "#555" },
});
