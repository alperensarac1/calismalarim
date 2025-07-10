<?php
header("Content-Type: application/json; charset=UTF-8");
require_once("mesajlasma_vt_baglanti.php");

$gonderen_id = isset($_POST["gonderen_id"]) ? intval($_POST["gonderen_id"]) : 0;
$alici_id    = isset($_POST["alici_id"]) ? intval($_POST["alici_id"]) : 0;
$mesaj_text  = isset($_POST["mesaj_text"]) ? trim($_POST["mesaj_text"]) : "";
$resim_var   = isset($_POST["resim_var"]) ? intval($_POST["resim_var"]) : 0;
$base64_img  = isset($_POST["base64_img"]) ? $_POST["base64_img"] : null;

if ($gonderen_id <= 0 || $alici_id <= 0) {
    echo json_encode(["success" => false, "error" => "Geçersiz gonderen_id veya alici_id"]);
    exit;
}

$resim_url = null;

// Resim varsa, base64'ü çöz ve dosya olarak kaydet
if ($resim_var === 1 && !empty($base64_img)) {
    $dosyaAdi = "mesaj_" . uniqid() . ".jpg";
    $hedefKlasor = "uploads/";
    if (!file_exists($hedefKlasor)) {
        mkdir($hedefKlasor, 0777, true);
    }

    $resimYolu = $hedefKlasor . $dosyaAdi;
    $base64_img = preg_replace('#^data:image/\w+;base64,#i', '', $base64_img); // başlığı temizle
    $decoded = base64_decode($base64_img);

    if ($decoded === false) {
        echo json_encode(["success" => false, "error" => "Base64 çözülemedi."]);
        exit;
    }

    if (file_put_contents($resimYolu, $decoded) === false) {
        echo json_encode(["success" => false, "error" => "Resim kaydedilemedi."]);
        exit;
    }

    $resim_url = "https://alperensaracdeneme.com/mesajlasma/" . $resimYolu;
;
}

// Veritabanına mesajı ekle
$sorgu = mysqli_prepare($baglan, "
    INSERT INTO mesajlasma_mesajlar (gonderen_id, alici_id, mesaj_text, resim_var, resim_url)
    VALUES (?, ?, ?, ?, ?)
");

mysqli_stmt_bind_param($sorgu, "iisis", $gonderen_id, $alici_id, $mesaj_text, $resim_var, $resim_url);

if (mysqli_stmt_execute($sorgu)) {
    echo json_encode([
        "success" => true,
        "message_id" => mysqli_insert_id($baglan),
        "resim_url" => $resim_url
    ]);
} else {
    echo json_encode(["success" => false, "error" => "Mesaj eklenemedi."]);
}

mysqli_close($baglan);
?>
