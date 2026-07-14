//
//  HomeViewController.swift
//  EBiletSwift
//
//  Created by Alperen Saraç on 25.06.2026.
//

import Foundation
import UIKit

/*
    HomeViewController

    XIB tabanlı ana ekran.

    Bu ekranda:
    - Kullanıcı karşılama yazısı
    - Biletlerim butonu
    - QR Kontrol butonu
    - Çıkış butonu
    - Şehir seçimi
    - İlçe seçimi
    - Etkinlikleri Listele butonu
    - UITableView ile etkinlik listesi

    Android tarafındaki RecyclerView karşılığı iOS'ta UITableView'dir.
*/
final class HomeViewController: UIViewController {

    // MARK: - IBOutlet

    @IBOutlet private weak var welcomeLabel: UILabel!
    @IBOutlet private weak var roleLabel: UILabel!

    @IBOutlet private weak var myTicketsButton: UIButton!
    @IBOutlet private weak var scannerButton: UIButton!
    @IBOutlet private weak var logoutButton: UIButton!

    @IBOutlet private weak var cityTextField: UITextField!
    @IBOutlet private weak var districtTextField: UITextField!
    @IBOutlet private weak var listEventsButton: UIButton!

    @IBOutlet private weak var statusLabel: UILabel!

    @IBOutlet private weak var tableView: UITableView!
    @IBOutlet private weak var activityIndicator: UIActivityIndicatorView!

    // MARK: - Data

    /*
        API'den gelen listeler.
    */
    private var cities: [City] = []
    private var districts: [District] = []
    private var events: [Event] = []

    /*
        Seçili şehir / ilçe.
    */
    private var selectedCity: City?
    private var selectedDistrict: District?

    /*
        TextField picker yapıları.
    */
    private let cityPicker = UIPickerView()
    private let districtPicker = UIPickerView()

    // MARK: - Lifecycle

    override func viewDidLoad() {
        super.viewDidLoad()

        setupUI()
        setupPickers()
        setupTableView()
        loadCities()
    }

    // MARK: - Setup

    private func setupUI() {
        view.backgroundColor = UIColor(red: 245/255, green: 246/255, blue: 250/255, alpha: 1)

        title = "Etkinlikler"

        welcomeLabel.text = "Hoş geldin, \(SessionManager.shared.fullName)"
        welcomeLabel.font = .boldSystemFont(ofSize: 22)
        welcomeLabel.textColor = UIColor(red: 15/255, green: 23/255, blue: 42/255, alpha: 1)

        let role = SessionManager.shared.role

        if role == "admin" {
            roleLabel.text = "Admin hesabı"
        } else if role == "staff" {
            roleLabel.text = "Görevli hesabı"
        } else {
            roleLabel.text = "Etkinlikleri keşfet"
        }

        roleLabel.font = .systemFont(ofSize: 14)
        roleLabel.textColor = UIColor(red: 100/255, green: 116/255, blue: 139/255, alpha: 1)

        setupSmallButton(myTicketsButton, title: "Biletlerim")
        setupSmallButton(scannerButton, title: "QR Kontrol")
        setupSmallButton(logoutButton, title: "Çıkış")

        /*
            QR Kontrol sadece staff/admin kullanıcıya görünsün.
        */
        scannerButton.isHidden = !SessionManager.shared.isStaffOrAdmin

        setupTextField(cityTextField, placeholder: "Şehir seçiniz")
        setupTextField(districtTextField, placeholder: "İlçe seçiniz")

        setupMainButton(listEventsButton, title: "Etkinlikleri Listele")

        statusLabel.text = "Şehirler yükleniyor..."
        statusLabel.font = .systemFont(ofSize: 14)
        statusLabel.textColor = UIColor(red: 100/255, green: 116/255, blue: 139/255, alpha: 1)
        statusLabel.numberOfLines = 0

        activityIndicator.hidesWhenStopped = true
        activityIndicator.stopAnimating()
    }

    private func setupSmallButton(_ button: UIButton, title: String) {
        button.setTitle(title, for: .normal)
        button.backgroundColor = UIColor(red: 37/255, green: 99/255, blue: 235/255, alpha: 1)
        button.setTitleColor(.white, for: .normal)
        button.titleLabel?.font = .boldSystemFont(ofSize: 14)
        button.layer.cornerRadius = 10
        button.layer.masksToBounds = true
    }

    private func setupMainButton(_ button: UIButton, title: String) {
        button.setTitle(title, for: .normal)
        button.backgroundColor = UIColor(red: 22/255, green: 163/255, blue: 74/255, alpha: 1)
        button.setTitleColor(.white, for: .normal)
        button.titleLabel?.font = .boldSystemFont(ofSize: 16)
        button.layer.cornerRadius = 12
        button.layer.masksToBounds = true
    }

