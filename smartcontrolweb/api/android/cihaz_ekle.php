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

if ($kullanici_id != $_SESSION["id"]) {
    exit("Yetkisiz erişim");
}

$cihaz_adi = trim($_POST["cihaz_adi"]);
$cihaz_kodu = trim($_POST["cihaz_kodu"]);
$cihaz_tipi = $_POST["cihaz_tipi"] ?? "O";
$kanal_sayisi = (int)($_POST["kanal_sayisi"] ?? 1);
$mekan_id = (int)($_POST["mekan_id"] ?? 0);

if (empty($cihaz_adi) || empty($cihaz_kodu) || $mekan_id == 0) {
    exit("Eksik veri");
}

// Mekan kontrol
$sql = "SELECT id FROM mekanlar WHERE id = ? AND kullanici_id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$mekan_id, $kullanici_id]);
if (!$sorgu->fetch()) {
    exit("Mekan bulunamadı");
}

// Aynı kod var mı?
$sql = "SELECT id FROM cihazlar WHERE cihaz_kodu = ? AND kullanici_id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$cihaz_kodu, $kullanici_id]);
if ($sorgu->fetch()) {
    exit("Bu cihaz kodu zaten kayıtlı");
}

// Sıra numarası
$sql = "SELECT MAX(sira) FROM cihazlar WHERE kullanici_id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$kullanici_id]);
$son_sira = $sorgu->fetchColumn() ?: 0;
$son_sira++;

$sql = "INSERT INTO cihazlar 
        (kullanici_id, mekan_id, cihaz_adi, cihaz_kodu, cihaz_tipi, kanal_sayisi, sira, aktif) 
        VALUES (?, ?, ?, ?, ?, ?, ?, 1)";

$sorgu = $pdo->prepare($sql);
$sorgu->execute([$kullanici_id, $mekan_id, $cihaz_adi, $cihaz_kodu, $cihaz_tipi, $kanal_sayisi, $son_sira]);

echo "OK";
?>