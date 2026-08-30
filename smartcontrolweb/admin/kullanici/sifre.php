<?php
session_start();
if (!isset($_SESSION["id"])) { header("Location: /smartcontrol/admin/index.php"); exit; }

require "../includes/header.php";
require "../includes/sidebar.php";
?>
<h2>Şifre Değiştir</h2>
<form action="sifre_guncelle.php" method="post">
    <label>Mevcut Şifre</label><br>
    <input type="password" name="eski_sifre" required><br><br>

    <label>Yeni Şifre</label><br>
    <input type="password" name="yeni_sifre" required><br><br>

    <label>Yeni Şifre Tekrar</label><br>
    <input type="password" name="yeni_sifre2" required><br><br>

    <input class="btn" type="submit" value="Şifre Değiştir">
</form>
<?php require "../includes/footer.php"; ?>