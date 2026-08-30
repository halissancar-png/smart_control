<?php
session_start();

if (isset($_SESSION["id"])) {
    header("Location: panel.php");
    exit;
}
?>
<!DOCTYPE html>
<html lang="tr">
<head>
<meta charset="UTF-8">
<title>SmartControl Giriş</title>
<link rel="stylesheet" href="css/login.css">
</head>
<body>
<div class="login-box">
    <h1>SmartControl</h1>
    <p>Sistem Kontrol Paneli</p>
    <?php if (isset($_GET["hata"])): ?>
        <div class="hata">Kullanıcı adı veya şifre hatalı</div>
    <?php endif; ?>
    <form action="kontrol.php" method="post">
        <input type="text" name="isim" placeholder="Kullanıcı adı" required>
        <input type="password" name="sifre" placeholder="Şifre" required>
        <button type="submit">Giriş Yap</button>
    </form>
    <p>Yeni kullanıcı mısınız? <a href="kullanici/kayit.php">Kayıt Ol</a></p>
</div>
</body>
</html>