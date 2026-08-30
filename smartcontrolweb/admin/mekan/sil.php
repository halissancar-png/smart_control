<?php
session_start();
if (!isset($_SESSION["id"])) exit;

require "../../db/db.php";

$id = (int)$_GET["id"];

$sql = "DELETE FROM mekanlar WHERE id = ? AND kullanici_id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$id, $_SESSION["id"]]);

header("Location: liste.php");
exit;
?>