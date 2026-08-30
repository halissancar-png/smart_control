<?php
require "../../db/db.php";

if (!isset($_GET["kod"])) {
    exit("KOD YOK");
}

$kod = $_GET["kod"];

$sql = "SELECT deger FROM cihazlar WHERE cihaz_kodu = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$kod]);
$cihaz = $sorgu->fetch(PDO::FETCH_ASSOC);

if ($cihaz) {
    echo $cihaz["deger"];
} else {
    echo "-1";
}
?>