//
//  MuzikCalar.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 23.02.2025.
//



import SwiftUI
import AVFoundation

struct MuzikCalar: View {
    @StateObject private var muzikCalarModel = MuzikCalarViewModel()

    var body: some View {
        VStack {
            Text("Şu An Çalan: \(muzikCalarModel.suAnkiMuzik)")
                .font(.title3)
                .padding()
            
            List(muzikCalarModel.muzikler, id: \.self) { muzik in
                HStack {
                    Text(muzik)
                    Spacer()
                    if muzik == muzikCalarModel.suAnkiMuzik {
                        Image(systemName: "music.note")
                            .foregroundColor(.blue)
                    }
                }
                .onTapGesture {
                    muzikCalarModel.muzikSecVeOynat(muzikAdi: muzik)
                }
            }
            
            HStack(spacing: 20) {
                Button(action: { muzikCalarModel.oncekiSarki() }) {
                    Image(systemName: "backward.fill")
                }
                Button(action: { muzikCalarModel.durdur() }) {
                    Image(systemName: "stop.fill")
                }
                Button(action: { muzikCalarModel.oynat() }) {
                    Image(systemName: "play.fill")
                }
                Button(action: { muzikCalarModel.duraklat() }) {
                    Image(systemName: "pause.fill")
                }
                Button(action: { muzikCalarModel.sonrakiSarki() }) {
                    Image(systemName: "forward.fill")
                }
            }
            .font(.largeTitle)
            .padding()
        }
    }
}


// AVAudioSession Uzantısı (Ses Çalma Modunu Ayarlar)
extension AVAudioSession {
    func configurePlaybackMode() {
        do {
            try self.setCategory(.playback, mode: .default, options: [])
            try self.setActive(true)
        } catch {
            print("⚠️ AVAudioSession Hatası: \(error.localizedDescription)")
        }
    }
}

struct MuzikCalar_Previews: PreviewProvider {
    static var previews: some View {
        MuzikCalar()
    }
}
