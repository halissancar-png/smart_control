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

$cihaz_id = (int)$_POST["id"];
$kanal = (int)$_POST["kanal"];
$deger = $_POST["deger"];

if ($cihaz_id == 0 || $kanal == 0) {
    exit("Eksik veri");
}

// Cihaz kontrol
$sql = "SELECT cihaz_kodu FROM cihazlar WHERE id = ? AND kullanici_id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$cihaz_id, $kullanici_id]);
$cihaz = $sorgu->fetch(PDO::FETCH_ASSOC);

if (!$cihaz) {
    exit("Cihaz bulunamadı");
}

// 0/1 -> ON/OFF
$komutDegeri = ($deger == 1) ? "ON" : "OFF";

// Mevcut komutları oku
$sql = "SELECT komut FROM cihazlar WHERE id = ? AND kullanici_id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$cihaz_id, $kullanici_id]);
$mevcut = $sorgu->fetchColumn();

$komutlar = [];
if ($mevcut) {
    $parcalar = explode("|", $mevcut);
    foreach ($parcalar as $p) {
        if (strpos($p, "=") !== false) {
            list($k, $v) = explode("=", $p);
            $komutlar[$k] = $v;
        }
    }
}
$komutlar[$kanal] = $komutDegeri;

$yeniKomut = implode("|", array_map(function($k, $v) {
    return $k . "=" . $v;
}, array_keys($komutlar), $komutlar));

// Komutu kaydet
$sql = "UPDATE cihazlar SET komut = ?, deger = ? WHERE id = ? AND kullanici_id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$yeniKomut, $yeniKomut, $cihaz_id, $kullanici_id]);

echo "OK";
?>