import React from "react";
import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";

import RoomListScreen from "./screens/RoomListScreen";
import RadioPlayerScreen from "./screens/RadioPlayerScreen";
import { RadioRoom } from "./models/RadioRoom";

export type RootStackParamList = {
  RoomList: undefined;
  RadioPlayer: {
    room: RadioRoom;
  };
};

const Stack = createNativeStackNavigator<RootStackParamList>();

export default function App() {
  return (
      <NavigationContainer>
        <Stack.Navigator>
          <Stack.Screen
              name="RoomList"
              component={RoomListScreen}
              options={{ title: "SyncRadio Odaları" }}
          />

          <Stack.Screen
              name="RadioPlayer"
              component={RadioPlayerScreen}
              options={{ title: "Dinleyici Modu" }}
          />
        </Stack.Navigator>
      </NavigationContainer>
  );
}