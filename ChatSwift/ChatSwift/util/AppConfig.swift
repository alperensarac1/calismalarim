import Foundation

class AppConfig {
    // App başlarken otomatik olarak kaydedilmiş ID'yi alır
    static var kullaniciId: Int = PrefManager().getirKullaniciId()
}

class PrefManager {
    
    private let userDefaults = UserDefaults.standard
    private let keyKullaniciId = "kullanici_id"
    
    func kaydetKullaniciId(id: Int) {
        userDefaults.set(id, forKey: keyKullaniciId)
    }
    
    func getirKullaniciId() -> Int {
        return userDefaults.integer(forKey: keyKullaniciId)
    }
    
    func temizleKullanici() {
        userDefaults.removeObject(forKey: keyKullaniciId)
    }
    
    func kullaniciVarMi() -> Bool {
        return getirKullaniciId() != -1
    }
}
