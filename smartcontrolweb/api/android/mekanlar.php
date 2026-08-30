<?php
require "../../db/db.php";

// Oturum kontrolü
if (session_status() == PHP_SESSION_NONE) {
    session_start();
}
if (!isset($_SESSION["id"])) {
    exit("Yetkisiz erişim");
}

$kullanici_id = (int)$_GET["kullanici_id"];

if ($kullanici_id == 0 || $kullanici_id != $_SESSION["id"]) {
    exit("Geçersiz kullanıcı");
}

$sql = "SELECT id, isim FROM mekanlar WHERE kullanici_id = ? ORDER BY sira ASC";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$kullanici_id]);
$mekanlar = $sorgu->fetchAll(PDO::FETCH_ASSOC);

header("Content-Type: application/json");
echo json_encode($mekanlar);
?>