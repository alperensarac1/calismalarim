//
//  OptionRowView.swift
//  CanliQuizSwift
//
//  Created by Alperen Saraç on 23.05.2026.
//

import Foundation
import UIKit

final class OptionRowView: UIView {

    let radioButton = UIButton(type: .system)
    let textField = UITextField()
    let deleteButton = UIButton(type: .system)

    var onSelect: (() -> Void)?
    var onDelete: (() -> Void)?

    var optionText: String {
        textField.text ?? ""
    }

    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setupUI()
    }

    func configure(index: Int, optionText: String) {
        textField.placeholder = "Şık \(index + 1)"

        if textField.text?.isEmpty ?? true {
            textField.text = optionText
        }
    }

    func setSelected(_ selected: Bool) {
        radioButton.setTitle(selected ? "●" : "○", for: .normal)
        radioButton.tintColor = selected ? .systemPurple : .systemGray
    }

    func setDeleteEnabled(_ enabled: Bool) {
        deleteButton.isEnabled = enabled
        deleteButton.alpha = enabled ? 1.0 : 0.4
    }

    private func setupUI() {
        translatesAutoresizingMaskIntoConstraints = false

        radioButton.setTitle("○", for: .normal)
        radioButton.titleLabel?.font = UIFont.systemFont(ofSize: 24)
        radioButton.tintColor = .systemGray
        radioButton.addTarget(self, action: #selector(selectTapped), for: .touchUpInside)

        textField.borderStyle = .roundedRect
        textField.font = UIFont.systemFont(ofSize: 15)
        textField.autocapitalizationType = .none

        deleteButton.setTitle("Sil", for: .normal)
        deleteButton.backgroundColor = UIColor.systemGray5
        deleteButton.tintColor = UIColor.systemRed
        deleteButton.layer.cornerRadius = 8
        deleteButton.addTarget(self, action: #selector(deleteTapped), for: .touchUpInside)

        addSubview(radioButton)
        addSubview(textField)
        addSubview(deleteButton)

        radioButton.translatesAutoresizingMaskIntoConstraints = false
        textField.translatesAutoresizingMaskIntoConstraints = false
        deleteButton.translatesAutoresizingMaskIntoConstraints = false

        NSLayoutConstraint.activate([
            heightAnchor.constraint(equalToConstant: 54),

            radioButton.leadingAnchor.constraint(equalTo: leadingAnchor),
            radioButton.centerYAnchor.constraint(equalTo: centerYAnchor),
            radioButton.widthAnchor.constraint(equalToConstant: 42),
            radioButton.heightAnchor.constraint(equalToConstant: 42),

            textField.leadingAnchor.constraint(equalTo: radioButton.trailingAnchor, constant: 8),
            textField.centerYAnchor.constraint(equalTo: centerYAnchor),
            textField.heightAnchor.constraint(equalToConstant: 48),

            deleteButton.leadingAnchor.constraint(equalTo: textField.trailingAnchor, constant: 8),
            deleteButton.trailingAnchor.constraint(equalTo: trailingAnchor),
            deleteButton.centerYAnchor.constraint(equalTo: centerYAnchor),
            deleteButton.widthAnchor.constraint(equalToConstant: 70),
            deleteButton.heightAnchor.constraint(equalToConstant: 48)
        ])
    }

    @objc private func selectTapped() {
        onSelect?()
    }

    @objc private func deleteTapped() {
        onDelete?()
    }
}
