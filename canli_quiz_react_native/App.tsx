import React, { useState } from "react";
import { SafeAreaView, StyleSheet } from "react-native";

import { AppScreen, ScoreItem } from "./src/models/types";
import { WebSocketManager } from "./src/socket/WebSocketManager";

import HomeScreen from "./src/screens/HomeScreen";
import CreateRoomScreen from "./src/screens/CreateRoomScreen";
import JoinRoomScreen from "./src/screens/JoinRoomScreen";
import OwnerRoomScreen from "./src/screens/OwnerRoomScreen";
import WaitingRoomScreen from "./src/screens/WaitingRoomScreen";
import QuizScreen from "./src/screens/QuizScreen";
import WinnerScreen from "./src/screens/WinnerScreen";

export default function App() {
  const [screen, setScreen] = useState<AppScreen>({ name: "home" });

  const goHome = () => {
    WebSocketManager.disconnect();
    setScreen({ name: "home" });
  };

  return (
      <SafeAreaView style={styles.container}>
        {screen.name === "home" && (
            <HomeScreen
                onCreateRoom={() => setScreen({ name: "create_room" })}
                onJoinRoom={() => setScreen({ name: "join_room" })}
            />
        )}

        {screen.name === "create_room" && (
            <CreateRoomScreen
                onBack={() => setScreen({ name: "home" })}
                onRoomCreated={(roomCode, username, questionTime) => {
                  setScreen({
                    name: "owner_room",
                    roomCode,
                    username,
                    questionTime,
                  });
                }}
            />
        )}

        {screen.name === "join_room" && (
            <JoinRoomScreen
                onBack={() => setScreen({ name: "home" })}
                onRoomJoined={(roomCode, username, questionTime) => {
                  setScreen({
                    name: "waiting_room",
                    roomCode,
                    username,
                    questionTime,
                  });
                }}
            />
        )}

        {screen.name === "owner_room" && (
            <OwnerRoomScreen
                roomCode={screen.roomCode}
                username={screen.username}
                questionTime={screen.questionTime}
                onQuizStarted={() => {
                  setScreen({
                    name: "quiz",
                    roomCode: screen.roomCode,
                    username: screen.username,
                    questionTime: screen.questionTime,
                    isOwner: true,
                  });
                }}
            />
        )}

        {screen.name === "waiting_room" && (
            <WaitingRoomScreen
                roomCode={screen.roomCode}
                username={screen.username}
                questionTime={screen.questionTime}
                onQuizStarted={() => {
                  setScreen({
                    name: "quiz",
                    roomCode: screen.roomCode,
                    username: screen.username,
                    questionTime: screen.questionTime,
                    isOwner: false,
                  });
                }}
            />
        )}

        {screen.name === "quiz" && (
            <QuizScreen
                roomCode={screen.roomCode}
                username={screen.username}
                questionTime={screen.questionTime}
                onQuizFinished={(winners: ScoreItem[], scoreboard: ScoreItem[]) => {
                  setScreen({
                    name: "winner",
                    winners,
                    scoreboard,
                  });
                }}
            />
        )}

        {screen.name === "winner" && (
            <WinnerScreen
                winners={screen.winners}
                scoreboard={screen.scoreboard}
                onBackHome={goHome}
            />
        )}
      </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#F8FAFC",
  },
});