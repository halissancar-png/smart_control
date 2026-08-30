<?php
require "../../db/db.php";

if (!isset($_GET["kod"])) {
    exit("KOD YOK");
}

$kod = $_GET["kod"];

$sql = "UPDATE cihazlar SET online = 1, son_gorulme = NOW() WHERE cihaz_kodu = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$kod]);

echo "OK";
?>