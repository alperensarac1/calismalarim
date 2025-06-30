<?php

include("kahve_vt_baglanti.php");


$response = array();


$baglanti = mysqli_connect(DB_SERVER, DB_USER, DB_PASSWORD, DB_DATABASE);


if (!$baglanti) {
    die(json_encode(["success" => 0, "message" => "Hatalı bağlantı: " . mysqli_connect_error()]));
}

$sqlsorgu = "SELECT * FROM kahve_urun WHERE urun_indirim = 1";
$result = mysqli_query($baglanti, $sqlsorgu);

if ($result && mysqli_num_rows($result) > 0) {
    $response["kahve_urun"] = array();
    
    while ($row = mysqli_fetch_assoc($result)) {
        $kahveler = array();
        $kahveler["id"] = $row["id"];
        $kahveler["urun_resim"] = $row["urun_resim"];
        $kahveler["urun_aciklama"] = $row["urun_aciklama"];
        $kahveler["urun_kategori_id"] = $row["urun_kategori_id"];
        $kahveler["urun_indirim"] = $row["urun_indirim"];
        $kahveler["urun_fiyat"] = $row["urun_fiyat"];
        
      
        if (isset($row["urun_indirimli_fiyat"])) {
            $kahveler["urun_indirimli_fiyat"] = $row["urun_indirimli_fiyat"];
        }

        array_push($response["kahve_urun"], $kahveler);
    }

    $response["success"] = 1;
} else {
    $response["success"] = 0;
    $response["message"] = "Veri bulunamadı";
}

header('Content-Type: application/json');
echo json_encode($response);

mysqli_close($baglanti);
?>
