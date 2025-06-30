//
//  GuvenlikSorusuSecVC.swift
//  isletimsistemlericalismasi
//
//  Created by Alperen Saraç on 5.01.2025.
//

import UIKit

class GuvenlikSorusuSecVC: UIViewController {

    @IBOutlet weak var btnEnSevilenRenkOnayla: UIButton!
    @IBOutlet weak var etEnSevilenRenk: UITextField!
    @IBOutlet weak var radioEnSevilenRenk: UIButton!
    @IBOutlet weak var btnEnSevilenHayvanOnayla: UIButton!
    @IBOutlet weak var etEnSevilenHayvan: UITextField!
    @IBOutlet weak var radioEnSevilenHayvan: UIButton!
    @IBOutlet weak var btnEnSevilenMuzikGrubuOnayla: UIButton!
    @IBOutlet weak var etEnSevilenMuzikGrubu: UITextField!
    @IBOutlet weak var radioEnSevilenMuzikGrubu: UIButton!
    
    var kullaniciBilgileri:KullaniciBilgileri?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        kullaniciBilgileri = KullaniciBilgileri()

        
        
        
    }
    
    @IBAction func radioEnSevilenRenkClicked(_ sender: Any) {
        radioGoster(tiklananRadio: radioEnSevilenRenk, tiklananEt: etEnSevilenRenk, tiklananBtn: btnEnSevilenRenkOnayla)
        radioGizle(digerRadio: radioEnSevilenHayvan, digerEt: etEnSevilenHayvan, digerBtn: btnEnSevilenHayvanOnayla)
        radioGizle(digerRadio: radioEnSevilenMuzikGrubu, digerEt: etEnSevilenMuzikGrubu, digerBtn: btnEnSevilenMuzikGrubuOnayla)
    }
    @IBAction func radioEnSevilenHayvanClicked(_ sender: Any) {
        radioGoster(tiklananRadio: radioEnSevilenHayvan, tiklananEt: etEnSevilenHayvan, tiklananBtn: btnEnSevilenHayvanOnayla)
        radioGizle(digerRadio: radioEnSevilenRenk, digerEt: etEnSevilenRenk, digerBtn: btnEnSevilenRenkOnayla)
        radioGizle(digerRadio: radioEnSevilenMuzikGrubu, digerEt: etEnSevilenMuzikGrubu, digerBtn: btnEnSevilenMuzikGrubuOnayla)
        
    }
    @IBAction func radioEnSevilenMuzikGrubuClicked(_ sender: Any) {
        radioGoster(tiklananRadio: radioEnSevilenMuzikGrubu, tiklananEt: etEnSevilenMuzikGrubu, tiklananBtn: btnEnSevilenMuzikGrubuOnayla)
        radioGizle(digerRadio: radioEnSevilenRenk, digerEt: etEnSevilenRenk, digerBtn: btnEnSevilenRenkOnayla)
        radioGizle(digerRadio: radioEnSevilenHayvan, digerEt: etEnSevilenHayvan, digerBtn: btnEnSevilenHayvanOnayla)
    }
    
    @IBAction func btnEnSevilenRenkClicked(_ sender: Any) {
        if !etEnSevilenRenk.text!.isEmpty{
            kullaniciBilgileri?.guvenlikSorusuCevapKaydet(guvenlikSorusuCevap: etEnSevilenRenk.text!)
            kullaniciBilgileri?.guvenlikSorusuKaydet(guvenlikSorusu: "En sevdiğiniz renk nedir?")
            girisSayfasinaGecisYap()
        }
       
    }
    
    @IBAction func btnEnMuzikGrubuClicked(_ sender: Any) {
        if !etEnSevilenMuzikGrubu.text!.isEmpty{
            kullaniciBilgileri?.guvenlikSorusuCevapKaydet(guvenlikSorusuCevap: etEnSevilenMuzikGrubu.text!)
            kullaniciBilgileri?.guvenlikSorusuKaydet(guvenlikSorusu: "En sevdiğiniz müzik grubu nedir?")
            girisSayfasinaGecisYap()
        }
        
    }
    
    @IBAction func btnEnSevilenHayvanClicked(_ sender: Any) {
        if !etEnSevilenHayvan.text!.isEmpty{
            kullaniciBilgileri?.guvenlikSorusuCevapKaydet(guvenlikSorusuCevap: etEnSevilenHayvan.text!)
            kullaniciBilgileri?.guvenlikSorusuKaydet(guvenlikSorusu: "En sevdiğiniz hayvan nedir?")
            girisSayfasinaGecisYap()
        }
    }
    
    
    func radioGoster(tiklananRadio:UIButton,tiklananEt:UITextField,tiklananBtn:UIButton){
        tiklananRadio.setImage(UIImage(systemName: "circle.inset.filled"), for: .normal)
        tiklananEt.isHidden = false
        tiklananBtn.isHidden = false
    }
    func radioGizle(digerRadio:UIButton,digerEt:UITextField,digerBtn:UIButton){
        digerRadio.setImage(UIImage(systemName: "circle"), for: .normal)
        digerEt.isHidden = true
        digerBtn.isHidden = true
    }
    func girisSayfasinaGecisYap(){
        performSegue(withIdentifier: "toGirisYap", sender: nil)
    }
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        self.performTransition(segue: segue, withIdentifier: "toGirisYap", viewControllerType: GirisEkraniVC.self)
    }
    
}
