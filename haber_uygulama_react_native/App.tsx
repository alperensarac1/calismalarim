// App.tsx
import React from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import AppNavigator from "./src/navigation/app_navigator";


const qc = new QueryClient();

export default function App() {
  return (
      <QueryClientProvider client={qc}>
        <AppNavigator />
      </QueryClientProvider>
  );
}
