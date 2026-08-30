<?php
require "../../db/db.php";
session_start();

if (!isset($_SESSION["id"])) {
    exit("Yetkisiz");
}

$cihaz_id = (int)$_POST["id"];
$kanal_durumlari = $_POST["durumlar"];
$deger = $_POST["deger"] ?? 0;

$sql = "UPDATE cihazlar 
        SET kanal_durumlari = ?, deger = ?, online = 1, son_gorulme = NOW() 
        WHERE id = ? AND kullanici_id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$kanal_durumlari, $deger, $cihaz_id, $_SESSION["id"]]);

echo "OK";
?>