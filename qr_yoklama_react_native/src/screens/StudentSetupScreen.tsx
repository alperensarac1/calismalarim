import React, { useState } from "react";
import { View, TextInput, Button, Text } from "react-native";
import { Prefs } from "../storage/prefs";
import { AppToast } from "../ui/toast";

export default function StudentSetupScreen({ navigation }: any) {
    const [no, setNo] = useState("");
    const [saving, setSaving] = useState(false);

    const save = async () => {
        const s = no.trim();
        if (!s) {
            AppToast.error("Öğrenci numarası gerekli");
            return;
        }
        setSaving(true);
        try {
            await Prefs.setStudentNo(s);
            AppToast.success("Kaydedildi ✅");
            navigation.replace("Scan", { studentNo: no });

        } catch (e: any) {
            AppToast.error(String(e?.message ?? e));
        } finally {
            setSaving(false);
        }
    };

    return (
        <View style={{ flex: 1, padding: 24, justifyContent: "center" }}>
            <Text style={{ fontSize: 20, fontWeight: "600", marginBottom: 12 }}>
                Öğrenci Girişi
            </Text>

            <TextInput
                value={no}
                onChangeText={setNo}
                placeholder="Öğrenci Numaranızı giriniz"
                keyboardType="numeric"
                style={{
                    borderWidth: 1,
                    borderColor: "#ccc",
                    borderRadius: 10,
                    padding: 12,
                    marginBottom: 12,
                }}
            />

            <Button title={saving ? "Kaydediliyor..." : "KAYDET"} onPress={save} disabled={saving} />
        </View>
    );
}
