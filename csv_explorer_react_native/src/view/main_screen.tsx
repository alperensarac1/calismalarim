import React, { useEffect, useMemo, useState } from "react";
import { View, Text, TextInput, Pressable, FlatList, StyleSheet } from "react-native";
import * as DocumentPicker from "expo-document-picker";
import * as Linking from "expo-linking";
import * as Clipboard from "expo-clipboard";

import { initDb, clearDb, getAll, insertAll, searchAllColumns, searchInColumn, type DbRow } from "../db/db";


import type { NativeStackScreenProps } from "@react-navigation/native-stack";
import {importCsvFromUri} from "../domain/csv_importer";
import {uploadCsv} from "../domain/upload_client";
import {RootStackParamList} from "../navigation/types";


type Props = NativeStackScreenProps<RootStackParamList, "Main">;

const ENDPOINT = "https://alperensaracdeneme.com/deneme/upload_csv.php";

export default function MainScreen({ navigation }: Props) {
    const [loading, setLoading] = useState(false);
    const [infoText, setInfoText] = useState("0 records");
    const [headers, setHeaders] = useState<string[]>([]);
    const [records, setRecords] = useState<DbRow[]>([]);

    const [selectedColumn, setSelectedColumn] = useState<string>("ALL_COLUMNS");
    const [query, setQuery] = useState<string>("");

    const [lastPickedUri, setLastPickedUri] = useState<string | null>(null);

    useEffect(() => {
        initDb();
        refreshAll();
    }, []);

    const cols = useMemo(() => ["ALL_COLUMNS", ...headers], [headers]);
    const canUpload = !!lastPickedUri;

    function refreshAll() {
        const list = getAll();
        setRecords(list);
        setInfoText(`${list.length} records`);
    }

    async function pickCsv() {
        const res = await DocumentPicker.getDocumentAsync({
            type: ["text/*", "text/csv", "application/vnd.ms-excel"],
            copyToCacheDirectory: true,
            multiple: false,
        });

        if (res.canceled) return;
        const uri = res.assets?.[0]?.uri;
        if (!uri) return;

        setLoading(true);
        setInfoText("Importing...");
        try {
            const out = await importCsvFromUri(uri);
            insertAll(out.rows);
            setHeaders(out.headers);

            setLastPickedUri(uri);

            const list = getAll();
            setRecords(list);
            setInfoText(`Imported: ${out.rows.length} rows`);
        } catch (e) {
            console.log(e);
            setInfoText("Import failed");
        } finally {
            setLoading(false);
        }
    }

    function applyFilter() {
        setLoading(true);
        setInfoText("Filtering...");
        try {
            const q = query.trim();
            let list: DbRow[];
            if (!q) list = getAll();
            else if (selectedColumn === "ALL_COLUMNS") list = searchAllColumns(q);
            else list = searchInColumn(selectedColumn, q);

            setRecords(list);
            setInfoText(`${list.length} records (filter: ${selectedColumn})`);
        } finally {
            setLoading(false);
        }
    }

    function clearFilter() {
        setQuery("");
        setSelectedColumn("ALL_COLUMNS");
        refreshAll();
    }

    function clearDatabase() {
        setLoading(true);
        try {
            clearDb();
            setHeaders([]);
            setRecords([]);
            setLastPickedUri(null);
            setQuery("");
            setSelectedColumn("ALL_COLUMNS");
            setInfoText("Database cleared");
        } finally {
            setLoading(false);
        }
    }

    async function doUpload() {
        if (!lastPickedUri) return;
        setLoading(true);
        setInfoText("Uploading...");
        try {
            const url = await uploadCsv(ENDPOINT, lastPickedUri);
            setInfoText("Upload done. Opening...");
            await Linking.openURL(url);
        } catch (e) {
            console.log(e);
            setInfoText("Upload failed");
        } finally {
            setLoading(false);
        }
    }

    function renderItem({ item }: { item: DbRow }) {
        let obj: Record<string, any> = {};
        try {
            obj = JSON.parse(item.data_json);
        } catch {}

        const id = (obj.id ?? "").toString();
        const first = (obj.first_name ?? obj.firstname ?? "").toString();
        const last = (obj.last_name ?? obj.lastname ?? "").toString();

        const title =
            id && (first || last)
                ? `#${id}  ${(first + " " + last).trim()}`
                : id
                    ? `#${id}`
                    : (first || last)
                        ? (first + " " + last).trim()
                        : "Row";

        const subtitle = buildSubtitle(obj);

        return (
            <Pressable
                style={styles.card}
                onPress={() => navigation.navigate("Details", { json: item.data_json, headers })}
            >
                <Text style={styles.title}>{title}</Text>
                <Text style={styles.sub}>{subtitle}</Text>

                <View style={{ flexDirection: "row", gap: 10, marginTop: 8 }}>
                    <Pressable style={styles.smallBtn} onPress={() => Clipboard.setStringAsync(item.data_json)}>
                        <Text style={styles.smallBtnText}>Copy JSON</Text>
                    </Pressable>
                </View>
            </Pressable>
        );
    }

    return (
        <View style={styles.root}>
            <View style={styles.rowWrap}>
                <Btn disabled={loading} label="Select CSV" onPress={pickCsv} />
                <Btn disabled={loading || !canUpload} label="Get .xls" onPress={doUpload} />
                <Btn disabled={loading} label="Filter" onPress={applyFilter} />
                <Btn disabled={loading} label="Clear" onPress={clearFilter} />
                <Btn disabled={loading} label="Clear DB" onPress={clearDatabase} tone="ghost" />
            </View>

            <Text style={styles.info}>{infoText}</Text>

            <Text style={styles.label}>Column</Text>
            <View style={styles.pills}>
                {cols.slice(0, 8).map((c) => (
                    <Pressable
                        key={c}
                        onPress={() => setSelectedColumn(c)}
                        style={[styles.pill, selectedColumn === c && styles.pillActive]}
                    >
                        <Text style={[styles.pillText, selectedColumn === c && styles.pillTextActive]}>{c}</Text>
                    </Pressable>
                ))}
                {cols.length > 8 && <Text style={styles.hint}>+ {cols.length - 8} more (quick picker)</Text>}
            </View>

            <TextInput
                value={query}
                onChangeText={setQuery}
                placeholder="Search"
                style={styles.input}
                editable={!loading}
            />

            <FlatList
                data={records}
                keyExtractor={(it) => String(it.local_id)}
                renderItem={renderItem}
                contentContainerStyle={{ paddingBottom: 24 }}
                ItemSeparatorComponent={() => <View style={{ height: 10 }} />}
            />
        </View>
    );
}

