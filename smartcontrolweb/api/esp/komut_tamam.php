<?php
require "../../db/db.php";

if (!isset($_GET["kod"]) || !isset($_GET["deger"])) {
    exit("Eksik veri");
}

$kod = $_GET["kod"];
$deger = $_GET["deger"];

$sql = "UPDATE cihazlar 
        SET deger = ?, komut = NULL, online = 1, son_gorulme = NOW() 
        WHERE cihaz_kodu = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$deger, $kod]);

echo "OK";
?>