<?php
header("Content-Type: application/json; charset=UTF-8");
require_once("mesajlasma_vt_baglanti.php");

$kullanici_id = isset($_GET["kullanici_id"]) ? intval($_GET["kullanici_id"]) : 0;

if ($kullanici_id <= 0) {
    echo json_encode(["success" => false, "error" => "Geçersiz kullanici_id"]);
    exit;
}

/*
 Hem gönderici hem alıcı olduğu durumlar:
 Her konuştuğu kişi için sadece EN SON mesajı döner.
*/
$sorgu = mysqli_prepare($baglan, "
    SELECT 
        k.id,
        k.ad,
        k.numara,
        m.mesaj_text,
        m.resim_var,
        m.tarih
    FROM mesajlasma_mesajlar m
    JOIN mesajlasma_kullanicilar k
        ON (CASE 
                WHEN m.gonderen_id = ? THEN m.alici_id = k.id
                WHEN m.alici_id = ? THEN m.gonderen_id = k.id
            END)
    WHERE m.gonderen_id = ? OR m.alici_id = ?
    AND m.tarih = (
        SELECT MAX(tarih)
        FROM mesajlasma_mesajlar
        WHERE 
            (gonderen_id = m.gonderen_id AND alici_id = m.alici_id)
            OR
            (gonderen_id = m.alici_id AND alici_id = m.gonderen_id)
    )
    GROUP BY k.id
    ORDER BY m.tarih DESC
");

mysqli_stmt_bind_param($sorgu, "iiii", $kullanici_id, $kullanici_id, $kullanici_id, $kullanici_id);
mysqli_stmt_execute($sorgu);
$sonuc = mysqli_stmt_get_result($sorgu);

$kisiler = [];
while ($row = mysqli_fetch_assoc($sonuc)) {
    $kisiler[] = [
        "id"         => $row["id"],
        "ad"         => $row["ad"],
        "numara"     => $row["numara"],
        "son_mesaj"  => $row["resim_var"] == 1 ? "[Resim]" : $row["mesaj_text"],
        "tarih"      => $row["tarih"]
    ];
}

echo json_encode(["success" => true, "kisiler" => $kisiler], JSON_UNESCAPED_UNICODE);
mysqli_close($baglan);
?>
