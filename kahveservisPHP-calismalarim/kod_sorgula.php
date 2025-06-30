<?php
include("kahve_vt_baglanti.php"); 

$dogrulamaKodu = isset($_POST['dogrulama_kodu']) ? trim($_POST['dogrulama_kodu']) : '';

if (empty($dogrulamaKodu)) {
    echo json_encode(["success" => 0, "message" => "Doğrulama kodu gerekli."]);
    exit;
}

$sqlsorgu = "SELECT dogrulama_kodu, kullanici_tel FROM kahve_dogrulama WHERE dogrulama_kodu = ?";

$statement = mysqli_prepare($baglan, $sqlsorgu);

if ($statement) {
   
    mysqli_stmt_bind_param($statement, "s", $dogrulamaKodu);
    
    mysqli_stmt_execute($statement);
   
    $result = mysqli_stmt_get_result($statement);

    if ($row = mysqli_fetch_assoc($result)) {
        
        $kullaniciTel = $row['kullanici_tel'];

       
        $sqlsorgu2 = "SELECT icilen_kahve, hediye_kahve FROM kahve_hediye_veritabani WHERE telefon_no = ?";
        $statement2 = mysqli_prepare($baglan, $sqlsorgu2);

        if ($statement2) {
           
            mysqli_stmt_bind_param($statement2, "s", $kullaniciTel);
          
            mysqli_stmt_execute($statement2);
            
            $result2 = mysqli_stmt_get_result($statement2);

            if ($row2 = mysqli_fetch_assoc($result2)) {
                
                echo json_encode([
                    "success" => 1,
                    "message" => "Doğrulama kodu bulundu.",
                    "dogrulama_kodu" => $row['dogrulama_kodu'],
                    "kullanici_tel" => $row['kullanici_tel'],
                    "icilen_kahve" => $row2["icilen_kahve"], 
                    "hediye_kahve" => $row2["hediye_kahve"] 
                ]);
            } else {
                echo json_encode(["success" => 0, "message" => "Telefon numarasıyla ilgili kahve bilgileri bulunamadı."]);
            }

            
            mysqli_stmt_close($statement2);
        } else {
            echo json_encode(["success" => 0, "message" => "Telefon numarası sorgulama sırasında hata oluştu."]);
        }
    } else {
        echo json_encode(["success" => 0, "message" => "Doğrulama kodu bulunamadı."]);
    }

   
    mysqli_stmt_close($statement);
} else {
    echo json_encode(["success" => 0, "message" => "Sorgu hazırlanırken hata oluştu."]);
}

mysqli_close($baglan);
?>

