import React from "react";
import { View, Text, Image } from "react-native";
import {Mesaj} from "../model/mesaj";


export default function MessageBubble({ msg, benimId }: { msg: Mesaj; benimId: number }) {
    const benMi = msg.gonderen_id === benimId;

    return (
        <View style={{ alignItems: benMi ? "flex-end" : "flex-start", marginVertical: 6 }}>
            <View
                style={{
                    maxWidth: "80%",
                    backgroundColor: benMi ? "#DBEAFE" : "#E5E7EB",
                    padding: 10,
                    borderRadius: 12,
                }}
            >
                {msg.resim_var === 1 && msg.resim_url ? (
                    <Image
                        source={{ uri: msg.resim_url }}
                        style={{ width: 220, height: 220, borderRadius: 10, marginBottom: 8 }}
                        resizeMode="cover"
                    />
                ) : null}

                {msg.mesaj_text ? <Text style={{ fontSize: 15 }}>{msg.mesaj_text}</Text> : null}

                <Text style={{ fontSize: 11, color: "#555", marginTop: 6 }}>{msg.tarih}</Text>
            </View>
        </View>
    );
}
