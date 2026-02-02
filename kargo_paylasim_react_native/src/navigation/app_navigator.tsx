import React from "react";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import LoginScreen from "../screens/login_screen";
import RegisterScreen from "../screens/register_screen";
import HomeScreen from "../screens/home_screen";
import CreateShipmentScreen from "../screens/create_shipment_screen";
import CreateAddressScreen from "../screens/create_address_screen";


export type RootStackParamList = {
    Login: undefined;
    Register: undefined;
    Home: undefined;
    CreateShipment: undefined;
    CreateAddress: undefined;
};

const Stack = createNativeStackNavigator<RootStackParamList>();

export default function AppNavigator({ isAuthed }: { isAuthed: boolean }) {
    return (
        <Stack.Navigator>
            {!isAuthed ? (
        <>
            <Stack.Screen name="Login" component={LoginScreen} options={{ title: "Giriş" }} />
    <Stack.Screen name="Register" component={RegisterScreen} options={{ title: "Kayıt Ol" }} />
    </>
) : (
        <>
            <Stack.Screen name="Home" component={HomeScreen} options={{ title: "Home" }} />
    <Stack.Screen name="CreateShipment" component={CreateShipmentScreen} options={{ title: "Yeni Gönderi" }} />
    <Stack.Screen name="CreateAddress" component={CreateAddressScreen} options={{ title: "Adres Ekle" }} />
    </>
)}
    </Stack.Navigator>
);
}
