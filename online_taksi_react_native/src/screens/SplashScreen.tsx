import React, { useEffect } from "react";
import { ActivityIndicator, StyleSheet, Text, View } from "react-native";
import { AppRoute } from "../../App";
import { SessionManager } from "../core/sessionManager";

type Props = {
    onRoute: (route: AppRoute) => void;
};

export function SplashScreen({ onRoute }: Props) {
    useEffect(() => {
        async function routeUser() {
            await new Promise((resolve) => setTimeout(resolve, 700));

            const loggedIn = await SessionManager.isLoggedIn();
            const role = await SessionManager.getRole();

            if (!loggedIn) {
                onRoute("login");
                return;
            }

            onRoute(role === "driver" ? "driverHome" : "customerHome");
        }

        routeUser();
    }, []);

    return (
        <View style={styles.container}>
            <Text style={styles.title}>onlinetaksi</Text>
            <ActivityIndicator style={{ marginTop: 16 }} />
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        alignItems: "center",
        justifyContent: "center",
    },
    title: {
        fontSize: 32,
        fontWeight: "800",
    },
});