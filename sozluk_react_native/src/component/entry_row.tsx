import React from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import {Entry} from "../models/entry";

export default function EntryRow({ entry, onPress }: { entry: Entry; onPress: () => void }) {
    return (
        <Pressable onPress={onPress} style={styles.card}>
            <Text style={styles.title}>{entry.title}</Text>
            <Text numberOfLines={2} style={styles.content}>{entry.content}</Text>
        </Pressable>
    );
}

const styles = StyleSheet.create({
    card: {
        borderWidth: 1,
        borderColor: "#ddd",
        borderRadius: 12,
        padding: 14,
        backgroundColor: "white",
    },
    title: { fontSize: 16, fontWeight: "700" },
    content: { marginTop: 6, color: "#222" },
});
