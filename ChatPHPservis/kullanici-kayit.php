<?php
header("Content-Type: application/json; charset=UTF-8");
require_once("mesajlasma_vt_baglanti.php");

$ad     = isset($_POST["ad"])     ? trim($_POST["ad"])     : null;
$numara = isset($_POST["numara"]) ? trim($_POST["numara"]) : null;

if (!$ad || !$numara) {
    echo json_encode(["error" => "Ad ve numara zorunludur."]);
    exit;
}

/* Numara zaten kayıtlı mı? */
$sorgu = mysqli_prepare($baglan, "SELECT id FROM mesajlasma_kullanicilar WHERE numara = ?");
mysqli_stmt_bind_param($sorgu, "s", $numara);
mysqli_stmt_execute($sorgu);
mysqli_stmt_store_result($sorgu);

if (mysqli_stmt_num_rows($sorgu) > 0) {
    echo json_encode(["error" => "Bu numara zaten kayıtlı."]);
    exit;
}

/* Yeni kullanıcıyı ekle */
$ekle = mysqli_prepare(
    $baglan,
    "INSERT INTO mesajlasma_kullanicilar (ad, numara) VALUES (?, ?)"
);
mysqli_stmt_bind_param($ekle, "ss", $ad, $numara);

if (mysqli_stmt_execute($ekle)) {
    echo json_encode([
        "success" => true,
        "message" => "Kayıt başarılı",
        "id"      => mysqli_insert_id($baglan)
    ]);
} else {
    echo json_encode(["success" => false, "error" => "Kayıt başarısız"]);
}

mysqli_close($baglan);
?>
