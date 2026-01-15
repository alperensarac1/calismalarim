import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import {RootStackParamList, Routes} from "./routes";
import RegisterScreen from "../view/register_screen";
import HomeScreen from "../view/home_screen";
import LoginScreen from "../view/login_screen";
import OdaScreen from "../view/oda_screen";


const Stack = createNativeStackNavigator<RootStackParamList>();

export default function AppNavigator() {
    return (
        <NavigationContainer>
            <Stack.Navigator initialRouteName={Routes.LOGIN}>
                <Stack.Screen name={Routes.LOGIN} component={LoginScreen} options={{ title: 'Giriş' }} />
                <Stack.Screen name={Routes.REGISTER} component={RegisterScreen} options={{ title: 'Kayıt' }} />
                <Stack.Screen name={Routes.HOME} component={HomeScreen} options={{ title: 'Odalarım' }} />
                <Stack.Screen
                    name={Routes.ODA}
                    component={OdaScreen}
                    options={({ route }) => ({ title: `Oda #${route.params.roomId}` })}
                />
            </Stack.Navigator>
        </NavigationContainer>
    );
}
