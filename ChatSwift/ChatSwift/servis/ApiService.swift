import Foundation
import Alamofire

class ApiService {
    static let shared = ApiService()
    private let baseURL = "https://alperensaracdeneme.com/mesajlasma/"
    
    private init() {}

    // Kullanıcı Kayıt
    func kullaniciKayit(ad: String, numara: String, completion: @escaping (Result<SimpleResponse, AFError>) -> Void) {
        let parameters: Parameters = [
            "ad": ad,
            "numara": numara
        ]
        AF.request("\(baseURL)kullanici-kayit.php", method: .post, parameters: parameters)
            .responseDecodable(of: SimpleResponse.self) { response in
                completion(response.result)
            }
    }
    
    // Mesaj Gönder
    func mesajGonder(gonderenId: Int, aliciId: Int, mesajText: String, resimVar: Int, base64Img: String?, completion: @escaping (Result<SimpleResponse, AFError>) -> Void) {
        var parameters: Parameters = [
            "gonderen_id": gonderenId,
            "alici_id": aliciId,
            "mesaj_text": mesajText,
            "resim_var": resimVar
        ]
        if let img = base64Img {
            parameters["base64_img"] = img
        }
        AF.request("\(baseURL)mesaj-gonder.php", method: .post, parameters: parameters)
            .responseDecodable(of: SimpleResponse.self) { response in
                completion(response.result)
            }
    }

    // Mesajları Getir
    func mesajlariGetir(gonderenId: Int, aliciId: Int, completion: @escaping (Result<MesajListResponse, AFError>) -> Void) {
        let parameters: Parameters = [
            "gonderen_id": gonderenId,
            "alici_id": aliciId
        ]
        AF.request("\(baseURL)mesajlari-getir.php", parameters: parameters)
            .responseDecodable(of: MesajListResponse.self) { response in
                completion(response.result)
            }
    }

    // Konuşulan Kişileri Getir
    func konusulanKisiler(kullaniciId: Int, completion: @escaping (Result<KonusulanKisiListResponse, AFError>) -> Void) {
        let parameters: Parameters = [
            "kullanici_id": kullaniciId
        ]
        AF.request("\(baseURL)konusulan-kullanicilar.php", parameters: parameters)
            .responseDecodable(of: KonusulanKisiListResponse.self) { response in
                completion(response.result)
            }
    }

    // Kullanıcıları Getir
    func kullanicilariGetir(completion: @escaping (Result<KullaniciListResponse, AFError>) -> Void) {
        AF.request("\(baseURL)kullanicilari-getir.php")
            .responseDecodable(of: KullaniciListResponse.self) { response in
                completion(response.result)
            }
    }

    // Bağlantı Testi
    func testConnection(completion: @escaping (Result<SimpleResponse, AFError>) -> Void) {
        AF.request("\(baseURL)test-connection.php")
            .responseDecodable(of: SimpleResponse.self) { response in
                completion(response.result)
            }
    }
}
