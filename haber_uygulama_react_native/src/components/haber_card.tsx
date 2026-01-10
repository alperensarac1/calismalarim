import React from "react";
import { View, Text, StyleSheet, Pressable } from "react-native";
import HaberMedia from "./haber_media";
import {HaberModel} from "../model/haber_model";


export default function HaberCard({
                                      haber,
                                      onPress,
                                  }: {
    haber: HaberModel;
    onPress: () => void;
}) {
    return (
        <Pressable onPress={onPress} style={styles.card}>
            <View style={styles.mediaWrap}>
                <HaberMedia mediaType={haber.media_type} url={haber.media_url} height={150} />
            </View>
            <View style={styles.footer}>
                <Text style={styles.title} numberOfLines={2}>
                    {haber.baslik}
                </Text>
                <Text style={styles.more}>Devamını Oku→</Text>
            </View>
        </Pressable>
    );
}

const styles = StyleSheet.create({
    card: {
        width: 300,
        borderRadius: 12,
        backgroundColor: "#fff",
        overflow: "hidden",
        elevation: 3,
    },
    mediaWrap: { width: "100%" },
    footer: { padding: 10, flexDirection: "row", alignItems: "center" },
    title: { flex: 1, fontWeight: "700" },
    more: { marginLeft: 8, color: "#888", fontSize: 12 },
});
