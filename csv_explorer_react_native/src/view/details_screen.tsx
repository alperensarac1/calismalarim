import React, { useMemo, useState } from "react";
import { View, Text, TextInput, Pressable, FlatList, StyleSheet } from "react-native";
import * as Clipboard from "expo-clipboard";

import type { NativeStackScreenProps } from "@react-navigation/native-stack";
import {RootStackParamList} from "../navigation/types";

type Props = NativeStackScreenProps<RootStackParamList, "Details">;

type FieldItem = { key: string; value: string };

export default function DetailsScreen({ route, navigation }: Props) {
    const jsonStr = route.params?.json ?? "{}";
    const headers = route.params?.headers ?? [];

    const [q, setQ] = useState("");

    const obj = useMemo<Record<string, any>>(() => {
        try { return JSON.parse(jsonStr); } catch { return {}; }
    }, [jsonStr]);

    const fields = useMemo(() => buildFields(headers, obj), [headers, obj]);

    const filtered = useMemo(() => {
        const qq = q.trim().toLowerCase();
        if (!qq) return fields;
        return fields.filter(
            (f) => f.key.toLowerCase().includes(qq) || f.value.toLowerCase().includes(qq)
        );
    }, [q, fields]);

    function copyJson() {
        Clipboard.setStringAsync(jsonStr);
    }

    function copyCsvRow() {
        Clipboard.setStringAsync(buildCsv(headers, obj));
    }

    return (
        <View style={styles.root}>
            <View style={styles.topRow}>
                <Text style={styles.h1}>Details</Text>
                <Pressable onPress={() => navigation.goBack()}>
                    <Text style={styles.link}>Back</Text>
                </Pressable>
            </View>

            <TextInput
                value={q}
                onChangeText={setQ}
                placeholder="Search in fields"
                style={styles.input}
            />

            <View style={{ flexDirection: "row", gap: 10 }}>
                <Btn label="Copy JSON" onPress={copyJson} />
                <Btn label="Copy CSV" onPress={copyCsvRow} tone="ghost" />
            </View>

            <Text style={styles.count}>{filtered.length} fields</Text>

            <FlatList
                data={filtered}
                keyExtractor={(it) => it.key}
                ItemSeparatorComponent={() => <View style={{ height: 10 }} />}
                contentContainerStyle={{ paddingBottom: 24 }}
                renderItem={({ item }) => (
                    <View style={styles.card}>
                        <Text style={styles.k}>{item.key}</Text>
                        <Text style={styles.v}>{item.value}</Text>
                    </View>
                )}
            />
        </View>
    );
}

function Btn({ label, onPress, tone }: { label: string; onPress: () => void; tone?: "ghost" }) {
    return (
        <Pressable onPress={onPress} style={[styles.btn, tone === "ghost" && styles.btnGhost]}>
            <Text style={[styles.btnText, tone === "ghost" && styles.btnGhostText]}>{label}</Text>
        </Pressable>
    );
}

function buildFields(headers: string[], obj: Record<string, any>): FieldItem[] {
    const out: FieldItem[] = [];

    if (headers?.length) {
        for (const h of headers) out.push({ key: h, value: (obj[h] ?? "-").toString() || "-" });

        const extra = Object.keys(obj).filter((k) => !headers.includes(k)).sort();
        for (const k of extra) out.push({ key: k, value: (obj[k] ?? "-").toString() || "-" });
    } else {
        const keys = Object.keys(obj).sort();
        for (const k of keys) out.push({ key: k, value: (obj[k] ?? "-").toString() || "-" });
    }

    return out;
}

function buildCsv(headers: string[], obj: Record<string, any>) {
    if (!headers?.length) return JSON.stringify(obj);
    const headerLine = headers.join(",");
    const rowLine = headers.map((h) => esc((obj[h] ?? "").toString())).join(",");
    return headerLine + "\n" + rowLine;
}

function esc(v0: string) {
    let v = v0 ?? "";
    const needs = v.includes(",") || v.includes('"') || v.includes("\n") || v.includes("\r");
    v = v.replace(/"/g, '""');
    if (needs) v = `"${v}"`;
    return v;
}

const styles = StyleSheet.create({
    root: { flex: 1, padding: 14, gap: 10 },
    topRow: { flexDirection: "row", justifyContent: "space-between", alignItems: "center" },
    h1: { fontSize: 20, fontWeight: "800" },
    link: { color: "#2563EB", fontWeight: "700" },
    input: { borderWidth: 1, borderColor: "#CBD5E1", borderRadius: 12, padding: 12 },
    btn: { paddingVertical: 10, paddingHorizontal: 12, borderRadius: 12, backgroundColor: "#111827" },
    btnText: { color: "white", fontWeight: "700" },
    btnGhost: { backgroundColor: "transparent", borderWidth: 1, borderColor: "#CBD5E1" },
    btnGhostText: { color: "#111827" },
    count: { fontWeight: "700" },
    card: { borderWidth: 1, borderColor: "#E5E7EB", borderRadius: 14, padding: 12, backgroundColor: "white" },
    k: { fontWeight: "800" },
    v: { marginTop: 6 },
});
