<?php
require "../../db/db.php";

if (!isset($_GET["kod"]) || !isset($_GET["kanal"]) || !isset($_GET["deger"])) {
    exit("Eksik veri");
}

$kod = $_GET["kod"];
$kanal = (int)$_GET["kanal"];
$deger = $_GET["deger"];

$sql = "SELECT id FROM cihazlar WHERE cihaz_kodu = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$kod]);
$cihaz = $sorgu->fetch(PDO::FETCH_ASSOC);

if (!$cihaz) exit("HATA");

$sql = "UPDATE cihaz_kanallari 
        SET deger = ? 
        WHERE cihaz_id = ? AND kanal_no = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$deger, $cihaz["id"], $kanal]);

// Cihaz online durumunu güncelle
$sql = "UPDATE cihazlar SET online = 1, son_gorulme = NOW() WHERE id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$cihaz["id"]]);

echo "OK";
?>