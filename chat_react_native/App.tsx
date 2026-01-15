import React from "react";
import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import BootGate from "./src/view/boot_gate";
import RegisterScreen from "./src/view/register_screen";
import ChatListScreen from "./src/view/chat_list_screen";
import SingleChatScreen from "./src/view/single_chat_screen";



export type RootStackParamList = {
  BootGate: undefined;
  Register: undefined;
  Chats: undefined;
  SingleChat: { aliciId: number; aliciAd: string };
};

const Stack = createNativeStackNavigator<RootStackParamList>();

export default function App() {
  return (
      <NavigationContainer>
        <Stack.Navigator>
          <Stack.Screen name="BootGate" component={BootGate} options={{ headerShown: false }} />
          <Stack.Screen name="Register" component={RegisterScreen} options={{ title: "Kayıt Ol" }} />
          <Stack.Screen name="Chats" component={ChatListScreen} options={{ title: "Mesajlar" }} />
          <Stack.Screen
              name="SingleChat"
              component={SingleChatScreen}
              options={({ route }) => ({ title: route.params.aliciAd })}
          />
        </Stack.Navigator>
      </NavigationContainer>
  );
}
