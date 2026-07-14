//
//  TicketScannerView.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import Foundation
import SwiftUI
import AVFoundation

/*
    TicketScannerView

    Staff/admin için QR bilet kontrol ekranıdır.

    Bu ekranda:
    - Kamera ile QR okutulur.
    - Manuel bilet kodu girilir.
    - check/ticket_check.php çağrılır.
    - Sonuç approved ise giriş onaylanır.
    - Hatalı/tekrar kullanılan biletlerde giriş reddedilir.

    Backend:
    check/ticket_check.php

    POST:
    api_token
    ticket_code
*/
struct TicketScannerView: View {

    // MARK: - UI State

    @State private var isScannerVisible: Bool = false
    @State private var isChecking: Bool = false

    @State private var manualCode: String = ""

    @State private var statusMessage: String = "QR kod okutabilir veya manuel bilet kodu girebilirsin."

    @State private var resultTitle: String = "Henüz kontrol yapılmadı"
    @State private var resultMessage: String = "QR okutulduğunda veya manuel kod girildiğinde sonuç burada görünecek."

    @State private var resultType: ScannerResultType = .neutral

    @State private var checkedTicket: Ticket?

    @State private var showAlert: Bool = false
    @State private var alertMessage: String = ""

    // MARK: - Permission

    @State private var cameraPermissionStatus: AVAuthorizationStatus = AVCaptureDevice.authorizationStatus(for: .video)

