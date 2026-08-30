<?php
require "../../db/db.php";

// Oturum kontrolü
if (session_status() == PHP_SESSION_NONE) {
    session_start();
}
if (!isset($_SESSION["id"])) {
    exit("Yetkisiz erişim");
}

$kullanici_id = (int)$_POST["kullanici_id"];
$isim = trim($_POST["isim"]);

if ($kullanici_id != $_SESSION["id"]) {
    exit("Yetkisiz erişim");
}

if (empty($isim) || $kullanici_id == 0) {
    exit("Eksik veri");
}

// Sıra numarası
$sql = "SELECT MAX(sira) FROM mekanlar WHERE kullanici_id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$kullanici_id]);
$son_sira = $sorgu->fetchColumn() ?: 0;
$son_sira++;

$sql = "INSERT INTO mekanlar (kullanici_id, isim, sira) VALUES (?, ?, ?)";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$kullanici_id, $isim, $son_sira]);

echo "OK";
?>