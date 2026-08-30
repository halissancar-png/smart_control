<?php
session_start();
require "../db/db.php";

$isim = trim($_POST["isim"]);
$sifre = trim($_POST["sifre"]);

$sql = "SELECT * FROM kullanicilar WHERE isim = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$isim]);
$kullanici = $sorgu->fetch(PDO::FETCH_ASSOC);

if ($kullanici && sifreDogrula($sifre, $kullanici["sifre"])) {
    $_SESSION["id"] = $kullanici["id"];
    $_SESSION["isim"] = $kullanici["isim"];
    $_SESSION["son_aktivite"] = time();
    header("Location: panel.php");
    exit;
} else {
    header("Location: index.php?hata=1");
    exit;
}
?>