<?php
header("Content-Type: application/json; charset=UTF-8");

$response = [
    "success" => true,
    "message" => "Sunucu bağlantısı başarılı!",
    "timestamp" => date("Y-m-d H:i:s")
];

echo json_encode($response, JSON_UNESCAPED_UNICODE);
?>