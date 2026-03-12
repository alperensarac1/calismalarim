import SwiftUI

struct RegisterView: View {
    @StateObject private var loc = LocationVM()
    @StateObject private var auth = AuthVM()

    @State private var role: Role = .HASTA
    @State private var ad = ""
    @State private var soyad = ""
    @State private var yas = ""
    @State private var telefon = ""
    @State private var sifre = ""
    @State private var info = ""

    var onAuthed: (Role) -> Void

    var body: some View {
        ScrollView {
            VStack(spacing: 12) {

                // 1) Başlık + rol
                Group {
                    Text("Kayıt").font(.title2).bold()

                    Picker("Rol", selection: $role) {
                        Text("Hasta").tag(Role.HASTA)
                        Text("Yardımcı").tag(Role.YARDIMCI)
                    }
                    .pickerStyle(.segmented)
                }

                // 2) Alanlar
                Group {
                    TextField("Ad", text: $ad).textFieldStyle(.roundedBorder)
                    TextField("Soyad", text: $soyad).textFieldStyle(.roundedBorder)
                    TextField("Yaş", text: $yas)
                        .keyboardType(.numberPad)
                        .textFieldStyle(.roundedBorder)
                    TextField("Telefon", text: $telefon).textFieldStyle(.roundedBorder)
                    SecureField("Şifre", text: $sifre).textFieldStyle(.roundedBorder)
                }

                // 3) Konum + butonlar
                Group {
                    Text(loc.statusText).font(.footnote)

                    Button("Konumdan Al") { loc.requestAndFetch() }
                        .buttonStyle(.bordered)

                    Button("Kayıt Ol") {
                        Task {
                            info = ""

                            guard let c = loc.city, let d = loc.district, !c.isEmpty, !d.isEmpty else {
                                info = "Şehir/ilçe tespit edilemedi"
                                return
                            }
                            guard !ad.isEmpty, !soyad.isEmpty, !telefon.isEmpty, !sifre.isEmpty else {
                                info = "Ad, soyad, telefon, şifre zorunlu"
                                return
                            }

                            let yasInt = Int(yas.trimmingCharacters(in: .whitespacesAndNewlines))

                            auth.loading = true
                            defer { auth.loading = false }

                            do {
                                let body = RegisterBody(role: role, ad: ad, soyad: soyad, yas: yasInt,
                                                        telefon: telefon, il: c, ilce: d, sifre: sifre)

                                let res: ApiOk<AnyCodable> = try await ApiClient.shared.post(
                                    "auth_register.php",
                                    body: body,
                                    response: ApiOk<AnyCodable>.self
                                )

                                if res.ok == true, let u = res.user {
                                    Session.save(id: u.id, role: u.role)
                                    onAuthed(u.role)
                                } else {
                                    info = res.error ?? "Kayıt başarısız"
                                }
                            } catch {
                                info = error.localizedDescription
                            }
                        }
                    }
                    .buttonStyle(.borderedProminent)
                }

                // 4) Durum
                Group {
                    if auth.loading { ProgressView() }
                    if !info.isEmpty { Text(info).foregroundColor(.red) }
                }
            }
            .padding()
        }
        .onAppear { loc.requestAndFetch() }
    }
}
