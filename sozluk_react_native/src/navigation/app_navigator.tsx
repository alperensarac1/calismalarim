import React, { useEffect, useState } from "react";
import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import { createBottomTabNavigator } from "@react-navigation/bottom-tabs";

import { Routes, RootStackParamList, TabsParamList } from "./routes";
import { SessionManager } from "../entity/session_manager";
import GundemScreen from "../screens/gundem_screen";
import BugunScreen from "../screens/bugun_screen";
import ProfilScreen from "../screens/ProfilScreen";
import LoginScreen from "../screens/login_screen";
import RegisterScreen from "../screens/register_screen";
import EntryEkleScreen from "../screens/entryekle_screen";
import EntryDetayScreen from "../screens/entrydetay_screen";



const Stack = createNativeStackNavigator<RootStackParamList>();
const Tabs = createBottomTabNavigator<TabsParamList>();

function TabsNavigator() {
    return (
        <Tabs.Navigator screenOptions={{ headerShown: true }}>
    <Tabs.Screen
        name={Routes.GUNDEM}
    component={GundemScreen}
    options={{ title: "Gündem" }}
    />
    <Tabs.Screen
    name={Routes.BUGUN}
    component={BugunScreen}
    options={{ title: "Bugün" }}
    />
    <Tabs.Screen
    name={Routes.PROFIL}
    component={ProfilScreen}
    options={{ title: "Profil" }}
    />
    </Tabs.Navigator>
);
}

export default function AppNavigator() {
    const [initialRoute, setInitialRoute] = useState<keyof RootStackParamList>(
        Routes.LOGIN
    );
    const [ready, setReady] = useState(false);

    useEffect(() => {
        let mounted = true;

        (async () => {
            try {
                const loggedIn = await SessionManager.isLoggedIn();
                if (!mounted) return;
                setInitialRoute(loggedIn ? Routes.TABS : Routes.LOGIN);
            } finally {
                if (mounted) setReady(true);
            }
        })();

        return () => {
            mounted = false;
        };
    }, []);

    if (!ready) return null;

    return (
        <NavigationContainer>
            <Stack.Navigator
                initialRouteName={initialRoute}
    screenOptions={{ headerShown: false }}
>
    <Stack.Screen name={Routes.LOGIN} component={LoginScreen} />
    <Stack.Screen name={Routes.REGISTER} component={RegisterScreen} />

    <Stack.Screen name={Routes.TABS} component={TabsNavigator} />

    {/* actions */}
    <Stack.Screen name={Routes.ENTRY_ADD} component={EntryEkleScreen} />
    <Stack.Screen name={Routes.ENTRY_DETAIL} component={EntryDetayScreen} />
    </Stack.Navigator>
    </NavigationContainer>
);
}
