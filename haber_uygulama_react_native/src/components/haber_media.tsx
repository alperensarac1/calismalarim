import React from "react";
import { Image, View, StyleSheet } from "react-native";
import { Video } from "expo-av";

export default function HaberMedia({
                                       mediaType,
                                       url,
                                       height = 200,
                                   }: {
    mediaType: string;
    url: string;
    height?: number;
}) {
    if (mediaType === "video") {
        return (
            <View style={[styles.box, { height }]}>
                <Video
                    source={{ uri: url }}
                    style={StyleSheet.absoluteFill}
                    shouldPlay
                    isLooping
                    useNativeControls={false}
                />
            </View>
        );
    }

    return (
        <Image
            source={{ uri: url }}
            style={[styles.box, { height }]}
            resizeMode="cover"
        />
    );
}

const styles = StyleSheet.create({
    box: {
        width: "100%",
        backgroundColor: "#eee",
    },
});
