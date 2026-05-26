import React from "react";
import { View, Text, Pressable, StyleSheet } from "react-native";

type Props = {
    onCreateRoom: () => void;
    onJoinRoom: () => void;
};

export default function HomeScreen({ onCreateRoom, onJoinRoom }: Props) {
    return (
        <View style={styles.container}>
            <Text style={styles.title}>Canlı Quiz</Text>

            <Text style={styles.subtitle}>
                Oda oluştur veya oda kodu ile quize katıl.
            </Text>

            <Pressable style={styles.button} onPress={onCreateRoom}>
                <Text style={styles.buttonText}>Oda Oluştur</Text>
            </Pressable>

            <Pressable style={styles.button} onPress={onJoinRoom}>
                <Text style={styles.buttonText}>Odaya Giriş Yap</Text>
            </Pressable>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 24,
        justifyContent: "center",
    },
    title: {
        fontSize: 34,
        fontWeight: "800",
        textAlign: "center",
        color: "#111827",
    },
    subtitle: {
        fontSize: 16,
        textAlign: "center",
        color: "#6B7280",
        marginTop: 12,
        marginBottom: 36,
    },
    button: {
        height: 56,
        backgroundColor: "#6D28D9",
        borderRadius: 14,
        justifyContent: "center",
        alignItems: "center",
        marginTop: 14,
    },
    buttonText: {
        color: "#FFFFFF",
        fontSize: 17,
        fontWeight: "700",
    },
});