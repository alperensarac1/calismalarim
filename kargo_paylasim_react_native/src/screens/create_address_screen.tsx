import React, { useState } from "react";
import { Alert, Button, ScrollView, TextInput } from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";
import { Endpoints } from "../api/endpoints";
import { postJSON, APIError } from "../api/client";
import {RootStackParamList} from "../navigation/app_navigator";

type Props = NativeStackScreenProps<RootStackParamList, "CreateAddress">;

export default function CreateAddressScreen({ navigation }: Props) {
    const [loading, setLoading] = useState(false);

    const [title, setTitle] = useState("");
    const [city, setCity] = useState("");
    const [district, setDistrict] = useState("");
    const [neighborhood, setNeighborhood] = useState("");
    const [addressLine, setAddressLine] = useState("");
    const [postal, setPostal] = useState("");

    const inputStyle = { borderWidth: 1, borderColor: "#ccc", padding: 12, borderRadius: 10, marginBottom: 10 };

    async function save() {
        if (!title || !city || !district || !addressLine) {
            Alert.alert("Uyarı", "Başlık, şehir, ilçe ve açık adres zorunlu.");
            return;
        }

        setLoading(true);
        try {
            const res = await postJSON<any>(Endpoints.addressCreate, {
                title, city, district,
                neighborhood,
                address_line: addressLine,
                postal_code: postal,
            });

            if (!res.ok) throw new APIError(res.error ?? "Adres eklenemedi");

            Alert.alert("Başarılı", "Adres eklendi.", [
                { text: "Tamam", onPress: () => navigation.goBack() },
            ]);
        } catch (e: any) {
            Alert.alert("Hata", e?.message ?? String(e));
        } finally {
            setLoading(false);
        }
    }

    return (
        <ScrollView contentContainerStyle={{ padding: 16 }}>
            <TextInput placeholder="Adres Başlığı (Ev/İş)" value={title} onChangeText={setTitle} style={inputStyle} />
            <TextInput placeholder="Şehir" value={city} onChangeText={setCity} style={inputStyle} />
            <TextInput placeholder="İlçe" value={district} onChangeText={setDistrict} style={inputStyle} />
            <TextInput placeholder="Mahalle (opsiyonel)" value={neighborhood} onChangeText={setNeighborhood} style={inputStyle} />
            <TextInput placeholder="Açık adres" value={addressLine} onChangeText={setAddressLine} multiline style={[inputStyle, { height: 90 }]} />
            <TextInput placeholder="Posta kodu (opsiyonel)" value={postal} onChangeText={setPostal} keyboardType="number-pad" style={inputStyle} />

            <Button title={loading ? "..." : "Kaydet"} onPress={save} disabled={loading} />
        </ScrollView>
    );
}