function Btn({
                 label,
                 onPress,
                 disabled,
                 tone,
             }: {
    label: string;
    onPress: () => void;
    disabled?: boolean;
    tone?: "ghost";
}) {
    return (
        <Pressable
            onPress={onPress}
            disabled={disabled}
            style={[
                styles.btn,
                tone === "ghost" && styles.btnGhost,
                disabled && { opacity: 0.5 },
            ]}
        >
            <Text style={[styles.btnText, tone === "ghost" && styles.btnGhostText]}>{label}</Text>
        </Pressable>
    );
}

function buildSubtitle(obj: Record<string, any>) {
    const lastSeen = (obj.last_seen ?? "").toString();
    const country = (obj.country_title ?? "").toString();
    const city = (obj.city_title ?? "").toString();

    const parts: string[] = [];
    if (lastSeen) parts.push(`Last seen: ${lastSeen}`);
    const loc = [country, city].filter(Boolean).join(" / ");
    if (loc) parts.push(loc);

    return parts.length ? parts.join(" • ") : "Tap to view details";
}

const styles = StyleSheet.create({
    root: { flex: 1, padding: 14, gap: 10 },
    rowWrap: { flexDirection: "row", flexWrap: "wrap", gap: 10 },
    btn: { paddingVertical: 10, paddingHorizontal: 12, borderRadius: 12, backgroundColor: "#111827" },
    btnText: { color: "white", fontWeight: "600" },
    btnGhost: { backgroundColor: "transparent", borderWidth: 1, borderColor: "#CBD5E1" },
    btnGhostText: { color: "#111827" },

    info: { fontWeight: "700", marginTop: 4 },
    label: { color: "#6B7280", fontSize: 12 },
    input: { borderWidth: 1, borderColor: "#CBD5E1", borderRadius: 12, padding: 12 },

    card: { borderWidth: 1, borderColor: "#E5E7EB", borderRadius: 14, padding: 12, backgroundColor: "white" },
    title: { fontSize: 16, fontWeight: "700" },
    sub: { color: "#6B7280", marginTop: 4 },

    pills: { flexDirection: "row", flexWrap: "wrap", gap: 8, alignItems: "center" },
    pill: { paddingVertical: 6, paddingHorizontal: 10, borderRadius: 999, borderWidth: 1, borderColor: "#CBD5E1" },
    pillActive: { backgroundColor: "#111827", borderColor: "#111827" },
    pillText: { fontSize: 12, color: "#111827" },
    pillTextActive: { color: "white" },
    hint: { color: "#6B7280", fontSize: 12 },
    smallBtn: { paddingVertical: 6, paddingHorizontal: 10, borderRadius: 10, borderWidth: 1, borderColor: "#CBD5E1" },
    smallBtnText: { fontSize: 12, color: "#111827" },
});
