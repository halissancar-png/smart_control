<?php
require "../../db/db.php";

if (!isset($_GET["kod"])) {
    exit("Eksik veri");
}

$kod = $_GET["kod"];

$sql = "SELECT komut FROM cihazlar WHERE cihaz_kodu = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$kod]);
$cihaz = $sorgu->fetch(PDO::FETCH_ASSOC);

if (!$cihaz) exit("HATA");
echo $cihaz["komut"] ?? "";
?>