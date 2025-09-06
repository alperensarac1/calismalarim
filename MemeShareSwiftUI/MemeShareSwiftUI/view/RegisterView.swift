

import Foundation
import SwiftUI

struct RegisterView: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var vm = RegisterViewModel()
    @State private var username = ""
    @State private var password = ""
    @State private var toast: Toast? = nil
    @FocusState private var focusedField: Field?

    enum Field { case username, password }

    var body: some View {
        VStack(spacing: 16) {
            Text("Kayıt Ol").font(.largeTitle).bold()

            TextField("Kullanıcı adı", text: $username)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
                .textContentType(.username)
                .padding().background(.gray.opacity(0.1)).clipShape(RoundedRectangle(cornerRadius: 12))
                .focused($focusedField, equals: .username)

            SecureField("Şifre", text: $password)
                .textContentType(.newPassword)
                .padding().background(.gray.opacity(0.1)).clipShape(RoundedRectangle(cornerRadius: 12))
                .focused($focusedField, equals: .password)

            Button {
                guard !username.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty,
                      !password.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
                    withAnimation { toast = Toast(message: "Tüm alanları doldurun") }
                    return
                }
                focusedField = nil
                vm.registerUser(username: username.trimmingCharacters(in: .whitespacesAndNewlines),
                                password: password.trimmingCharacters(in: .whitespacesAndNewlines))
            } label: {
                HStack {
                    if vm.isLoading { ProgressView().padding(.trailing, 6) }
                    Text("Kayıt Ol")
                }
                .frame(maxWidth: .infinity)
            }
            .disabled(vm.isLoading)
            .buttonStyle(.borderedProminent)

            Button("Giriş Ekranına Dön") { dismiss() }
                .padding(.top, 8)

            Spacer(minLength: 0)
        }
        .padding()
        .toast($toast)
        .onChange(of: vm.registerResult) { res in
            guard let res else { return }
            if res.success {
                withAnimation { toast = Toast(message: "Kayıt başarılı! Giriş yapabilirsiniz") }
                DispatchQueue.main.asyncAfter(deadline: .now() + 1.25) {
                    dismiss()
                }
            } else {
                withAnimation { toast = Toast(message: "Hata: \(res.message ?? "Bilinmeyen")") }
            }
        }
    }
}
