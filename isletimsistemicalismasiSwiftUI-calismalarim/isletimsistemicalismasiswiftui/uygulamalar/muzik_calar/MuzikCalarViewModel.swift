//
//  MuzikCalarViewModel.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 23.02.2025.
//
import UIKit
import AVFAudio

class MuzikCalarViewModel: ObservableObject {
    @Published var muzikler = ["disfigure", "shine", "vidya"]
    @Published var suAnkiMuzik: String = ""
    
    private var muzikCalar: AVAudioPlayer?
    private var muzikIndex: Int = 0

    init() {
        suAnkiMuzik = muzikler[muzikIndex]
        AVAudioSession.sharedInstance().configurePlaybackMode()
    }

    func muzikSecVeOynat(muzikAdi: String) {
        if let index = muzikler.firstIndex(of: muzikAdi) {
            muzikIndex = index
            muzikOynat()
        }
    }
    
    func muzikOynat() {
        guard let dosyaURL = Bundle.main.url(forResource: muzikler[muzikIndex], withExtension: "mp3") else {
            print("❌ Dosya bulunamadı: \(muzikler[muzikIndex]).mp3")
            return
        }

        do {
            muzikCalar = try AVAudioPlayer(contentsOf: dosyaURL)
            muzikCalar?.prepareToPlay()
            muzikCalar?.play()
            suAnkiMuzik = muzikler[muzikIndex]
        } catch {
            print("⚠️ Müzik oynatılamadı: \(error.localizedDescription)")
        }
    }

    func oynat() {
        muzikCalar?.play()
    }
    
    func duraklat() {
        muzikCalar?.pause()
    }
    
    func durdur() {
        muzikCalar?.stop()
        muzikCalar?.currentTime = 0
    }
    
    func oncekiSarki() {
        if muzikIndex > 0 {
            muzikIndex -= 1
            muzikOynat()
        }
    }

    func sonrakiSarki() {
        if muzikIndex < muzikler.count - 1 {
            muzikIndex += 1
            muzikOynat()
        }
    }
}

