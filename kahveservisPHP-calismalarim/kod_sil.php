<?php
include("kahve_vt_baglanti.php");

$dogrulamaKodu = isset($_POST['dogrulama_kodu']) ? trim($_POST['dogrulama_kodu']) : '';

if (empty($dogrulamaKodu)) {
    echo json_encode(["success" => 0, "message" => "Doğrulama kodu gerekli."]);
    exit;
}

$deleteQuery = "DELETE FROM kahve_dogrulama WHERE dogrulama_kodu = ?";
$deleteStmt = mysqli_prepare($baglan, $deleteQuery);

if ($deleteStmt) {
    mysqli_stmt_bind_param($deleteStmt, "s", $dogrulamaKodu);
    mysqli_stmt_execute($deleteStmt);
    
    if (mysqli_stmt_affected_rows($deleteStmt) > 0) {
        echo json_encode(["success" => 1, "message" => "Doğrulama kodu silindi."]);
    } else {
        echo json_encode(["success" => 0, "message" => "Doğrulama kodu bulunamadı veya zaten silinmiş."]);
    }

    mysqli_stmt_close($deleteStmt);
} else {
    echo json_encode(["success" => 0, "message" => "Sorgu hazırlanırken hata oluştu."]);
}

mysqli_close($baglan);
?>
