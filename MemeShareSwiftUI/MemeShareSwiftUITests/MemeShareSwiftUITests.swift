//
//  MemeShareSwiftUITests.swift
//  MemeShareSwiftUITests
//
//  Created by Alperen Saraç on 2.09.2025.
//

import XCTest
@testable import MemeShareSwiftUI

final class MemeShareSwiftUITests: XCTestCase {

    // MARK: - API Testleri

    func testGetJoinedRooms_Decode() async throws {
        // 1) Fake JSON
        let json = """
        [
          {"room_id": 10, "room_code": "ABC123", "created_by": "2"},
          {"room_id": 11, "room_code": "XYZ789", "created_by": 2}
        ]
        """.data(using: .utf8)!

        // 2) Decode etmeyi dene
        let decoded = try JSONDecoder().decode([OdaModel].self, from: json)

        XCTAssertEqual(decoded.count, 2, "İki oda decode edilmeli")
        XCTAssertEqual(decoded[0].odaId, 10)
        XCTAssertEqual(decoded[1].roomCode, "XYZ789")
        XCTAssertEqual(decoded[0].createdBy, 2)
    }

    func testGetJoinedRooms_API() async throws {
        // ⚠️ Bu test gerçek sunucuya istek atar!
        // userId değerini, DB’de kesinlikle odası olan bir kullanıcı ile test edin.
        let userId = 2
        do {
            let rooms = try await APIService.shared.getJoinedRooms(userId: userId)
            print("Rooms:", rooms)
            XCTAssertNotNil(rooms, "Rooms boş dönmemeli")
        } catch {
            XCTFail("API çağrısı hata verdi: \(error.localizedDescription)")
        }
    }

    // MARK: - Performans Testi

    func testPerformanceExample() throws {
        self.measure {
            // Buraya performans ölçmek istediğin fonksiyonu koy
            let _ = (0..<1000).map { "Oda-\($0)" }
        }
    }
}
