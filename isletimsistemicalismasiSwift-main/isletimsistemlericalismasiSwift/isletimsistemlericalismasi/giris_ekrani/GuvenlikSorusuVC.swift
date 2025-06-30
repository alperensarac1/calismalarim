import UIKit

class GuvenlikSorusuVC: UIViewController {

    @IBOutlet weak var etGuvenlikSorusuCevap: UITextField!
    @IBOutlet weak var tvGuvenlikSorusu: UILabel!
    var kullaniciBilgileri:KullaniciBilgileri?
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        self.performTransition(segue: segue, withIdentifier: "toSifreOlustur", viewControllerType: SifreOlusturVC.self)
    }
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        kullaniciBilgileri = KullaniciBilgileri()
        
        tvGuvenlikSorusu.text = kullaniciBilgileri?.guvenlikSorusuGetir()
    
    }
   
    @IBAction func btnOnaylaClicked(_ sender: Any) {
        guard let guvenlikSorusuCevap = etGuvenlikSorusuCevap.text,!etGuvenlikSorusuCevap.text!.isEmpty else{
            let alert = UIAlertController(title: "Hata", message: "Güvenlik sorusunu cevaplamadınız.", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "Tamam", style: .default))
            self.present(alert, animated: true)
            return
        }
        if kullaniciBilgileri!.guvenlikSorusuDogrula(dogrulanacakCevap: guvenlikSorusuCevap){
            performSegue(withIdentifier: "toSifreOlustur", sender: nil)
        }
        else{
            let alert = UIAlertController(title: "Hata", message: "Cevabınız eşleşmiyor.", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "Tamam", style: .default))
            self.present(alert, animated: true)
            return
        }
    }
    
}
