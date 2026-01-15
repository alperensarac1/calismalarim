import React, { useEffect } from "react";
import { View, ActivityIndicator } from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";

import type { RootStackParamList } from "../../App";
import {PrefManager} from "../util/pref_manager";
import {AppConfig} from "../util/app_config";


type Props = NativeStackScreenProps<RootStackParamList, "BootGate">;

export default function BootGate({ navigation }: Props) {
    useEffect(() => {
        (async () => {
            const id = await PrefManager.getirKullaniciId();
            if (id !== -1) {
                AppConfig.kullaniciId = id;
                navigation.reset({ index: 0, routes: [{ name: "Chats" }] });
            } else {
                navigation.reset({ index: 0, routes: [{ name: "Register" }] });
            }
        })();
    }, [navigation]);

    return (
        <View style={{ flex: 1, alignItems: "center", justifyContent: "center" }}>
    <ActivityIndicator size="large" />
        </View>
);
}
