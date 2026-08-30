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
$cihaz_id = (int)$_POST["cihaz_id"];

if ($kullanici_id != $_SESSION["id"]) {
    exit("Yetkisiz erişim");
}

if ($cihaz_id == 0) {
    exit("Eksik veri");
}

$sql = "DELETE FROM cihazlar WHERE id = ? AND kullanici_id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$cihaz_id, $kullanici_id]);

echo "OK";
?>