    private func setupTextField(_ textField: UITextField, placeholder: String) {
        textField.placeholder = placeholder
        textField.backgroundColor = UIColor(red: 238/255, green: 242/255, blue: 255/255, alpha: 1)
        textField.layer.cornerRadius = 12
        textField.layer.masksToBounds = true

        textField.tintColor = .clear

        let paddingView = UIView(frame: CGRect(x: 0, y: 0, width: 14, height: 44))
        textField.leftView = paddingView
        textField.leftViewMode = .always
    }

    private func setupPickers() {
        cityPicker.delegate = self
        cityPicker.dataSource = self

        districtPicker.delegate = self
        districtPicker.dataSource = self

        cityTextField.inputView = cityPicker
        districtTextField.inputView = districtPicker

        cityTextField.inputAccessoryView = makeToolbar(
            doneSelector: #selector(cityPickerDoneTapped)
        )

        districtTextField.inputAccessoryView = makeToolbar(
            doneSelector: #selector(districtPickerDoneTapped)
        )
    }

    private func makeToolbar(doneSelector: Selector) -> UIToolbar {
        let toolbar = UIToolbar()
        toolbar.sizeToFit()

        let flexibleSpace = UIBarButtonItem(
            barButtonSystemItem: .flexibleSpace,
            target: nil,
            action: nil
        )

        let doneButton = UIBarButtonItem(
            title: "Seç",
            style: .done,
            target: self,
            action: doneSelector
        )

        toolbar.items = [flexibleSpace, doneButton]

        return toolbar
    }

    private func setupTableView() {
        tableView.backgroundColor = .clear
        tableView.separatorStyle = .none

        tableView.delegate = self
        tableView.dataSource = self

        /*
            XIB tabanlı cell register ediyoruz.
        */
        let nib = UINib(
            nibName: "EventTableViewCell",
            bundle: nil
        )

        tableView.register(
            nib,
            forCellReuseIdentifier: EventTableViewCell.identifier
        )
    }

    // MARK: - Picker Done Actions

    @objc private func cityPickerDoneTapped() {
        if !cities.isEmpty {
            let selectedRow = cityPicker.selectedRow(inComponent: 0)
            let city = cities[selectedRow]

            selectedCity = city
            cityTextField.text = city.name

            /*
                Şehir değişince:
                - ilçe sıfırlanır
                - etkinlikler temizlenir
                - yeni ilçeler yüklenir
            */
            selectedDistrict = nil
            districtTextField.text = ""
            districts.removeAll()
            events.removeAll()
            tableView.reloadData()

            loadDistricts(cityId: city.id)
        }

        cityTextField.resignFirstResponder()
    }

    @objc private func districtPickerDoneTapped() {
        if !districts.isEmpty {
            let selectedRow = districtPicker.selectedRow(inComponent: 0)
            let district = districts[selectedRow]

            selectedDistrict = district
            districtTextField.text = district.name
        }

        districtTextField.resignFirstResponder()
    }

    // MARK: - Actions

    @IBAction private func myTicketsButtonTapped(_ sender: UIButton) {
        let myTicketsVC = MyTicketsViewController()
           navigationController?.pushViewController(myTicketsVC, animated: true)
    }

    @IBAction private func scannerButtonTapped(_ sender: UIButton) {
        /*
            TicketScannerViewController sonraki adımda yapılacak.
        */
        showAlert(message: "QR Kontrol ekranı sonraki adımda eklenecek.")
    }

    @IBAction private func logoutButtonTapped(_ sender: UIButton) {
        SessionManager.shared.logout()

        let loginVC = LoginViewController()

        navigationController?.setViewControllers(
            [loginVC],
            animated: true
        )
    }

    @IBAction private func listEventsButtonTapped(_ sender: UIButton) {
        guard let city = selectedCity else {
            showAlert(message: "Lütfen şehir seçiniz")
            return
        }

        guard let district = selectedDistrict else {
            showAlert(message: "Lütfen ilçe seçiniz")
            return
        }

        loadEvents(cityId: city.id, districtId: district.id)
    }

    // MARK: - API Calls

    private func loadCities() {
        setLoading(true)
        statusLabel.text = "Şehirler yükleniyor..."

        APIService.shared.getCities(
            apiToken: SessionManager.shared.apiToken
        ) { [weak self] result in
            guard let self else { return }

            self.setLoading(false)

            switch result {
            case .success(let response):

                guard response.success else {
                    self.statusLabel.text = response.message
                    return
                }

                self.cities = response.data ?? []
                self.cityPicker.reloadAllComponents()

                if self.cities.isEmpty {
                    self.statusLabel.text = "Aktif şehir bulunamadı."
                } else {
                    self.statusLabel.text = "Şehir seçiniz."
                }

            case .failure(let error):
                self.statusLabel.text = error.localizedDescription
            }
        }
    }

