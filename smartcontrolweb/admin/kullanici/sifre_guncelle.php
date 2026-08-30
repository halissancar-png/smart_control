<?php
session_start();
if (!isset($_SESSION["id"])) exit;

require "../../db/db.php";

$eski = $_POST["eski_sifre"];
$yeni = $_POST["yeni_sifre"];
$tekrar = $_POST["yeni_sifre2"];

if ($yeni != $tekrar) die("Yeni şifreler aynı değil");

$sql = "SELECT sifre FROM kullanicilar WHERE id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$_SESSION["id"]]);
$kullanici = $sorgu->fetch(PDO::FETCH_ASSOC);

if ($eski != $kullanici["sifre"]) die("Eski şifre yanlış");

$sql = "UPDATE kullanicilar SET sifre = ? WHERE id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$yeni, $_SESSION["id"]]);

header("Location: sifre.php");
exit;
?>