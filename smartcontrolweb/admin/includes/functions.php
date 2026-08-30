<?php
function logEkle($kullanici_id, $islem) {
    global $pdo;
    $sql = "INSERT INTO loglar (kullanici_id, islem) VALUES (?, ?)";
    $sorgu = $pdo->prepare($sql);
    $sorgu->execute([$kullanici_id, $islem]);
}
?>