import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { ApiService } from "../core/apiService";
import { SessionManager } from "../core/sessionManager";
import { AppButton } from "../components/AppButton";
import { AppTextField } from "../components/AppTextField";

export function LoginPage() {
    const navigate = useNavigate();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const [loading, setLoading] = useState(false);

    function isValidEmail(value: string): boolean {
        return /^\S+@\S+\.\S+$/.test(value);
    }

    async function login() {
        const cleanEmail = email.trim();
        const cleanPassword = password.trim();

        if (cleanEmail.length === 0) {
            alert("E-posta zorunludur.");
            return;
        }

        if (!isValidEmail(cleanEmail)) {
            alert("Geçerli bir e-posta giriniz.");
            return;
        }

        if (cleanPassword.length === 0) {
            alert("Şifre zorunludur.");
            return;
        }

        if (cleanPassword.length < 6) {
            alert("Şifre en az 6 karakter olmalıdır.");
            return;
        }

        try {
            setLoading(true);

            const response = await ApiService.login({
                email: cleanEmail,
                password: cleanPassword,
            });

            setLoading(false);

            if (!response.success) {
                alert(response.message);
                return;
            }

            if (!response.data) {
                alert("Kullanıcı bilgisi alınamadı.");
                return;
            }

            SessionManager.saveUser(response.data);

            navigate("/", {
                replace: true,
            });
        } catch (error) {
            setLoading(false);

            const message =
                error instanceof Error ? error.message : "Bilinmeyen hata oluştu.";

            alert(message);
        }
    }

    return (
        <main className="auth-page">
            <section className="auth-header">
                <div className="auth-icon">🎟️</div>
                <h1>Etkinlik Bileti</h1>
                <p>Etkinlikleri keşfet, biletini kolayca satın al.</p>
            </section>

            <section className="auth-card">
                <h2>Giriş Yap</h2>

                <div className="form-group">
                    <AppTextField
                        value={email}
                        placeholder="E-posta"
                        type="email"
                        onChange={setEmail}
                    />
                </div>

                <div className="form-group">
                    <AppTextField
                        value={password}
                        placeholder="Şifre"
                        type="password"
                        onChange={setPassword}
                    />
                </div>

                <AppButton
                    title="Giriş Yap"
                    loading={loading}
                    color="blue"
                    onClick={login}
                />

                <Link className="auth-link" to="/register">
                    Hesabın yok mu? Kayıt ol
                </Link>
            </section>
        </main>
    );
}