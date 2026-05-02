
import React, { useState } from "react";
import { SplashScreen } from "./src/screens/SplashScreen";
import { LoginScreen } from "./src/screens/LoginScreen";
import { RegisterScreen } from "./src/screens/RegisterScreen";
import { CustomerHomeScreen } from "./src/screens/CustomerHomeScreen";
import { DriverHomeScreen } from "./src/screens/DriverHomeScreen";

export type AppRoute =
    | "splash"
    | "login"
    | "register"
    | "customerHome"
    | "driverHome";

export default function App() {
  const [route, setRoute] = useState<AppRoute>("splash");

  if (route === "splash") {
    return <SplashScreen onRoute={setRoute} />;
  }

  if (route === "login") {
    return <LoginScreen onRoute={setRoute} />;
  }

  if (route === "register") {
    return <RegisterScreen onRoute={setRoute} />;
  }

  if (route === "driverHome") {
    return <DriverHomeScreen onRoute={setRoute} />;
  }

  return <CustomerHomeScreen onRoute={setRoute} />;
}