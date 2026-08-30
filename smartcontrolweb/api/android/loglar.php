<?php
require "../../db/db.php";

$kullanici_id = (int)($_POST["kullanici_id"] ?? 0);
$islem = $_POST["islem"] ?? "";

if ($kullanici_id == 0 || empty($islem)) {
    exit("Eksik veri");
}

$sql = "INSERT INTO loglar (kullanici_id, islem) VALUES (?, ?)";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$kullanici_id, $islem]);

echo "OK";
?>