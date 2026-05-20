import React from "react";
import { StyleSheet, Text, TouchableOpacity, View } from "react-native";

type Props = {
    onStartBroadcast: () => void;
    onWatchBroadcasts: () => void;
};

export function HomeScreen(props: Props) {
    return (
        <View style={styles.container}>
            <Text style={styles.title}>Canlı Yayın Uygulaması</Text>

            <TouchableOpacity style={styles.button} onPress={props.onStartBroadcast}>
                <Text style={styles.buttonText}>Yayın Aç</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.button} onPress={props.onWatchBroadcasts}>
                <Text style={styles.buttonText}>Yayınları İzle</Text>
            </TouchableOpacity>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 24,
        justifyContent: "center",
        backgroundColor: "#F8FAFC",
    },
    title: {
        fontSize: 26,
        fontWeight: "700",
        textAlign: "center",
        marginBottom: 32,
        color: "#0F172A",
    },
    button: {
        backgroundColor: "#2563EB",
        padding: 16,
        borderRadius: 12,
        marginBottom: 16,
    },
    buttonText: {
        color: "#FFFFFF",
        textAlign: "center",
        fontSize: 16,
        fontWeight: "700",
    },
});