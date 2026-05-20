import React, { useState } from "react";
import { SafeAreaView, StyleSheet } from "react-native";
import { AppScreen } from "./src/types/AppScreen";
import { HomeScreen } from "./src/screens/HomeScreen";
import { RoomListScreen } from "./src/screens/RoomListScreen";
import { ViewerScreen } from "./src/screens/ViewerScreen";
import { BroadcasterScreen } from "./src/screens/BroadcasterScreen";

export default function App() {
  const [screen, setScreen] = useState<AppScreen>({
    name: "home",
  });

  return (
      <SafeAreaView style={styles.container}>
        {screen.name === "home" && (
            <HomeScreen
                onStartBroadcast={() => {
                  setScreen({ name: "broadcaster" });
                }}
                onWatchBroadcasts={() => {
                  setScreen({ name: "room_list" });
                }}
            />
        )}

        {screen.name === "room_list" && (
            <RoomListScreen
                onBack={() => {
                  setScreen({ name: "home" });
                }}
                onRoomPress={room => {
                  setScreen({
                    name: "viewer",
                    room,
                  });
                }}
            />
        )}

        {screen.name === "viewer" && (
            <ViewerScreen
                room={screen.room}
                onBack={() => {
                  setScreen({ name: "room_list" });
                }}
            />
        )}

        {screen.name === "broadcaster" && (
            <BroadcasterScreen
                onBack={() => {
                  setScreen({ name: "home" });
                }}
            />
        )}
      </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
});