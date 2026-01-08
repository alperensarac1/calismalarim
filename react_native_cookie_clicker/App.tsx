import { StatusBar } from 'expo-status-bar';
import { StyleSheet, Text, View } from 'react-native';

// App.tsx
import React, { useEffect } from "react";
import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import MainScreen from "./src/screens/main_screen";
import PrestigeShopScreen from "./src/screens/prestige_shop_screen";
import {useGameStore} from "./src/data/game_store";

export type RootStackParamList = {
  Main: undefined;
  Shop: undefined;
};

const Stack = createNativeStackNavigator<RootStackParamList>();

export default function App() {
  const hydrate = useGameStore(s => s.hydrate);
  const stopLoop = useGameStore(s => s.stopLoop);

  useEffect(() => {
    void hydrate();
    return () => stopLoop();
  }, [hydrate, stopLoop]);

  return (
      <NavigationContainer>
        <Stack.Navigator initialRouteName="Main">
          <Stack.Screen name="Main" component={MainScreen} options={{ headerShown: false }} />
          <Stack.Screen name="Shop" component={PrestigeShopScreen} options={{ title: "Prestige Mağazası" }} />
        </Stack.Navigator>
      </NavigationContainer>
  );
}
