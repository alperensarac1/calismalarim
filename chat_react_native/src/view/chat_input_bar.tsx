import React, { useState } from "react";
import { View, TextInput, Pressable, Text, Image } from "react-native";
import * as ImagePicker from "expo-image-picker";

type Props = {
    onSend: (text: string, imgBase64: string | null) => void;
};

export default function ChatInputBar({ onSend }: Props) {
    const [text, setText] = useState("");
    const [preview, setPreview] = useState<{ uri: string; base64: string } | null>(null);

    const pickImage = async () => {
        const perm = await ImagePicker.requestMediaLibraryPermissionsAsync();
        if (!perm.granted) return;

        const result = await ImagePicker.launchImageLibraryAsync({
            mediaTypes: ImagePicker.MediaTypeOptions.Images,
            base64: true,
            quality: 0.8,
        });

        if (!result.canceled) {
            const asset = result.assets[0];
            if (asset.base64) setPreview({ uri: asset.uri, base64: asset.base64 });
        }
    };

    const send = () => {
        const t = text.trim();
        const b64 = preview?.base64 ?? null;
        if (!t && !b64) return;

        onSend(t, b64);
        setText("");
        setPreview(null);
    };

    return (
        <View style={{ padding: 10, borderTopWidth: 1, borderColor: "#eee", backgroundColor: "white" }}>
            {preview?.uri ? (
                <Pressable onPress={() => setPreview(null)} style={{ marginBottom: 8 }}>
                    <Image source={{ uri: preview.uri }} style={{ height: 170, borderRadius: 10 }} />
                    <Text style={{ color: "#6D28D9", marginTop: 6 }}>Kaldır (tıkla)</Text>
                </Pressable>
            ) : null}

            <View style={{ flexDirection: "row", alignItems: "center", gap: 8 }}>
                <Pressable
                    onPress={pickImage}
                    style={{
                        width: 42,
                        height: 42,
                        borderRadius: 21,
                        backgroundColor: "#F3F4F6",
                        alignItems: "center",
                        justifyContent: "center",
                    }}
                >
                    <Text style={{ fontSize: 22 }}>+</Text>
                </Pressable>

                <TextInput
                    value={text}
                    onChangeText={setText}
                    placeholder="Mesaj yaz…"
                    style={{ flex: 1, borderWidth: 1, borderColor: "#ddd", borderRadius: 12, paddingHorizontal: 12, paddingVertical: 10 }}
                    returnKeyType="send"
                    onSubmitEditing={send}
                />

                <Pressable
                    onPress={send}
                    style={{ paddingHorizontal: 14, paddingVertical: 10, borderRadius: 12, backgroundColor: "#6D28D9" }}
                >
                    <Text style={{ color: "white", fontWeight: "700" }}>Gönder</Text>
                </Pressable>
            </View>
        </View>
    );
}
