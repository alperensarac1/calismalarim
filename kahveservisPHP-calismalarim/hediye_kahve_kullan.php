<?php
include("kahve_vt_baglanti.php");

$response = array();
$tel = mysqli_real_escape_string($baglan, $_POST['telefon_no']); 


$sorgula = "SELECT hediye_kahve FROM kahve_hediye_veritabani WHERE telefon_no = '$tel'";
$result = $baglan->query($sorgula);

if ($result->num_rows > 0) {

    $row = $result->fetch_assoc();
    $hediye_kahve = $row['hediye_kahve'];

    if ($hediye_kahve > 0) {
        $hediye_kahve--;

        $guncelle = "UPDATE kahve_hediye_veritabani SET hediye_kahve = $hediye_kahve WHERE dogrulama_kodu = '$tel'";
        if ($baglan->query($guncelle) === TRUE) {
            $response["success"] = 1;
            $response["hediye_kahve"] = $hediye_kahve;
            $response["message"] = "Hediye kahve alındı, güncellendi.";
        } else {
            $response["success"] = 0;
            $response["message"] = "Veritabanı güncelleme hatası: " . $baglan->error;
        }
    } else {
        $response["success"] = 0;
        $response["message"] = "Hediye kahveniz bulunmamaktadır.";
    }
} else {
    $response["success"] = 0;
    $response["message"] = "Kullanıcı bulunamadı.";
}

header('Content-Type: application/json');
echo json_encode($response);
?>
