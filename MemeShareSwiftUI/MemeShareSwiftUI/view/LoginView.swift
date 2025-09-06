import Foundation
import SwiftUI
import Combine

struct LoginView: View {
    @StateObject private var vm = LoginViewModel()
    @State private var username = ""
    @State private var password = ""
    @State private var isLoggingIn = false
    @State private var toast: Toast? = nil
    @FocusState private var focusedField: Field?

    @State private var goHome = false
    @State private var homeUserId: Int?

    enum Field { case username, password }

    var body: some View {
        NavigationStack {
            VStack(spacing: 16) {
                Text("Giriş Yap").font(.largeTitle).bold()

                TextField("Kullanıcı adı", text: $username)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                    .textContentType(.username)
                    .padding().background(.gray.opacity(0.1)).clipShape(RoundedRectangle(cornerRadius: 12))
                    .focused($focusedField, equals: .username)

                SecureField("Şifre", text: $password)
                    .textContentType(.password)
                    .padding().background(.gray.opacity(0.1)).clipShape(RoundedRectangle(cornerRadius: 12))
                    .focused($focusedField, equals: .password)

                Button {
                    guard !username.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty,
                          !password.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
                        withAnimation { toast = Toast(message: "Tüm alanları doldurun") }
                        return
                    }
                    focusedField = nil
                    isLoggingIn = true
                    vm.loginUser(username: username.trimmingCharacters(in: .whitespacesAndNewlines),
                                 password: password.trimmingCharacters(in: .whitespacesAndNewlines))
                } label: {
                    HStack {
                        if isLoggingIn { ProgressView().padding(.trailing, 6) }
                        Text("Giriş Yap")
                    }
                    .frame(maxWidth: .infinity)
                }
                .disabled(isLoggingIn)
                .buttonStyle(.borderedProminent)

                NavigationLink("Kayıt Ol", destination: RegisterView())
                    .padding(.top, 8)

                NavigationLink("", isActive: $goHome) {
                    RoomsView(userId: homeUserId ?? -1)
                }
                .hidden()

                Spacer(minLength: 0)
            }
            .padding()
            .toast($toast)
            .onChange(of: vm.loginResult) {res in
                guard let res else { return }
                isLoggingIn = false
                if res.success, let uid = res.userId {
                    withAnimation { toast = Toast(message: "Giriş başarılı!") }
                    // Toast kapanınca yönlenelim (küçük gecikme)
                    DispatchQueue.main.asyncAfter(deadline: .now() + 1.25) {
                        homeUserId = uid
                        goHome = true
                    }
                } else {
                    withAnimation { toast = Toast(message: res.message ?? "Bilinmeyen hata") }
                }
            }
        }
    }
}
