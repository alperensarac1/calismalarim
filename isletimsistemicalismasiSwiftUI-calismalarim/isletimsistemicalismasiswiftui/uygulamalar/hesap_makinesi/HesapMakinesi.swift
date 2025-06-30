//
//  KesapMakinesi.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 23.02.2025.
//

import SwiftUI

struct HesapMakinesi: View {
    @State private var numberOnScreen: Double = 0
    @State private var previousNumber: Double = 0
    @State private var operation: String = ""
    @State private var display: String = "0"
    @State private var performingMath: Bool = false
    
    let buttons: [[String]] = [
        ["7", "8", "9", "/"],
        ["4", "5", "6", "x"],
        ["1", "2", "3", "-"],
        ["C", "0", "=", "+"]
    ]
    //MARK: BODY
    var body: some View {
        VStack {
            Spacer()
            
            Text(display)
                .font(.largeTitle)
                .frame(maxWidth: .infinity, alignment: .trailing)
                .padding()
                .background(Color.gray.opacity(0.2))
                .cornerRadius(10)
            
            Spacer()
            
            ForEach(buttons, id: \..self) { row in
                HStack {
                    ForEach(row, id: \..self) { button in
                        Button(action: {
                            self.buttonTapped(button)
                        }) {
                            Text(button)
                                .font(.largeTitle)
                                .frame(width: 80, height: 80)
                                .background(Color.blue.opacity(0.7))
                                .foregroundColor(.white)
                                .clipShape(Circle())
                        }
                    }//:ForEach
                }//:HStack
            }//:ForEach
            Spacer()
        }//:VStack
        .padding()
    }//:Body End
    
    func buttonTapped(_ button: String) {
        if let _ = Double(button) {
            if performingMath {
                display = button
                numberOnScreen = Double(display) ?? 0
                performingMath = false
            } else {
                display = (display == "0") ? button : display + button
                numberOnScreen = Double(display) ?? 0
            }
        } else if button == "C" {
            display = "0"
            previousNumber = 0
            numberOnScreen = 0
            operation = ""
        } else if button == "=" {
            calculateResult()
        } else {
            previousNumber = numberOnScreen
            operation = button
            performingMath = true
        }//:Function End
        func calculateResult() {
            switch operation {
            case "/": display = String(previousNumber / numberOnScreen)
            case "x": display = String(previousNumber * numberOnScreen)
            case "-": display = String(previousNumber - numberOnScreen)
            case "+": display = String(previousNumber + numberOnScreen)
            default: break
            }
        }
    }//:Function End
}
//MARK: PREVIEW
struct HesapMakinesi_Previews: PreviewProvider {
    static var previews: some View {
        HesapMakinesi()
    }
}