    private func loadDistricts(cityId: Int) {
        setLoading(true)
        statusLabel.text = "İlçeler yükleniyor..."

        APIService.shared.getDistrictsByCity(
            apiToken: SessionManager.shared.apiToken,
            cityId: cityId
        ) { [weak self] result in
            guard let self else { return }

            self.setLoading(false)

            switch result {
            case .success(let response):

                guard response.success else {
                    self.statusLabel.text = response.message
                    return
                }

                self.districts = response.data ?? []
                self.districtPicker.reloadAllComponents()

                if self.districts.isEmpty {
                    self.statusLabel.text = "Bu şehir için aktif ilçe bulunamadı."
                } else {
                    self.statusLabel.text = "İlçe seçip etkinlikleri listeleyebilirsin."
                }

            case .failure(let error):
                self.statusLabel.text = error.localizedDescription
            }
        }
    }

    private func loadEvents(cityId: Int, districtId: Int) {
        setLoading(true)
        statusLabel.text = "Etkinlikler yükleniyor..."

        APIService.shared.getEventsByLocation(
            apiToken: SessionManager.shared.apiToken,
            cityId: cityId,
            districtId: districtId
        ) { [weak self] result in
            guard let self else { return }

            self.setLoading(false)

            switch result {
            case .success(let response):

                guard response.success else {
                    self.statusLabel.text = response.message
                    return
                }

                self.events = response.data ?? []
                self.tableView.reloadData()

                if self.events.isEmpty {
                    self.statusLabel.text = "Bu konum için etkinlik bulunamadı."
                } else {
                    self.statusLabel.text = "\(self.events.count) etkinlik listelendi."
                }

            case .failure(let error):
                self.statusLabel.text = error.localizedDescription
            }
        }
    }

    private func setLoading(_ isLoading: Bool) {
        listEventsButton.isEnabled = !isLoading
        myTicketsButton.isEnabled = !isLoading
        scannerButton.isEnabled = !isLoading
        logoutButton.isEnabled = !isLoading

        if isLoading {
            activityIndicator.startAnimating()
        } else {
            activityIndicator.stopAnimating()
        }
    }
}

// MARK: - UIPickerViewDelegate, UIPickerViewDataSource

extension HomeViewController: UIPickerViewDelegate, UIPickerViewDataSource {

    func numberOfComponents(
        in pickerView: UIPickerView
    ) -> Int {
        return 1
    }

    func pickerView(
        _ pickerView: UIPickerView,
        numberOfRowsInComponent component: Int
    ) -> Int {
        if pickerView == cityPicker {
            return cities.count
        }

        if pickerView == districtPicker {
            return districts.count
        }

        return 0
    }

    func pickerView(
        _ pickerView: UIPickerView,
        titleForRow row: Int,
        forComponent component: Int
    ) -> String? {
        if pickerView == cityPicker {
            return cities[row].name
        }

        if pickerView == districtPicker {
            return districts[row].name
        }

        return nil
    }

    func pickerView(
        _ pickerView: UIPickerView,
        didSelectRow row: Int,
        inComponent component: Int
    ) {
        /*
            Kullanıcı picker üzerinde gezdiğinde text güncellensin.
            Asıl seçim "Seç" butonunda yapılır.
        */
        if pickerView == cityPicker, !cities.isEmpty {
            cityTextField.text = cities[row].name
        }

        if pickerView == districtPicker, !districts.isEmpty {
            districtTextField.text = districts[row].name
        }
    }
}

// MARK: - UITableViewDataSource, UITableViewDelegate

extension HomeViewController: UITableViewDataSource, UITableViewDelegate {

    func tableView(
        _ tableView: UITableView,
        numberOfRowsInSection section: Int
    ) -> Int {
        return events.count
    }

    func tableView(
        _ tableView: UITableView,
        cellForRowAt indexPath: IndexPath
    ) -> UITableViewCell {
        guard let cell = tableView.dequeueReusableCell(
            withIdentifier: EventTableViewCell.identifier,
            for: indexPath
        ) as? EventTableViewCell else {
            return UITableViewCell()
        }

        let event = events[indexPath.row]

        cell.configure(with: event)

        return cell
    }

    func tableView(
        _ tableView: UITableView,
        didSelectRowAt indexPath: IndexPath
    ) {
        tableView.deselectRow(at: indexPath, animated: true)

        let event = events[indexPath.row]

        let detailVC = EventDetailViewController(eventId: event.id)
        navigationController?.pushViewController(detailVC, animated: true)
    }

    func tableView(
        _ tableView: UITableView,
        heightForRowAt indexPath: IndexPath
    ) -> CGFloat {
        return 330
    }
}
