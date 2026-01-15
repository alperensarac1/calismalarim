import React from 'react';
import {AuthProvider} from "./src/store/auth_context";
import { RoomsProvider } from "./src/store/rooms_context";
import AppNavigator from "./src/navigation/app_navigator";
import {MediaProvider} from "./src/store/media_context";


export function App() {
  return (
      <AuthProvider>
        <RoomsProvider>
          <MediaProvider>
            <AppNavigator/>
          </MediaProvider>
        </RoomsProvider>
      </AuthProvider>
  );
}
