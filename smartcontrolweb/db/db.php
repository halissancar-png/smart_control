<?php
$host = "localhost";
$dbname = "sancart1_db1";
$username = "sancart1_admin";
$password = "Sncrtknk2026+";

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    die("Veritabanı bağlantı hatası: " . $e->getMessage());
}

// ============================================================
// ŞİFRE YARDIMCI FONKSİYONLARI
// ============================================================

function sifrele($sifre) {
    return password_hash($sifre, PASSWORD_BCRYPT);
}

function sifreDogrula($sifre, $hash) {
    return password_verify($sifre, $hash);
}

// ============================================================
// OTURUM KONTROLÜ (API'LER İÇİN)
// ============================================================

function apiOturumKontrol() {
    if (session_status() == PHP_SESSION_NONE) {
        session_start();
    }
    if (!isset($_SESSION["id"])) {
        http_response_code(401);
        exit("Yetkisiz erişim");
    }
    return $_SESSION["id"];
}

function apiJsonCikti($data) {
    header("Content-Type: application/json");
    echo json_encode($data);
    exit;
}

function apiCikti($mesaj) {
    echo $mesaj;
    exit;
}
?>