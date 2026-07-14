import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { ApiService } from "../core/apiService";
import { SessionManager } from "../core/sessionManager";
import { AppButton } from "../components/AppButton";
import { AppTextField } from "../components/AppTextField";

export function RegisterPage() {
    const navigate = useNavigate();

    const [fullName, setFullName] = useState("");
    const [email, setEmail] = useState("");
    const [phone, setPhone] = useState("");
    const [password, setPassword] = useState("");

    const [loading, setLoading] = useState(false);

    function isValidEmail(value: string): boolean {
        return /^\S+@\S+\.\S+$/.test(value);
    }

    async function register() {
        const cleanFullName = fullName.trim();
        const cleanEmail = email.trim();
        const cleanPhone = phone.trim();
        const cleanPassword = password.trim();

        if (cleanFullName.length === 0) {
            alert("Ad soyad zorunludur.");
            return;
        }

        if (cleanFullName.length < 3) {
            alert("Ad soyad en az 3 karakter olmalıdır.");
            return;
        }

        if (cleanEmail.length === 0) {
            alert("E-posta zorunludur.");
            return;
        }

        if (!isValidEmail(cleanEmail)) {
            alert("Geçerli bir e-posta giriniz.");
            return;
        }

        if (cleanPhone.length > 0 && cleanPhone.length < 10) {
            alert("Telefon numarası eksik görünüyor.");
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

            const response = await ApiService.register({
                fullName: cleanFullName,
                email: cleanEmail,
                phone: cleanPhone,
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
                <div className="auth-icon">👤</div>
                <h1>Yeni Hesap Oluştur</h1>
                <p>Etkinlik biletlerini kolayca satın almak için kayıt ol.</p>
            </section>

            <section className="auth-card">
                <h2>Kayıt Bilgileri</h2>

                <div className="form-group">
                    <AppTextField
                        value={fullName}
                        placeholder="Ad Soyad"
                        onChange={setFullName}
                    />
                </div>

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
                        value={phone}
                        placeholder="Telefon"
                        type="tel"
                        onChange={setPhone}
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
                    title="Kayıt Ol"
                    loading={loading}
                    color="green"
                    onClick={register}
                />

                <Link className="auth-link" to="/login">
                    Zaten hesabın var mı? Giriş yap
                </Link>
            </section>
        </main>
    );
}