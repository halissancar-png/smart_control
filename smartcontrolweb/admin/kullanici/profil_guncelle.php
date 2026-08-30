<?php
session_start();
if (!isset($_SESSION["id"])) exit;

require "../../db/db.php";

$isim = trim($_POST["isim"]);

$sql = "UPDATE kullanicilar SET isim = ? WHERE id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$isim, $_SESSION["id"]]);

$_SESSION["isim"] = $isim;
header("Location: profil.php");
exit;
?>