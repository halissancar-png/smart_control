<?php
if (session_status() == PHP_SESSION_NONE) session_start();

if (!isset($_SESSION["id"])) {
    header("Location: /smartcontrolweb/admin/index.php");
    exit;
}
?>
<!DOCTYPE html>
<html lang="tr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SmartControl</title>
    <link rel="stylesheet" href="/smartcontrolweb/admin/css/style.css">
	<script src="/smartcontrolweb/admin/js/panel.js"></script>
</head>
<body>

<div class="topbar">
    <div class="logo">Smart<span>Control</span></div>
    <div class="user">
	    <button onclick="toggleTheme()" style="background:none; border:none; color:white; font-size:18px; cursor:pointer;">
        🌙
    </button>
        <?php echo htmlspecialchars($_SESSION["isim"]); ?>
        <a href="/smartcontrolweb/admin/cikis.php">Çıkış</a>
    </div>
</div>

<div class="container">
    <div class="sidebar">
        <a href="/smartcontrolweb/admin/panel.php" class="active">🏠 Kontrol Paneli</a>
        <a href="/smartcontrolweb/admin/ayarlar.php">⚙️ Ayarlar</a>
        <a href="/smartcontrolweb/admin/log.php">📋 Son İşlemler</a>
    </div>
<div class="content">
	



<script>
	function toggleTheme() {
		document.body.classList.toggle('dark');
		localStorage.setItem('theme', document.body.classList.contains('dark') ? 'dark' : 'light');
	}

	// Sayfa yüklendiğinde tema kontrolü
	document.addEventListener('DOMContentLoaded', function() {
		if (localStorage.getItem('theme') === 'dark') {
			document.body.classList.add('dark');
		}
	});
</script>