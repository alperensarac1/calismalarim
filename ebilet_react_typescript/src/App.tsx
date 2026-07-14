import { Navigate, Route, Routes } from "react-router-dom";

import { SessionManager } from "./core/sessionManager";
import { LoginPage } from "./pages/LoginPage";
import { RegisterPage } from "./pages/RegisterPage";
import { HomePage } from "./pages/HomePage";
import { EventDetailPage } from "./pages/EventDetailPage";
import { MyTicketsPage } from "./pages/MyTicketsPage";
import { TicketDetailPage } from "./pages/TicketDetailPage";

function ProtectedRoute({ children }: { children: React.ReactNode }) {
    if (!SessionManager.isLoggedIn()) {
        return <Navigate to="/login" replace />;
    }

    return children;
}

function PublicRoute({ children }: { children: React.ReactNode }) {
    if (SessionManager.isLoggedIn()) {
        return <Navigate to="/" replace />;
    }

    return children;
}

export default function App() {
    return (
        <Routes>
            <Route
                path="/login"
                element={
                    <PublicRoute>
                        <LoginPage />
                    </PublicRoute>
                }
            />

            <Route
                path="/register"
                element={
                    <PublicRoute>
                        <RegisterPage />
                    </PublicRoute>
                }
            />

            <Route
                path="/"
                element={
                    <ProtectedRoute>
                        <HomePage />
                    </ProtectedRoute>
                }
            />

            <Route
                path="/events/:eventId"
                element={
                    <ProtectedRoute>
                        <EventDetailPage />
                    </ProtectedRoute>
                }
            />

            <Route
                path="/tickets"
                element={
                    <ProtectedRoute>
                        <MyTicketsPage />
                    </ProtectedRoute>
                }
            />

            <Route
                path="/tickets/:ticketId"
                element={
                    <ProtectedRoute>
                        <TicketDetailPage />
                    </ProtectedRoute>
                }
            />
        </Routes>
    );
}