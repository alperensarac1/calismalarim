//
//  ServiceImpl.swift
//  KahveQROlusturucu
//
//  Created by Alperen Saraç on 2.04.2025.
//

import Foundation

class ServiceImpl{
    
    
    static func getInstance()->Service{
        return KahveHTTPServisDao.shared
    }
}
