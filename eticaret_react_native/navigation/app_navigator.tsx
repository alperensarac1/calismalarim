import React from "react";
import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import LoginScreen from "../screens/login_screen";
import RegisterScreen from "../screens/register_screen";
import MainTabs from "./main_tabs";
import ProductDetailScreen from "../screens/product_detail_screen";
import OrderDetailScreen from "../screens/order_detail_screen";


export type RootStackParamList = {
    Login: undefined;
    Register: undefined;
    Main: undefined;
    ProductDetail: { id: number };
    OrderDetail: { id: number };
};

const Stack = createNativeStackNavigator<RootStackParamList>();

export default function AppNavigator() {
    return (
        <NavigationContainer>
            <Stack.Navigator initialRouteName="Login">
                <Stack.Screen name="Login" component={LoginScreen} options={{ title: "Giriş" }} />
                <Stack.Screen name="Register" component={RegisterScreen} options={{ title: "Kayıt" }} />
                <Stack.Screen name="Main" component={MainTabs} options={{ headerShown: false }} />
                <Stack.Screen name="ProductDetail" component={ProductDetailScreen} options={{ title: "Ürün" }} />
                <Stack.Screen name="OrderDetail" component={OrderDetailScreen} options={{ title: "Sipariş Detay" }} />
            </Stack.Navigator>
        </NavigationContainer>
    );
}
