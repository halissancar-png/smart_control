<?php
require "../../db/db.php";

// Oturum kontrolü
if (session_status() == PHP_SESSION_NONE) {
    session_start();
}
if (!isset($_SESSION["id"])) {
    exit("Yetkisiz erişim");
}

$kullanici_id = (int)$_POST["kullanici_id"];
$mekan_id = (int)$_POST["mekan_id"];

if ($kullanici_id != $_SESSION["id"]) {
    exit("Yetkisiz erişim");
}

if ($mekan_id == 0) {
    exit("Eksik veri");
}

$sql = "DELETE FROM mekanlar WHERE id = ? AND kullanici_id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$mekan_id, $kullanici_id]);

echo "OK";
?>