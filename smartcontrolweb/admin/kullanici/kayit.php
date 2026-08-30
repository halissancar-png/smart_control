<?php
session_start();
if (isset($_SESSION["id"])) { header("Location: /smartcontrol/admin/panel.php"); exit; }
?>
<!DOCTYPE html>
<html lang="tr">
<head>
<meta charset="UTF-8">
<title>Yeni Kullanıcı</title>
<link rel="stylesheet" href="/smartcontrol/admin/css/login.css">
</head>
<body>
<div class="login-box">
    <h1>SmartControl</h1>
    <p>Yeni Kullanıcı Kaydı</p>
    <form action="kaydet.php" method="post">
        <input type="text" name="isim" placeholder="Kullanıcı adı" maxlength="20" required>
        <input type="password" name="sifre" placeholder="Şifre" maxlength="20" required>
        <button type="submit">Kayıt Ol</button>
    </form>
    <p>Zaten hesabın var mı? <a href="/smartcontrol/admin/index.php">Giriş Yap</a></p>
</div>
</body>
</html>