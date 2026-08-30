<?php
session_start();
if (!isset($_SESSION["id"])) { header("Location: /smartcontrolweb/admin/index.php"); exit; }
require "../../db/db.php";

$sql = "SELECT c.*, m.isim as mekan_adi 
        FROM cihazlar c
        LEFT JOIN mekanlar m ON m.id = c.mekan_id
        WHERE c.kullanici_id = ?
        ORDER BY m.isim ASC, c.sira ASC";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$_SESSION["id"]]);
$cihazlar = $sorgu->fetchAll(PDO::FETCH_ASSOC);

require "../includes/header.php";
?>

<h2>Cihaz Yönetimi</h2>
<p class="alt-baslik">Tüm cihazlar listelenir. Düzenleme ve silme işlemleri buradan yapılır.</p>

<a class="btn btn-mavi" href="/smartcontrolweb/admin/panel.php">← Kontrol Paneline Dön</a>
<br><br>

<div class="cihaz-grid">
    <?php if (empty($cihazlar)): ?>
        <p style="color:#94a3b8;">Henüz cihaz eklenmemiş.</p>
    <?php else: ?>
        <?php foreach ($cihazlar as $c): ?>
            <div class="cihaz-card">
                <div class="baslik">
                    <h3><?php echo htmlspecialchars($c["cihaz_adi"]); ?></h3>
                    <span style="font-size:13px; color:#64748b;"><?php echo $c["cihaz_tipi"] ?? 'O'; ?> | <?php echo $c["kanal_sayisi"] ?? 1; ?> kanal</span>
                </div>
                <div class="kod">Kod: <?php echo htmlspecialchars($c["cihaz_kodu"]); ?></div>
                <div class="kod" style="color:#64748b;">Mekan: <?php echo htmlspecialchars($c["mekan_adi"] ?? 'Belirtilmemiş'); ?></div>
                <div style="margin-top:12px; display:flex; gap:8px;">
                    <a class="btn" href="duzenle.php?id=<?php echo $c["id"]; ?>" style="padding:4px 12px; font-size:13px;">✏️ Düzenle</a>
                    <a class="btn btn-kirmizi" href="sil.php?id=<?php echo $c["id"]; ?>" onclick="return confirm('Silinsin mi?');" style="padding:4px 12px; font-size:13px;">🗑️ Sil</a>
                </div>
            </div>
        <?php endforeach; ?>
    <?php endif; ?>
</div>

<?php require "../includes/footer.php"; ?>