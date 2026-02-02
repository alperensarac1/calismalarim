import React, { useState } from "react";
import { Alert, Button, Text, TextInput, View } from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";
import { Endpoints } from "../api/endpoints";
import { postJSON, APIError } from "../api/client";
import type { LookupReceiverData, CreateShipmentData } from "../api/types";
import {RootStackParamList} from "../navigation/app_navigator";
import {isLikelyTrPhoneE164, normalizeTrToE164} from "../util/phone";

type Props = NativeStackScreenProps<RootStackParamList, "CreateShipment">;

export default function CreateShipmentScreen({ navigation }: Props) {
    const [phone, setPhone] = useState("");
    const [lookupText, setLookupText] = useState<string | null>(null);
    const [canConfirm, setCanConfirm] = useState(false);
    const [confirmedPhone, setConfirmedPhone] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    function reset() {
        setLookupText(null);
        setCanConfirm(false);
        setConfirmedPhone(null);
    }

    async function lookup() {
        reset();
        const normalized = normalizeTrToE164(phone);
        if (!isLikelyTrPhoneE164(normalized)) {
            Alert.alert("Uyarı", "Telefon formatı hatalı. Örn: 05xx... veya +905xx...");
            return;
        }
        setPhone(normalized);

        setLoading(true);
        try {
            const res = await postJSON<LookupReceiverData>(Endpoints.receiverLookup, { phone: normalized });
            if (!res.ok || !res.data) throw new APIError(res.error ?? "User not found");
            setLookupText(`Bulunan: ${res.data.masked_first_name} ${res.data.masked_last_name} • Onaylıyor musun?`);
            setCanConfirm(true);
            setConfirmedPhone(normalized);
        } catch (e: any) {
            Alert.alert("Hata", e?.message ?? String(e));
        } finally {
            setLoading(false);
        }
    }

    async function confirmCreate() {
        if (!confirmedPhone) {
            Alert.alert("Uyarı", "Önce kişiyi bul.");
            return;
        }

        setLoading(true);
        try {
            const res = await postJSON<CreateShipmentData>(Endpoints.shipmentCreate, {
                receiver_phone: confirmedPhone,
                sender_address_id: null,
            });

            if (!res.ok || !res.data) {
                const msg = res.error ?? "Create shipment failed";
                if (msg.toLowerCase().includes("receiver address not found") || msg.toUpperCase().includes("RECEIVER_ADDRESS_MISSING")) {
                    throw new APIError("Bu kullanıcı henüz adresini kaydetmemiş.");
                }
                throw new APIError(msg);
            }

            Alert.alert("Gönderi Oluşturuldu", `Kod: ${res.data.pickup_code}\nSon geçerlilik: ${res.data.code_expires_at}`, [
                { text: "Tamam", onPress: () => navigation.goBack() },
            ]);
        } catch (e: any) {
            Alert.alert("Hata", e?.message ?? String(e));
        } finally {
            setLoading(false);
        }
    }

    return (
        <View style={{ padding: 16, gap: 10 }}>
            <TextInput
                placeholder="Alıcı Telefon"
                value={phone}
                onChangeText={setPhone}
                keyboardType="phone-pad"
                style={{ borderWidth: 1, borderColor: "#ccc", padding: 12, borderRadius: 10 }}
            />

            <View style={{ flexDirection: "row", gap: 8 }}>
                <View style={{ flex: 1 }}>
                    <Button title={loading ? "..." : "Bul"} onPress={lookup} disabled={loading} />
                </View>
                <Button title="İptal" onPress={reset} disabled={loading} />
            </View>

            {!!lookupText && <Text style={{ marginTop: 10 }}>{lookupText}</Text>}

            <View style={{ marginTop: 20 }}>
                <Button title="Onayla ve Oluştur" onPress={confirmCreate} disabled={!canConfirm || loading} />
            </View>
        </View>
    );
}
