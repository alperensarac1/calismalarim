<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);
?>

<?php
$serverName ="localhost";
$username = "u227672425_admin";
$sifre = "ugu5ag05sS.";
$dbName = "u227672425_db";


$baglan = mysqli_connect($serverName,$username,$sifre,$dbName);
mysqli_set_charset($baglan,"utf8");

?>
