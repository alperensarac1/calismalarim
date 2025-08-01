//
//  ApiService.swift
//  HaberUygulamaSwift
//
//  Created by Alperen Saraç on 19.07.2025.
//

import Foundation
import Alamofire

class ApiService {

    static let shared = ApiService()
    private init() {}

    func getHaberler(completion: @escaping (Result<[HaberModel], AFError>) -> Void) {
        let url = ApiConstants.baseURL + "haber_haberler-get.php"
        AF.request(url).responseDecodable(of: [HaberModel].self) { response in
            completion(response.result)
        }
    }

    func getYorumlar(haberId: String, completion: @escaping (Result<[YorumModel], AFError>) -> Void) {
        let url = ApiConstants.baseURL + "haber_yorumlar-get.php"
        let parameters: Parameters = ["haber_id": haberId]
        AF.request(url, parameters: parameters).responseDecodable(of: [YorumModel].self) { response in
            completion(response.result)
        }
    }

    func insertYorum(request: YorumInsertRequest, completion: @escaping (Result<ApiResponse, AFError>) -> Void) {
        let url = ApiConstants.baseURL + "haber_yorumlar-insert.php"
        AF.request(url, method: .post, parameters: request, encoder: JSONParameterEncoder.default)
            .responseDecodable(of: ApiResponse.self) { response in
                completion(response.result)
            }
    }

    func getKategoriler(completion: @escaping (Result<[HaberTuruModel], AFError>) -> Void) {
        let url = ApiConstants.baseURL + "haber_haberturleri-get.php"
        AF.request(url).responseDecodable(of: [HaberTuruModel].self) { response in
            completion(response.result)
        }
    }

    func getSon3Haber(completion: @escaping (Result<[HaberModel], AFError>) -> Void) {
        let url = ApiConstants.baseURL + "haber_haberler-son3-get.php"
        AF.request(url).responseDecodable(of: [HaberModel].self) { response in
            completion(response.result)
        }
    }

    func getSonDakikaHaberler(completion: @escaping (Result<[HaberModel], AFError>) -> Void) {
        let url = ApiConstants.baseURL + "haber_haberler-sondakika-get.php"
        AF.request(url).responseDecodable(of: [HaberModel].self) { response in
            completion(response.result)
        }
    }

    func getGundemHaberler(completion: @escaping (Result<[HaberModel], AFError>) -> Void) {
        let url = ApiConstants.baseURL + "haber_haberler-gundem-get.php"
        AF.request(url).responseDecodable(of: [HaberModel].self) { response in
            completion(response.result)
        }
    }
}
