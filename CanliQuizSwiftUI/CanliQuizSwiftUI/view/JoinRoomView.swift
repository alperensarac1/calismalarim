import SwiftUI

struct JoinRoomView: View {

    /*
        ViewModel'i EnvironmentObject olarak alıyoruz.

        Çünkü LiveQuizSwiftUIApp.swift içinde:
        
        RootView()
            .environmentObject(viewModel)

        şeklinde vermiştik.
    */
    @EnvironmentObject var viewModel: QuizViewModel

    @State private var username: String = ""
    @State private var roomCode: String = ""

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {

                Text("Odaya Giriş Yap")
                    .font(.system(size: 28, weight: .bold))

                Text("Kullanıcı adını ve oda kodunu gir.")
                    .font(.system(size: 15))
                    .foregroundColor(.secondary)

                TextField("Kullanıcı adı", text: $username)
                    .textFieldStyle(.roundedBorder)
                    .textInputAutocapitalization(.never)

                TextField("Oda kodu", text: $roomCode)
                    .textFieldStyle(.roundedBorder)
                    .keyboardType(.numberPad)

                Button {
                    viewModel.joinRoom(
                        roomCode: roomCode,
                        username: username
                    )
                } label: {
                    Text("Odaya Katıl")
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                }
                .buttonStyle(.borderedProminent)

                Button {
                    viewModel.goHome()
                } label: {
                    Text("Geri dön")
                }

                Text(viewModel.statusText)
                    .font(.system(size: 15))
                    .foregroundColor(.secondary)
                    .padding(.top, 8)

                Spacer()
            }
            .padding(24)
        }
        .navigationTitle("Odaya Giriş")
        .background(Color(red: 248/255, green: 250/255, blue: 252/255))
    }
}
