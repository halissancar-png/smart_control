<?php
session_start();
if (!isset($_SESSION["id"])) exit;

require "../../db/db.php";

$id = (int)$_POST["id"];
$cihaz_adi = trim($_POST["cihaz_adi"]);
$aktif = isset($_POST["aktif"]) ? 1 : 0;

$sql = "SELECT mekan_id FROM cihazlar WHERE id = ? AND kullanici_id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$id, $_SESSION["id"]]);
$cihaz = $sorgu->fetch();
if (!$cihaz) die("Cihaz bulunamadı");

$sql = "UPDATE cihazlar SET cihaz_adi = ?, aktif = ? WHERE id = ? AND kullanici_id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$cihaz_adi, $aktif, $id, $_SESSION["id"]]);

header("Location: liste.php?mekan_id=" . $cihaz["mekan_id"]);
exit;
?>