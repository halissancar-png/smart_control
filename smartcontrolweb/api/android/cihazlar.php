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

$sql = "SELECT 
            id, 
            cihaz_adi, 
            cihaz_kodu, 
            cihaz_tipi, 
            kanal_sayisi, 
            mekan_id, 
            aktif,
            deger,
            komut,
            son_gorulme
        FROM cihazlar 
        WHERE kullanici_id = ? 
        ORDER BY cihaz_adi";

$sorgu = $pdo->prepare($sql);
$sorgu->execute([$kullanici_id]);
$cihazlar = $sorgu->fetchAll(PDO::FETCH_ASSOC);

header("Content-Type: application/json");
echo json_encode($cihazlar);
?>