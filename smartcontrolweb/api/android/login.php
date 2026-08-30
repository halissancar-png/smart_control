<?php
require "../../db/db.php";

$isim = trim($_POST["isim"] ?? "");
$sifre = trim($_POST["sifre"] ?? "");

if (empty($isim) || empty($sifre)) {
    exit("Eksik veri");
}

$sql = "SELECT id, isim, sifre FROM kullanicilar WHERE isim = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$isim]);
$kullanici = $sorgu->fetch(PDO::FETCH_ASSOC);

if ($kullanici && sifreDogrula($sifre, $kullanici["sifre"])) {
    echo "OK|" . $kullanici["id"] . "|" . $kullanici["isim"];
} else {
    echo "HATA";
}
?>