<?php
include("kahve_vt_baglanti.php");

$response = array();
$tel = $_POST['telefon_no'];

$sorgula = "SELECT icilen_kahve, hediye_kahve FROM kahve_hediye_veritabani WHERE telefon_no = '$tel'";
$result = $baglan->query($sorgula);

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    $icilen_kahve = $row['icilen_kahve'];
    $hediye_kahve = $row['hediye_kahve'];

    $icilen_kahve++;

    if ($icilen_kahve >= 5) {
        $icilen_kahve = 0;
        $hediye_kahve++;
    }

    $guncelle = "UPDATE kahve_hediye_veritabani SET icilen_kahve = $icilen_kahve, hediye_kahve = $hediye_kahve WHERE telefon_no = '$tel'";
    if ($baglan->query($guncelle) === TRUE) {
        $response["success"] = 1;
        $response["icilen_kahve"] = $icilen_kahve;
        $response["hediye_kahve"] = $hediye_kahve;
        $response["message"] = "Kahve bilgisi güncellendi.";
    } else {
        $response["success"] = 0;
        $response["message"] = "Veritabanı güncelleme hatası: " . $baglan->error;
    }
} else {
    $response["success"] = 0;
    $response["message"] = "Kullanıcı bulunamadı.";
}

header('Content-Type: application/json');
echo json_encode($response);

?>
