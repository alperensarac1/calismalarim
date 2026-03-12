import SwiftUI

struct PatientView: View {
    @StateObject private var loc = LocationVM()
    @StateObject private var vm = PatientVM()
    var onLogout: () -> Void = {}

    private var patientId: Int { Session.userId() }

    var body: some View {
        ScrollView {
            VStack(spacing: 12) {

                // 1) Header
                Group {
                    HStack {
                        Text("Hasta").font(.title2).bold()
                        Spacer()
                        Button("Çıkış") {
                            Session.clear()
                            onLogout()
                        }
                    }
                }

                // 2) Konum
                Group {
                    Text(loc.statusText).font(.footnote)
                    Button("Konumu Yenile") { loc.requestAndFetch() }
                        .buttonStyle(.bordered)

                    Divider()
                }

                // 3) Form
                Group {
                    TextField("Servis", text: $vm.servis).textFieldStyle(.roundedBorder)
                    TextField("Oda No", text: $vm.oda).textFieldStyle(.roundedBorder)

                    Button("Yardım İste") {
                        Task {
                            guard let lat = loc.lat, let lng = loc.lng else {
                                vm.statusText = "Konum alınmadan gönderilemez."
                                return
                            }
                            await vm.createHelp(patientId: patientId, lat: lat, lng: lng)
                            await vm.fetchActive(patientId: patientId)
                        }
                    }
                    .buttonStyle(.borderedProminent)

                    if vm.loading { ProgressView() }

                    Divider()
                }

                // 4) Durum
                Group {
                    if let a = vm.active {
                        let rem = a.remaining_seconds ?? 0

                        Text("Durum: \(a.status)" +
                             (a.status == "ACCEPTED" ? " (Kalan: \(TimeUtils.formatRemainingSeconds(rem)))" : ""))

                        HStack {
                            if a.status == "ACCEPTED" {
                                Button("Onayla") {
                                    Task {
                                        await vm.confirm(requestId: a.id, patientId: patientId)
                                        await vm.fetchActive(patientId: patientId)
                                    }
                                }
                                .buttonStyle(.borderedProminent)
                            }

                            if a.status == "OPEN" || a.status == "ACCEPTED" {
                                Button("İptal") {
                                    Task {
                                        await vm.cancel(requestId: a.id, patientId: patientId)
                                        await vm.fetchActive(patientId: patientId)
                                    }
                                }
                                .buttonStyle(.bordered)
                            }
                        }
                    } else {
                        Text("Durum: Aktif istek yok")
                    }

                    if !vm.statusText.isEmpty {
                        Text(vm.statusText)
                    }
                }
            }
            .padding()
        }
        .onAppear {
            loc.requestAndFetch()
            vm.startPolling(patientId: patientId)
        }
        .onDisappear {
            vm.stopPolling()
        }
    }
}
