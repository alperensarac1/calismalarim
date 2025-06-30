import UIKit

class SifreOlusturVC: UIViewController {

    @IBOutlet weak var imgSifreGosterTekrar: UIImageView!
    @IBOutlet weak var imgSifreGoster: UIImageView!
    @IBOutlet weak var etSifreTekrar: UITextField!
    @IBOutlet weak var etSifre: UITextField!
    
    var kullaniciBilgileri: KullaniciBilgileri!
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        self.performTransition(segue: segue, withIdentifier: "toGirisYap", viewControllerType: GirisEkraniVC.self)
        self.performTransition(segue: segue, withIdentifier: "toGuvenlikSorusuSec", viewControllerType: GuvenlikSorusuSecVC.self)
    }
    override func viewDidLoad() {
        super.viewDidLoad()
        kullaniciBilgileri = KullaniciBilgileri()
        DispatchQueue.main.async {
            if self.kullaniciBilgileri.sifreKaydiOlusturulmusMu() {
                print("Şifre kaydı var, Giriş Yap sayfasına geçiliyor.")
                self.performSegue(withIdentifier: "toGirisYap", sender: nil)
            } else {
                print("Şifre kaydı yok, mevcut sayfada kalınıyor.")
            }
        }
        setupPasswordToggle(for: etSifre, with: imgSifreGoster)
        setupPasswordToggle(for: etSifreTekrar, with: imgSifreGosterTekrar)
        
    }

    @IBAction func btnKaydet(_ sender: Any) {
        guard let sifre = etSifre.text, !sifre.isEmpty,
            let sifreTekrar = etSifreTekrar.text, !sifreTekrar.isEmpty else {
            // Uyarı göster
            let alert = UIAlertController(title: "Hata", message: "Şifre alanları boş bırakılamaz.", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "Tamam", style: .default))
            self.present(alert, animated: true)
            return
        }
        
        if sifre == sifreTekrar {
            kullaniciBilgileri?.sifreKaydi(sifre: sifre)
        
            performSegue(withIdentifier: "toGuvenlikSorusuSec", sender: nil)
            
           
        } else {
            // Şifreler eşleşmiyor
            let alert = UIAlertController(title: "Hata", message: "Şifreler eşleşmiyor.", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "Tamam", style: .default))
            self.present(alert, animated: true)
        }
    }
}
