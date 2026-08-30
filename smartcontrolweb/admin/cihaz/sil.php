<?php
session_start();
if (!isset($_SESSION["id"])) exit;

require "../../db/db.php";

$id = (int)$_GET["id"];

$sql = "SELECT mekan_id FROM cihazlar WHERE id = ? AND kullanici_id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$id, $_SESSION["id"]]);
$cihaz = $sorgu->fetch();
if (!$cihaz) die("Cihaz bulunamadı");

$sql = "DELETE FROM cihazlar WHERE id = ? AND kullanici_id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$id, $_SESSION["id"]]);

header("Location: liste.php?mekan_id=" . $cihaz["mekan_id"]);
exit;
?>