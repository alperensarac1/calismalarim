import React, { useEffect, useState } from "react";
import { NavigationContainer } from "@react-navigation/native";

import { ActivityIndicator, View } from "react-native";
import {tokenStore} from "./storage/token_store";
import AppNavigator from "./navigation/app_navigator";

export default function AppRoot() {
    const [loading, setLoading] = useState(true);
    const [isAuthed, setAuthed] = useState(false);

    useEffect(() => {
        (async () => {
            const t = await tokenStore.get();
            setAuthed(!!t);
            setLoading(false);
        })();
    }, []);

    if (loading) {
        return (
            <View style={{ flex: 1, alignItems: "center", justifyContent: "center" }}>
                <ActivityIndicator />
            </View>
        );
    }

    return (
        <NavigationContainer>
            <AppNavigator isAuthed={isAuthed} />
        </NavigationContainer>
    );
}
