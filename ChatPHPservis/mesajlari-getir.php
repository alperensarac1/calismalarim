<?php
header("Content-Type: application/json; charset=UTF-8");
require_once("mesajlasma_vt_baglanti.php");

$gonderen_id = isset($_GET["gonderen_id"]) ? intval($_GET["gonderen_id"]) : 0;
$alici_id    = isset($_GET["alici_id"]) ? intval($_GET["alici_id"]) : 0;

if ($gonderen_id <= 0 || $alici_id <= 0) {
    echo json_encode(["success" => false, "error" => "Geçersiz gonderen_id veya alici_id"]);
    exit;
}

// İki kullanıcı arasındaki tüm mesajları al (giden ve gelen)
$sorgu = mysqli_prepare($baglan, "
    SELECT 
        id,
        gonderen_id,
        alici_id,
        mesaj_text,
        resim_var,
        resim_url,
        tarih
    FROM mesajlasma_mesajlar
    WHERE 
        (gonderen_id = ? AND alici_id = ?)
        OR
        (gonderen_id = ? AND alici_id = ?)
    ORDER BY tarih ASC
");

mysqli_stmt_bind_param($sorgu, "iiii", $gonderen_id, $alici_id, $alici_id, $gonderen_id);
mysqli_stmt_execute($sorgu);
$sonuc = mysqli_stmt_get_result($sorgu);

$mesajlar = [];
while ($row = mysqli_fetch_assoc($sonuc)) {
    $mesajlar[] = $row;
}

echo json_encode(["success" => true, "mesajlar" => $mesajlar], JSON_UNESCAPED_UNICODE);
mysqli_close($baglan);
?>
