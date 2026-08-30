<?php
session_start();
if (!isset($_SESSION["id"])) exit;

require "../../db/db.php";

$id = (int)$_POST["id"];
$isim = trim($_POST["isim"]);

$sql = "UPDATE mekanlar SET isim = ? WHERE id = ? AND kullanici_id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$isim, $id, $_SESSION["id"]]);

header("Location: liste.php");
exit;
?>