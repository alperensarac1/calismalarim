import React from "react";
import { View, Text, StyleSheet, FlatList, ScrollView, Pressable } from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";
import { useQuery } from "@tanstack/react-query";
import {getGundem, getKategoriler, getSonDakika} from "../api/haber_api";
import {RootStackParamList} from "../navigation/routes";
import HaberCard from "../components/haber_card";


type Props = NativeStackScreenProps<RootStackParamList, "Home">;

export default function HomeScreen({ navigation }: Props) {
    const sonDakikaQ = useQuery({ queryKey: ["sonDakika"], queryFn: getSonDakika });
    const gundemQ = useQuery({ queryKey: ["gundem"], queryFn: getGundem });
    const katQ = useQuery({ queryKey: ["kategoriler"], queryFn: getKategoriler });

    return (
        <ScrollView contentContainerStyle={styles.container}>
        <Text style={styles.h1}>SON DAKİKA</Text>
    <FlatList
    data={sonDakikaQ.data ?? []}
    horizontal
    keyExtractor={(i) => String(i.id)}
    ItemSeparatorComponent={() => <View style={{ width: 8 }} />}
    renderItem={({ item }) => (
        <HaberCard
            haber={item}
    onPress={() => navigation.navigate("Detail", { haber: item })}
    />
)}
    ListEmptyComponent={<Text>Son dakika haberleri yükleniyor...</Text>}
    showsHorizontalScrollIndicator={false}
    style={{ marginTop: 8 }}
    />

    <Text style={[styles.h1, { marginTop: 24 }]}>GÜNDEM</Text>
    <FlatList
    data={gundemQ.data ?? []}
    horizontal
    keyExtractor={(i) => String(i.id)}
    ItemSeparatorComponent={() => <View style={{ width: 8 }} />}
    renderItem={({ item }) => (
        <HaberCard
            haber={item}
    onPress={() => navigation.navigate("Detail", { haber: item })}
    />
)}
    ListEmptyComponent={<Text>Gündem haberleri yükleniyor...</Text>}
    showsHorizontalScrollIndicator={false}
    style={{ marginTop: 8 }}
    />

    <Text style={[styles.h1, { marginTop: 24 }]}>KATEGORİLER</Text>
    <View style={{ marginTop: 8 }}>
    {(katQ.data ?? []).map((k) => (
        <Pressable
            key={k.id}
        onPress={() => navigation.navigate("Category", { kategoriId: k.id, kategoriAdi: k.tur_adi })}
        style={styles.katItem}
        >
        <Text style={styles.katText}>{k.tur_adi}</Text>
            </Pressable>
    ))}
    {(!katQ.data || katQ.data.length === 0) && <Text>Kategoriler yükleniyor...</Text>}
    </View>
    </ScrollView>
    );
    }

    const styles = StyleSheet.create({
        container: { padding: 16 },
        h1: { fontSize: 24, fontWeight: "800", color: "#C65555" },
        katItem: { paddingVertical: 12, borderBottomWidth: StyleSheet.hairlineWidth, borderColor: "#ddd" },
        katText: { fontSize: 18 },
    });
