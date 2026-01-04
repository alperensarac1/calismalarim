import React, { useEffect, useState } from "react";
import Toast from "react-native-toast-message";
import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import { Prefs } from "./src/storage/prefs";
import StudentSetupScreen from "./src/screens/StudentSetupScreen";
import ScanScreen from "./src/screens/ScanScreen";
import WebScreen from "./src/screens/WebScreen";
import { ActivityIndicator, View } from "react-native";
import "react-native-gesture-handler";
import { enableScreens } from "react-native-screens";


import { createStackNavigator } from "@react-navigation/stack";
type RootStackParamList = {
  StudentSetup: undefined;
  Scan: { studentNo: string };
  Web: { title: string; url: string };
};

const Stack = createNativeStackNavigator<RootStackParamList>();

export default function App() {
  const [loading, setLoading] = useState(true);
  const [studentNo, setStudentNo] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      const no = await Prefs.getStudentNo();
      setStudentNo(no);
      setLoading(false);
    })();
  }, []);

  if (loading) {
    return (
        <View style={{ flex: 1, alignItems: "center", justifyContent: "center" }}>
          <ActivityIndicator size="large" />
        </View>
    );
  }

  return (
      <>
        <NavigationContainer>
          <Stack.Navigator>
            {studentNo ? (
                <Stack.Screen
                    name="Scan"
                    component={ScanScreen}
                    initialParams={{ studentNo }}
                    options={{ headerShown: false }}
                />
            ) : (
                <Stack.Screen
                    name="StudentSetup"
                    component={StudentSetupScreen}
                    options={{ title: "Öğrenci Girişi" }}
                />
            )}

            <Stack.Screen
                name="Web"
                component={WebScreen}
                options={({ route }) => ({
                  title: route.params.title,
                })}
            />
          </Stack.Navigator>
        </NavigationContainer>

        <Toast />
      </>
  );
}
