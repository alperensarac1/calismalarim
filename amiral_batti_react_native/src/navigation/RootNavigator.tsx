import React from "react";
import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";

import LobbyScreen from "../screens/LobbyScreen";
import PlacementScreen from "../screens/PlacementScreen";
import GameScreen from "../screens/GameScreen";
import { RootStackParamList } from "./types";

const Stack = createNativeStackNavigator<RootStackParamList>();

export default function RootNavigator() {
    return (
        <NavigationContainer>
            <Stack.Navigator initialRouteName="Lobby">
                <Stack.Screen name="Lobby" component={LobbyScreen} />
                <Stack.Screen name="Placement" component={PlacementScreen} />
                <Stack.Screen name="Game" component={GameScreen} />
            </Stack.Navigator>
        </NavigationContainer>
    );
}
