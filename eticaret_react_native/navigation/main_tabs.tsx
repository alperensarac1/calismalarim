import React from "react";
import { createBottomTabNavigator } from "@react-navigation/bottom-tabs";
import HomeScreen from "../screens/home_screen";
import CartScreen from "../screens/cart_screen";
import OrdersScreen from "../screens/orders_screen";
import SettingsScreen from "../screens/settings_screen";


const Tab = createBottomTabNavigator();

export default function MainTabs() {
    return (
        <Tab.Navigator>
            <Tab.Screen name="Home" component={HomeScreen} options={{ title: "Anasayfa" }} />
            <Tab.Screen name="Cart" component={CartScreen} options={{ title: "Sepet" }} />
            <Tab.Screen name="Orders" component={OrdersScreen} options={{ title: "Siparişler" }} />
            <Tab.Screen name="Settings" component={SettingsScreen} options={{ title: "Ayarlar" }} />
        </Tab.Navigator>
    );
}
