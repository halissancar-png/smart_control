<?php
session_start();
require $_SERVER["DOCUMENT_ROOT"] . "/smartcontrolweb/db/db.php";

$isim = trim($_POST["isim"]);
$sifre = trim($_POST["sifre"]);

// Kullanıcı adı kontrolü
$sql = "SELECT id FROM kullanicilar WHERE isim = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$isim]);
if ($sorgu->fetch()) {
    die("Bu kullanıcı adı zaten kullanılıyor.");
}

// Şifreyi hash'le
$sifre_hash = sifrele($sifre);

$sql = "INSERT INTO kullanicilar (isim, sifre) VALUES (?, ?)";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$isim, $sifre_hash]);

header("Location: /smartcontrolweb/admin/index.php");
exit;
?>