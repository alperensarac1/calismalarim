import React, {useEffect, useState} from 'react';
import {ActivityIndicator, View} from 'react-native';
import {NavigationContainer} from '@react-navigation/native';
import {createNativeStackNavigator} from '@react-navigation/native-stack';

import {SessionManager} from '../core/sessionManager';
import {RootStackParamList} from './routes';
import {LoginScreen} from '../screens/auth/LoginScreen';
import {RegisterScreen} from '../screens/auth/RegisterScreen';
import {HomeScreen} from '../screens/home/HomeScreen';
import {EventDetailScreen} from '../screens/event/EventDetailScreen';
import {MyTicketsScreen} from '../screens/tickets/MyTicketsScreen';
import {TicketDetailScreen} from '../screens/tickets/TicketDetailScreen';
import {TicketScannerScreen} from "../screens/scanner/TicketScannerScreen";

const Stack = createNativeStackNavigator<RootStackParamList>();

function SplashScreen() {
    return (
        <View
            style={{
                flex: 1,
                alignItems: 'center',
                justifyContent: 'center',
            }}>
            <ActivityIndicator size="large" />
        </View>
    );
}

export function RootNavigator() {
    const [isLoading, setIsLoading] = useState(true);
    const [initialRoute, setInitialRoute] =
        useState<keyof RootStackParamList>('Login');

    useEffect(() => {
        checkSession();
    }, []);

    async function checkSession() {
        const loggedIn = await SessionManager.isLoggedIn();

        setInitialRoute(loggedIn ? 'Home' : 'Login');
        setIsLoading(false);
    }

    if (isLoading) {
        return <SplashScreen />;
    }

    return (
        <NavigationContainer>
            <Stack.Navigator initialRouteName={initialRoute}>
                <Stack.Screen
                    name="Login"
                    component={LoginScreen}
                    options={{
                        headerShown: false,
                    }}
                />

                <Stack.Screen
                    name="Register"
                    component={RegisterScreen}
                    options={{
                        title: 'Kayıt Ol',
                    }}
                />

                <Stack.Screen
                    name="Home"
                    component={HomeScreen}
                    options={{
                        title: 'Etkinlikler',
                    }}
                />

                <Stack.Screen
                    name="EventDetail"
                    component={EventDetailScreen}
                    options={{
                        title: 'Etkinlik Detayı',
                    }}
                />

                <Stack.Screen
                    name="MyTickets"
                    component={MyTicketsScreen}
                    options={{
                        title: 'Biletlerim',
                    }}
                />

                <Stack.Screen
                    name="TicketDetail"
                    component={TicketDetailScreen}
                    options={{
                        title: 'Bilet Detayı',
                    }}
                />
                <Stack.Screen
                    name="TicketScanner"
                    component={TicketScannerScreen}
                    options={{
                        title: 'QR Kontrol',
                    }}
                />
            </Stack.Navigator>
        </NavigationContainer>
    );
}