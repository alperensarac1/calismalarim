<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);
?>

<?php
$serverName ="localhost";
$username = "";
$sifre = "";
$dbName = "";


$baglan = mysqli_connect($serverName,$username,$sifre,$dbName);
mysqli_set_charset($baglan,"utf8");

?>
