<?php
session_start();
if (!isset($_SESSION["id"])) exit;

require "../../db/db.php";

$isim = trim($_POST["isim"]);
$kullanici_id = $_SESSION["id"];

$sql = "SELECT MAX(sira) FROM mekanlar WHERE kullanici_id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$kullanici_id]);
$son_sira = $sorgu->fetchColumn() ?: 0;
$son_sira++;

$sql = "INSERT INTO mekanlar (kullanici_id, isim, sira) VALUES (?, ?, ?)";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$kullanici_id, $isim, $son_sira]);

header("Location: liste.php");
exit;
?>