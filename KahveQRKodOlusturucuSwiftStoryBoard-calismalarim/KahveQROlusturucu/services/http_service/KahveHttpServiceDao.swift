//
//  KahveHttpServiceDao.swift
//  KahveQROlusturucu
//
//  Created by Alperen Saraç on 30.03.2025.
//
import Foundation
import Alamofire

class KahveHTTPServisDao:Service {
    static let shared = KahveHTTPServisDao()
    private let baseURL = "https://alperensaracdeneme.com/kahveservis/"

    private init() {}

    func kullaniciEkle(kisiTel: String, completion: @escaping (CRUDCevap?) -> Void) {
        let url = "\(baseURL)kullanici_ekle.php"
        let parameters: [String: Any] = ["telefon_no": kisiTel]
        
        print(" Gönderilen Veriler: \(parameters) KahveHttpServiceDao")

        AF.request(url, method: .post, parameters: parameters, encoding: URLEncoding.httpBody)
            .validate()
            .responseString { response in
                switch response.result {
                case .success(let jsonString):
                    print(" API'den gelen ham yanıt: \(jsonString) KahveHttpServiceDao")

                    guard let data = jsonString.data(using: .utf8) else {
                        print("JSON verisine dönüştürülemedi! KahveHttpServiceDao")
                        completion(nil)
                        return
                    }

                    do {
                        let decodedResponse = try JSONDecoder().decode(CRUDCevap.self, from: data)
                        print(" Çözümlenmiş API Yanıtı: \(decodedResponse) KahveHttpServiceDao")
                        completion(decodedResponse)
                    } catch {
                        print(" JSON decode hatası: \(error.localizedDescription)")
                        print(" API'den gelen JSON geçersiz mi? İçerik: \(jsonString) " )
                        completion(nil)
                    }

                case .failure(let error):
                    print("API isteği başarısız: \(error.localizedDescription) KahveHttpServiceDao")
                    completion(nil)
                }
            }

    }

    func kahveWithKategoriId(kategori: UrunKategori, completion: @escaping (UrunCevap?) -> Void) {
        let url = "\(baseURL)kahve_with_kategori_id.php"
        let parameters: [String: Any] = ["id": kategori.kategoriKodu()]

        AF.request(url, method: .get, parameters: parameters, encoding: URLEncoding.default)
            .responseString { response in
                switch response.result {
                case .success(let jsonString):
                    print(" API'den gelen JSON (Kategori ID: \(kategori.kategoriKodu())): \(jsonString) KahveHttpServiceDao")
                    
                    guard let data = jsonString.data(using: .utf8) else {
                        print(" JSON string, Data'ya çevrilemedi! KahveHttpServiceDao")
                        completion(nil)
                        return
                    }
                    
                    let decoder = JSONDecoder()
                    do {
                        let decodedResponse = try decoder.decode(UrunCevap.self, from: data)
                        if decodedResponse.success == 1, !decodedResponse.kahveUrun.isEmpty {
                            completion(decodedResponse)
                        } else {
                            print("API'den geçerli veri alınamadı: Boş liste döndü.")
                            completion(nil)
                        }
                    } catch {
                        print(" JSON Decode Hatası: \(error.localizedDescription)")
                        completion(nil)
                    }
                    
                case .failure(let error):
                    print(" API Hatası: Kategori ID \(kategori.kategoriKodu()) için veri alınamadı. Hata: \(error.localizedDescription)")
                    completion(nil)
                }
            }
    }

    func tumKahveler(completion: @escaping (UrunCevap?) -> Void) {
        let url = "\(baseURL)tum_kahveler.php"

        AF.request(url, method: .get,encoding: URLEncoding.default)
            .validate()
            .responseString { response in
                switch response.result {
                case .success(let jsonString):
                    print(" API'den gelen JSON (: \(jsonString) KahveHttpServiceDao")
                    
                    guard let data = jsonString.data(using: .utf8) else {
                        print("JSON string, Data'ya çevrilemedi! KahveHttpServiceDao")
                        completion(nil)
                        return
                    }
                    
                    let decoder = JSONDecoder()
                    do {
                        let decodedResponse = try decoder.decode(UrunCevap.self, from: data)
                        if decodedResponse.success == 1, !decodedResponse.kahveUrun.isEmpty {
                            completion(decodedResponse)
                        } else {
                            print(" API'den geçerli veri alınamadı: Boş liste döndü. KahveHttpServiceDao")
                            completion(nil)
                        }
                    } catch {
                        print("JSON Decode Hatası: \(error.localizedDescription) KahveHttpServiceDao")
                        completion(nil)
                    }
                    
                case .failure(let error):
                    print("API Hatası: tum kahveler için veri alınamadı. Hata: \(error.localizedDescription)")
                    completion(nil)
                }
            }
    }

    
    func kodUret(dogrulamaKodu: String, kisiTel: String, completion: @escaping (KodUretCevap?) -> Void) {
        if kisiTel.isEmpty {
            print("❌ Telefon numarası eksik!")
            return
        }
        
        let url = "\(baseURL)kod_uret.php"
        let parameters: [String: Any] = [
            "dogrulama_kodu": dogrulamaKodu,
            "kullanici_tel": kisiTel
        ]
        
        print(" Gönderilen Veriler: \(parameters)")
        
        AF.request(url, method: .post, parameters: parameters, encoding: URLEncoding.httpBody)
            .responseString { response in
                switch response.result {
                case .success(let jsonString):
                    print(" API'den gelen JSON KAHVESERVISHTTPDAO KODURET: \(jsonString)")
                    let data = jsonString.data(using: .utf8)
                    if let jsonData = data {
                        let decoder = JSONDecoder()
                        let decodedResponse = try? decoder.decode(KodUretCevap.self, from: jsonData)
                        completion(decodedResponse)
                    } else {
                        print(" API'den gelen JSON KAHVESERVISHTTPDAO KODURET: hata")
                        completion(nil)
                    }
                case .failure:
                    print(" API'den gelen JSON KAHVESERVISHTTPDAO KODURET: hata")
                    completion(nil)
                }
            }
    }

}
/*
 Kullanım
 KahveHTTPServisDao.shared.kullaniciEkle(kisiTel: "05551234567") { response in
     print(response.message)
 }

 KahveHTTPServisDao.shared.tumKahveler { response in
     print(response.kahveUrun)
 }
 
 */
