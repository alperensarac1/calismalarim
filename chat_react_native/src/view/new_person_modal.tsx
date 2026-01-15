import React, { useState } from "react";
import { Modal, View, Text, TextInput, Pressable } from "react-native";

type Props = {
    visible: boolean;
    onClose: () => void;
    onConfirm: (numara: string) => void;
};

export default function NewPersonModal({ visible, onClose, onConfirm }: Props) {
    const [numara, setNumara] = useState("");

    const submit = () => {
        const v = numara.trim();
        if (!v) return;
        onConfirm(v);
        setNumara("");
    };

    return (
        <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose}>
            <View style={{ flex: 1, backgroundColor: "rgba(0,0,0,0.4)", justifyContent: "center", padding: 18 }}>
                <View style={{ backgroundColor: "white", borderRadius: 14, padding: 16, gap: 10 }}>
                    <Text style={{ fontSize: 18, fontWeight: "700" }}>Yeni Mesaj</Text>

                    <TextInput
                        placeholder="Alıcı numarası"
                        keyboardType="phone-pad"
                        value={numara}
                        onChangeText={setNumara}
                        style={{ borderWidth: 1, borderRadius: 10, padding: 12 }}
                    />

                    <View style={{ flexDirection: "row", justifyContent: "flex-end", gap: 10 }}>
                        <Pressable onPress={onClose} style={{ padding: 10 }}>
                            <Text>İptal</Text>
                        </Pressable>
                        <Pressable onPress={submit} style={{ padding: 10 }}>
                            <Text style={{ color: "#6D28D9", fontWeight: "700" }}>Gönder</Text>
                        </Pressable>
                    </View>
                </View>
            </View>
        </Modal>
    );
}
