<?php
session_start();
if (!isset($_SESSION["id"])) { header("Location: /smartcontrolweb/admin/index.php"); exit; }
?>
<!DOCTYPE html>
<html lang="tr">
<head>
    <meta charset="UTF-8">
    <title>Mekan Ekle</title>
    <link rel="stylesheet" href="/smartcontrolweb/admin/css/style.css">
</head>
<body>
<div class="topbar">
    <div class="logo">Smart<span>Control</span></div>
    <div class="user"><?php echo htmlspecialchars($_SESSION["isim"]); ?> | <a href="/smartcontrolweb/admin/cikis.php">Çıkış</a></div>
</div>
<div class="container">
    <div class="sidebar">
        <a href="/smartcontrolweb/admin/panel.php">🏠 Mekanlar</a>
        <a href="/smartcontrolweb/admin/mekan/liste.php" class="active">📁 Mekan Yönetimi</a>
        <a href="/smartcontrolweb/admin/cihaz/liste.php">📟 Cihazlar</a>
    </div>
    <div class="content">
        <h2>Yeni Mekan</h2>
        <form action="kaydet.php" method="post">
            <div class="form-group">
                <label>Mekan Adı</label>
                <input type="text" name="isim" required>
            </div>
            <button class="btn btn-mavi" type="submit">Kaydet</button>
            <a class="btn" href="liste.php">İptal</a>
        </form>
    </div>
</div>
</body>
</html>