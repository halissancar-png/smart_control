<?php
require "../../db/db.php";

$kullanici_id = (int)$_POST["kullanici_id"];
$cihaz_id = (int)$_POST["cihaz_id"];
$cihaz_adi = trim($_POST["cihaz_adi"]);
$cihaz_kodu = trim($_POST["cihaz_kodu"]);
$mekan_id = (int)$_POST["mekan_id"];
$aktif = (int)$_POST["aktif"];

if ($kullanici_id == 0 || $cihaz_id == 0) {
    exit("Eksik veri");
}

// ✅ Cihazı teknik_id ile değil, id ile güncelle
$sql = "UPDATE cihazlar 
        SET cihaz_adi = ?, cihaz_kodu = ?, mekan_id = ?, aktif = ? 
        WHERE id = ? AND kullanici_id = ?";

$sorgu = $pdo->prepare($sql);
$sorgu->execute([$cihaz_adi, $cihaz_kodu, $mekan_id, $aktif, $cihaz_id, $kullanici_id]);

echo "OK";
?>