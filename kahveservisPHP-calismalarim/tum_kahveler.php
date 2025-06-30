<?php
include("kahve_vt_baglanti.php");

$response = array();


if (!$baglan) {
    die("Hatalı bağlantı : " . mysqli_connect_error());
}

$sqlsorgu = "SELECT * FROM kahve_urun";
$result = mysqli_query($baglan, $sqlsorgu);

if (mysqli_num_rows($result) > 0) {
    $response["kahve_urun"] = array();
    
    while ($row = mysqli_fetch_assoc($result)) {
        $kahveler = array();
        $kahveler["id"] = $row["id"];
        $kahveler["urun_ad"] = $row["urun_ad"];
        $kahveler["urun_resim"] = $row["urun_resim"];
        $kahveler["urun_aciklama"] = $row["urun_aciklama"];
        $kahveler["urun_kategori_id"] = $row["urun_kategori_id"];
        $kahveler["urun_indirim"] = $row["urun_indirim"];
        $kahveler["urun_fiyat"] = $row["urun_fiyat"];
         $kahveler["urun_indirimli_fiyat"] = $row["urun_indirimli_fiyat"];
        array_push($response["kahve_urun"], $kahveler);
    }

    $response["success"] = 1;
} else {
    $response["success"] = 0;
    $response["message"] = "No data found";
}

header('Content-Type: application/json');
echo json_encode($response);

mysqli_close($baglan);
?>
