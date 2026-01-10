import React from "react";
import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import HomeScreen from "../screens/home_screen";
import DetailScreen from "../screens/detail_screen";
import CategoryScreen from "../screens/category_screen";
import {RootStackParamList} from "./routes";



const Stack = createNativeStackNavigator<RootStackParamList>();

export default function AppNavigator() {
    return (
        <NavigationContainer>
            <Stack.Navigator>
                <Stack.Screen name="Home" component={HomeScreen} options={{ title: "Haberler" }} />
                <Stack.Screen name="Detail" component={DetailScreen} options={{ title: "Detay" }} />
                <Stack.Screen name="Category" component={CategoryScreen} options={{ title: "Kategori" }} />
            </Stack.Navigator>
        </NavigationContainer>
    );
}
