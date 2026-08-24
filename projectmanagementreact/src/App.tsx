import { useEffect } from 'react';

import { AppRouter } from './app/router/AppRouter';
import { useAuthStore } from './features/auth/store/authStore';
import {GlobalNotification} from "./features/notifications/components/GlobalNotification.tsx";

function App() {
  const initializeAuth = useAuthStore(
      (state) => state.initializeAuth,
  );

  useEffect(() => {
    void initializeAuth();
  }, [initializeAuth]);

  return (
      <>
        <AppRouter />

        <GlobalNotification />
      </>
  );
}

export default App;