import React from "react";
import { TextInput, StyleSheet, View } from "react-native";

export default function SearchField({
                                        value,
                                        onChangeText,
                                    }: {
    value: string;
    onChangeText: (t: string) => void;
}) {
    return (
        <View style={styles.wrap}>
            <TextInput
                value={value}
                onChangeText={onChangeText}
                placeholder="Ara"
                style={styles.input}
                autoCapitalize="none"
            />
        </View>
    );
}

const styles = StyleSheet.create({
    wrap: { width: "100%" },
    input: {
        borderWidth: 1,
        borderColor: "#bbb",
        borderRadius: 10,
        paddingHorizontal: 12,
        paddingVertical: 10,
    },
});
