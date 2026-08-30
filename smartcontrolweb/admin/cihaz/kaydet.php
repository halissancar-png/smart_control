<?php
session_start();
if (!isset($_SESSION["id"])) exit;

require "../../db/db.php";

$mekan_id = (int)$_POST["mekan_id"];
$cihaz_adi = trim($_POST["cihaz_adi"]);
$cihaz_kodu = trim($_POST["cihaz_kodu"]);

$kullanici_id = $_SESSION["id"];

// Mekan kontrol
$sql = "SELECT id FROM mekanlar WHERE id = ? AND kullanici_id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$mekan_id, $kullanici_id]);
if (!$sorgu->fetch()) {
    die("Mekan bulunamadı");
}

// Cihaz kodundan tip ve kanal sayısını çıkar
$cihaz_tipi = substr($cihaz_kodu, 5, 1);        // SNCR_O8_6E79BA → O
$kanal_sayisi = (int)substr($cihaz_kodu, 6, 1); // SNCR_O8_6E79BA → 8

// Sıra numarası
$sql = "SELECT MAX(sira) FROM cihazlar WHERE kullanici_id = ? AND mekan_id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$kullanici_id, $mekan_id]);
$son_sira = $sorgu->fetchColumn() ?: 0;
$son_sira++;

$sql = "INSERT INTO cihazlar 
        (kullanici_id, mekan_id, cihaz_adi, cihaz_kodu, cihaz_tipi, kanal_sayisi, sira, aktif) 
        VALUES (?, ?, ?, ?, ?, ?, ?, 1)";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$kullanici_id, $mekan_id, $cihaz_adi, $cihaz_kodu, $cihaz_tipi, $kanal_sayisi, $son_sira]);

header("Location: liste.php?mekan_id=" . $mekan_id);
exit;
?>