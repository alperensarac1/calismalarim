<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);


$serverName = "";
$username = "";
$sifre = "";
$dbName = "";

$baglan = mysqli_connect($serverName, $username, $sifre, $dbName);


if (!$baglan) {
    die("Bağlantı başarısız: " . mysqli_connect_error());
}

mysqli_set_charset($baglan, "utf8");

$telefonNo = isset($_POST['telefon_no']) ? trim($_POST['telefon_no']) : '';

if (empty($telefonNo)) {
    echo json_encode(["success" => 0, "message" => "Telefon numarası gerekli."]);
    exit;
}

$sqlsorgu = "SELECT icilen_kahve, hediye_kahve FROM kahve_hediye_veritabani WHERE telefon_no = ?";
$statement = mysqli_prepare($baglan, $sqlsorgu);

if ($statement) {
    
    mysqli_stmt_bind_param($statement, "s", $telefonNo);

    mysqli_stmt_execute($statement);

    $result = mysqli_stmt_get_result($statement);

    if ($row = mysqli_fetch_assoc($result)) {
        
        echo json_encode([
            "success" => 1,
            "message" => "Kullanıcı bulundu.",
            "icilen_kahve" => $row['icilen_kahve'],
            "hediye_kahve" => $row['hediye_kahve']
        ]);
    } else {
        $sqlInsert = "INSERT INTO kahve_hediye_veritabani (telefon_no,icilen_kahve, hediye_kahve) VALUES (?, 0, 0)";
        $statementInsert = mysqli_prepare($baglan, $sqlInsert);

        if ($statementInsert) {
            
            mysqli_stmt_bind_param($statementInsert, "s", $telefonNo);

            
            if (mysqli_stmt_execute($statementInsert)) {
                echo json_encode([
                    "success" => 1,
                    "message" => "Yeni kullanıcı eklendi.",
                    "icilen_kahve" => 0,
                    "hediye_kahve" => 0
                ]);
            } else {
                echo json_encode(["success" => 0, "message" => "Kullanıcı eklenirken hata oluştu: " . mysqli_error($baglan)]);
            }
            
            mysqli_stmt_close($statementInsert);
        } else {
            echo json_encode(["success" => 0, "message" => "Kullanıcı eklenirken hata oluştu."]);
        }
    }
    mysqli_stmt_close($statement);
} else {
    echo json_encode(["success" => 0, "message" => "Telefon numarası sorgulama sırasında hata oluştu."]);
}

mysqli_close($baglan);
?>
