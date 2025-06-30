<?php
include("kahve_vt_baglanti.php");

if (!isset($_POST['dogrulama_kodu']) || !isset($_POST['kullanici_tel'])) {
    echo json_encode(["success" => 0, "message" => "Gerekli parametreler eksik!"]);
    exit;
}

$dogrulamaKodu = trim($_POST['dogrulama_kodu']);
$tel = trim($_POST['kullanici_tel']);

if (empty($dogrulamaKodu) || empty($tel)) {
    echo json_encode(["success" => 0, "message" => "Boş değerler gönderilemez!"]);
    exit;
}

$sqlInsert = "INSERT INTO kahve_dogrulama (dogrulama_kodu, kullanici_tel) VALUES (?, ?)";
$statementInsert = mysqli_prepare($baglan, $sqlInsert);

if ($statementInsert) {
    mysqli_stmt_bind_param($statementInsert, "ss", $dogrulamaKodu, $tel);
    if (mysqli_stmt_execute($statementInsert)) {
        echo json_encode(["success" => 1, "message" => "Doğrulama kodu başarıyla eklendi."]);
    } else {
        echo json_encode(["success" => 0, "message" => "Veritabanına ekleme başarısız!"]);
    }
    mysqli_stmt_close($statementInsert);
} else {
    echo json_encode(["success" => 0, "message" => "Sorgu hazırlanırken hata oluştu."]);
}


$sqlDelete = "DELETE FROM kahve_dogrulama WHERE zaman_sutunu < NOW() - INTERVAL 90 SECOND";
mysqli_query($baglan, $sqlDelete);


mysqli_close($baglan);
?>
