import React from "react";
import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import type { RootStackParamList } from "./types";
import MainScreen from "../screens/MainScreen";
import DetailsScreen from "../screens/DetailsScreen";

const Stack = createNativeStackNavigator<RootStackParamList>();

export default function AppNav() {
    return (
        <NavigationContainer>
            <Stack.Navigator>
                <Stack.Screen name="Main" component={MainScreen} options={{ title: "CSV Explorer" }} />
    <Stack.Screen name="Details" component={DetailsScreen} options={{ title: "Row Details" }} />
    </Stack.Navigator>
    </NavigationContainer>
);
}