    var body: some View {
        ZStack {
            Color(red: 245 / 255, green: 246 / 255, blue: 250 / 255)
                .ignoresSafeArea()

            ScrollView {
                VStack(spacing: 14) {
                    headerCard

                    if !SessionManager.shared.isStaffOrAdmin {
                        unauthorizedCard
                    } else {
                        scannerCard
                        manualCheckCard
                        resultCard
                    }
                }
                .padding(14)
            }
        }
        .navigationTitle("QR Kontrol")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            refreshPermissionStatus()
        }
        .alert("Uyarı", isPresented: $showAlert) {
            Button("Tamam", role: .cancel) {}
        } message: {
            Text(alertMessage)
        }
    }

    // MARK: - Header

    private var headerCard: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("QR Bilet Kontrol")
                .font(.title)
                .bold()
                .foregroundStyle(Color(red: 15 / 255, green: 23 / 255, blue: 42 / 255))

            Text("Görevli: \(SessionManager.shared.fullName)")
                .font(.subheadline)
                .foregroundStyle(.secondary)

            Text("Rol: \(SessionManager.shared.role)")
                .font(.subheadline)
                .foregroundStyle(.secondary)

            HStack(spacing: 8) {
                if isChecking {
                    ProgressView()
                }

                Text(statusMessage)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)

                Spacer()
            }
            .padding(.top, 4)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .shadow(color: .black.opacity(0.08), radius: 8, x: 0, y: 4)
    }

    // MARK: - Unauthorized

    private var unauthorizedCard: some View {
        VStack(spacing: 12) {
            Image(systemName: "lock.fill")
                .font(.largeTitle)
                .foregroundStyle(.red)

            Text("Yetkisiz Erişim")
                .font(.title2)
                .bold()
                .foregroundStyle(.red)

            Text("Bu ekran sadece staff veya admin hesabıyla kullanılabilir.")
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(22)
        .background(Color.red.opacity(0.10))
        .clipShape(RoundedRectangle(cornerRadius: 18))
    }

    // MARK: - Scanner

    private var scannerCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Kamera ile QR Okut")
                .font(.title3)
                .bold()

            Text(scannerPermissionText)
                .font(.subheadline)
                .foregroundStyle(.secondary)

            if cameraPermissionStatus == .authorized {
                scannerArea

                AppButton(
                    title: isScannerVisible ? "Kamerayı Kapat" : "Kamerayı Başlat",
                    backgroundColor: isScannerVisible ? .red : .green,
                    isLoading: false
                ) {
                    isScannerVisible.toggle()
                }
            } else {
                AppButton(
                    title: "Kamera İzni Ver",
                    backgroundColor: .blue
                ) {
                    requestCameraPermission()
                }
            }
        }
        .padding(16)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .shadow(color: .black.opacity(0.08), radius: 8, x: 0, y: 4)
    }

    private var scannerArea: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 18)
                .fill(Color.black)

            if isScannerVisible {
                QRScannerRepresentable { code in
                    handleScannedCode(code)
                }
                .clipShape(RoundedRectangle(cornerRadius: 18))

                scannerOverlay
            } else {
                VStack(spacing: 10) {
                    Image(systemName: "qrcode.viewfinder")
                        .font(.largeTitle)
                        .foregroundStyle(.white)

                    Text("Kamerayı başlatmak için butona bas")
                        .font(.subheadline)
                        .foregroundStyle(.white.opacity(0.8))
                }
            }
        }
        .frame(height: 280)
    }

    private var scannerOverlay: some View {
        VStack {
            Spacer()

            RoundedRectangle(cornerRadius: 18)
                .stroke(Color.white, lineWidth: 3)
                .frame(width: 210, height: 210)

            Spacer()

            Text("QR kodu çerçevenin içine getir")
                .font(.subheadline)
                .foregroundStyle(.white)
                .padding(.bottom, 14)
        }
    }

    private var scannerPermissionText: String {
        switch cameraPermissionStatus {
        case .authorized:
            return "Kamera hazır. QR kodu okutabilirsin."
        case .notDetermined:
            return "QR okutmak için kamera izni gerekiyor."
        case .denied, .restricted:
            return "Kamera izni kapalı. iPhone Ayarlar uygulamasından izin vermen gerekir."
        @unknown default:
            return "Kamera izni durumu bilinmiyor."
        }
    }

    // MARK: - Manual

    private var manualCheckCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Manuel Kod Kontrolü")
                .font(.title3)
                .bold()

            AppTextField(
                title: "Bilet kodu",
                text: $manualCode
            )

            AppButton(
                title: "Kodu Kontrol Et",
                backgroundColor: .blue,
                isLoading: isChecking
            ) {
                let code = manualCode.trimmingCharacters(in: .whitespacesAndNewlines)

                guard !code.isEmpty else {
                    showError("Bilet kodu zorunludur.")
                    return
                }

                Task {
                    await checkTicket(code: code)
                }
            }
        }
        .padding(16)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .shadow(color: .black.opacity(0.08), radius: 8, x: 0, y: 4)
    }

    // MARK: - Result

    private var resultCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 10) {
                Image(systemName: resultIcon)
                    .font(.title2)
                    .foregroundStyle(resultColor)

                Text(resultTitle)
                    .font(.title3)
                    .bold()
                    .foregroundStyle(resultColor)

                Spacer()
            }

            Text(resultMessage)
                .font(.subheadline)
                .foregroundStyle(Color(red: 51 / 255, green: 65 / 255, blue: 85 / 255))

            if let checkedTicket {
                Divider()

                resultInfoBlock(
                    title: "Bilet Bilgisi",
                    text: buildTicketInfo(checkedTicket)
                )

                resultInfoBlock(
                    title: "Kullanıcı Bilgisi",
                    text: buildUserInfo(checkedTicket)
                )

                resultInfoBlock(
                    title: "Etkinlik Bilgisi",
                    text: buildEventInfo(checkedTicket)
                )

                resultInfoBlock(
                    title: "Konum Bilgisi",
                    text: buildLocationInfo(checkedTicket)
                )
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(resultColor.opacity(resultType == .neutral ? 0.04 : 0.12))
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .shadow(color: .black.opacity(0.06), radius: 7, x: 0, y: 3)
    }

    private func resultInfoBlock(
        title: String,
        text: String
    ) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(.footnote)
                .bold()
                .foregroundStyle(.secondary)

            Text(text)
                .font(.subheadline)
                .foregroundStyle(Color(red: 15 / 255, green: 23 / 255, blue: 42 / 255))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private var resultColor: Color {
        switch resultType {
        case .neutral:
            return .blue
        case .success:
            return .green
        case .failed:
            return .red
        }
    }

    private var resultIcon: String {
        switch resultType {
        case .neutral:
            return "info.circle.fill"
        case .success:
            return "checkmark.circle.fill"
        case .failed:
            return "xmark.circle.fill"
        }
    }

    // MARK: - Permission Actions

    private func refreshPermissionStatus() {
        cameraPermissionStatus = AVCaptureDevice.authorizationStatus(for: .video)
    }

    private func requestCameraPermission() {
        AVCaptureDevice.requestAccess(for: .video) { granted in
            DispatchQueue.main.async {
                refreshPermissionStatus()

                if granted {
                    statusMessage = "Kamera izni verildi. Kamerayı başlatabilirsin."
                } else {
                    statusMessage = "Kamera izni verilmedi."
                }
            }
        }
    }

    // MARK: - QR Actions

    private func handleScannedCode(_ code: String) {
        let cleanCode = code.trimmingCharacters(in: .whitespacesAndNewlines)

        guard !cleanCode.isEmpty else {
            return
        }

        /*
            Kamera görünümünü kapatıyoruz.
            Böylece aynı QR sürekli tekrar okunmaz.
        */
        isScannerVisible = false
        manualCode = cleanCode

        Task {
            await checkTicket(code: cleanCode)
        }
    }

    // MARK: - API

    private func checkTicket(code: String) async {
        guard !isChecking else {
            return
        }

        isChecking = true
        statusMessage = "Bilet kontrol ediliyor..."

        resultType = .neutral
        resultTitle = "Kontrol ediliyor..."
        resultMessage = "Bilet bilgisi backend üzerinden doğrulanıyor."
        checkedTicket = nil

        do {
            let response = try await APIService.shared.checkTicket(
                apiToken: SessionManager.shared.apiToken,
                ticketCode: code
            )

            isChecking = false
            statusMessage = "Kontrol tamamlandı."

            guard response.success else {
                showFailedResult(
                    title: "Giriş Reddedildi",
                    message: response.message
                )
                return
            }

            guard let ticket = response.data else {
                /*
                    Bazı backend cevaplarında success true olup data boş olabilir.
                    Yine de response.message gösteriyoruz.
                */
                showFailedResult(
                    title: "Bilet Kontrol Edildi",
                    message: response.message
                )
                return
            }

            let resultValue = ticket.result ?? "approved"

            if resultValue == "approved" {
                showSuccessResult(
                    message: response.message,
                    ticket: ticket
                )
            } else {
                showFailedResult(
                    title: "Giriş Reddedildi",
                    message: response.message
                )
            }

        } catch {
            isChecking = false
            statusMessage = error.localizedDescription

            showFailedResult(
                title: "Bağlantı Hatası",
                message: error.localizedDescription
            )
        }
    }

    private func showSuccessResult(
        message: String,
        ticket: Ticket
    ) {
        resultType = .success
        resultTitle = "Giriş Onaylandı"
        resultMessage = message
        checkedTicket = ticket
    }

    private func showFailedResult(
        title: String,
        message: String
    ) {
        resultType = .failed
        resultTitle = title
        resultMessage = message
        checkedTicket = nil
    }

    private func showError(_ message: String) {
        alertMessage = message
        showAlert = true
    }

    // MARK: - Text Builders

    private func buildTicketInfo(_ ticket: Ticket) -> String {
        let ticketId = String(ticket.resolvedTicketId)
        let ticketCode = ticket.ticketCode ?? "-"
        let status = ticket.ticketStatus ?? ticket.status ?? "-"

        return """
        Bilet ID: \(ticketId)
        Bilet Kodu: \(ticketCode)
        Durum: \(status)
        """
    }

    private func buildUserInfo(_ ticket: Ticket) -> String {
        let fullName = ticket.user?.fullName ?? "-"
        let email = ticket.user?.email ?? "-"
        let phone = ticket.user?.phone ?? "-"

        return """
        Kullanıcı: \(fullName)
        E-posta: \(email)
        Telefon: \(phone)
        """
    }

    private func buildEventInfo(_ ticket: Ticket) -> String {
        let eventTitle = ticket.event?.title ?? ticket.eventTitle ?? "-"
        let eventDate = ticket.event?.eventDate ?? "-"

        return """
        Etkinlik: \(eventTitle)
        Tarih: \(eventDate)
        """
    }

    private func buildLocationInfo(_ ticket: Ticket) -> String {
        let cityName = ticket.location?.cityName ?? ticket.city?.name ?? "-"
        let districtName = ticket.location?.districtName ?? ticket.district?.name ?? "-"
        let venueName = ticket.location?.venueName ?? ticket.venue?.name ?? "-"
        let address = ticket.location?.venueAddress ?? ticket.venue?.address ?? "-"

        return """
        Konum: \(cityName) / \(districtName)
        Sahne: \(venueName)
        Adres: \(address)
        """
    }
}

/*
    ScannerResultType

    Sonuç kartının rengini ve ikonunu belirlemek için kullanılır.
*/
enum ScannerResultType {
    case neutral
    case success
    case failed
}
