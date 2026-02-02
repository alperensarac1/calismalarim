import React, { useState } from "react";
import { Alert, Button, ScrollView, TextInput, View } from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";
import { Endpoints } from "../api/endpoints";
import { postJSON, APIError } from "../api/client";
import type { RegisterData } from "../api/types";
import {RootStackParamList} from "../navigation/app_navigator";

type Props = NativeStackScreenProps<RootStackParamList, "Register">;

export default function RegisterScreen({ navigation }: Props) {
    const [loading, setLoading] = useState(false);

    const [first, setFirst] = useState("");
    const [last, setLast] = useState("");
    const [phone, setPhone] = useState("");
    const [tc, setTc] = useState("");
    const [password, setPassword] = useState("");

    const [addressTitle, setAddressTitle] = useState("");
    const [city, setCity] = useState("");
    const [district, setDistrict] = useState("");
    const [neighborhood, setNeighborhood] = useState("");
    const [addressLine, setAddressLine] = useState("");
    const [postal, setPostal] = useState("");

    const inputStyle = { borderWidth: 1, borderColor: "#ccc", padding: 12, borderRadius: 10, marginBottom: 10 };

    async function onRegister() {
        setLoading(true);
        try {
            const res = await postJSON<RegisterData>(Endpoints.register, {
                phone: phone.trim(),
                first_name: first.trim(),
                last_name: last.trim(),
                tc_no: tc.trim(),
                password,

                address_title: addressTitle.trim(),
                city: city.trim(),
                district: district.trim(),
                neighborhood: neighborhood.trim(),
                address_line: addressLine.trim(),
                postal_code: postal.trim(),
            });

            if (!res.ok) throw new APIError(res.error ?? "Register failed");

            Alert.alert("Başarılı", "Kayıt oluşturuldu. Giriş yapabilirsin.");
            navigation.goBack();
        } catch (e: any) {
            Alert.alert("Hata", e?.message ?? String(e));
        } finally {
            setLoading(false);
        }
    }

    return (
        <ScrollView contentContainerStyle={{ padding: 16 }}>
            <TextInput placeholder="İsim" value={first} onChangeText={setFirst} style={inputStyle} />
            <TextInput placeholder="Soyisim" value={last} onChangeText={setLast} style={inputStyle} />
            <TextInput placeholder="Telefon" value={phone} onChangeText={setPhone} keyboardType="phone-pad" style={inputStyle} />
            <TextInput placeholder="TC (11 hane)" value={tc} onChangeText={setTc} keyboardType="number-pad" style={inputStyle} />
            <TextInput placeholder="Şifre" value={password} onChangeText={setPassword} secureTextEntry style={inputStyle} />

            <TextInput placeholder="Adres başlığı (Ev/İş)" value={addressTitle} onChangeText={setAddressTitle} style={inputStyle} />
            <TextInput placeholder="Şehir" value={city} onChangeText={setCity} style={inputStyle} />
            <TextInput placeholder="İlçe" value={district} onChangeText={setDistrict} style={inputStyle} />
            <TextInput placeholder="Mahalle (opsiyonel)" value={neighborhood} onChangeText={setNeighborhood} style={inputStyle} />
            <TextInput placeholder="Açık adres" value={addressLine} onChangeText={setAddressLine} multiline style={[inputStyle, { height: 90 }]} />
            <TextInput placeholder="Posta kodu (opsiyonel)" value={postal} onChangeText={setPostal} keyboardType="number-pad" style={inputStyle} />

            <Button title={loading ? "..." : "Kayıt Ol"} onPress={onRegister} disabled={loading} />
        </ScrollView>
    );
}
