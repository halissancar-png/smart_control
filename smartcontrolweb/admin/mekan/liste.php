<?php
session_start();
if (!isset($_SESSION["id"])) { header("Location: /smartcontrolweb/admin/index.php"); exit; }
require "../../db/db.php";

$sql = "SELECT * FROM mekanlar WHERE kullanici_id = ? ORDER BY sira ASC";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$_SESSION["id"]]);
$mekanlar = $sorgu->fetchAll(PDO::FETCH_ASSOC);

require "../includes/header.php";
?>

<h2>Mekan Yönetimi</h2>
<p class="alt-baslik">Mekan ekle, düzenle veya sil.</p>

<a class="btn btn-mavi" href="ekle.php">+ Yeni Mekan</a>
<br><br>

<div class="mekan-grid">
    <?php foreach ($mekanlar as $m): ?>
        <div class="mekan-card" style="cursor:default;">
            <span class="icon">🏢</span>
            <div class="isim"><?php echo htmlspecialchars($m["isim"]); ?></div>
            <div style="margin-top:12px;">
                <a class="btn" href="duzenle.php?id=<?php echo $m["id"]; ?>">✏️ Düzenle</a>
                <a class="btn btn-kirmizi" href="sil.php?id=<?php echo $m["id"]; ?>" onclick="return confirm('Silinsin mi?');">🗑️ Sil</a>
            </div>
        </div>
    <?php endforeach; ?>
</div>

<?php require "../includes/footer.php"; ?>