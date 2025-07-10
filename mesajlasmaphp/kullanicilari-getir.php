<?php
header("Content-Type: application/json; charset=UTF-8");
require_once("mesajlasma_vt_baglanti.php");

$sorgu = mysqli_query($baglan, "SELECT id, ad, numara FROM mesajlasma_kullanicilar ORDER BY ad ASC");

$kullanicilar = [];

while ($row = mysqli_fetch_assoc($sorgu)) {
    $kullanicilar[] = $row;
}

echo json_encode(["success" => true, "kullanicilar" => $kullanicilar], JSON_UNESCAPED_UNICODE);
mysqli_close($baglan);
?>
