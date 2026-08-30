<?php
session_start();
if (!isset($_SESSION["id"])) { header("Location: /smartcontrolweb/admin/index.php"); exit; }
require "../../db/db.php";

$mekan_id = (int)$_GET["mekan_id"];

$sql = "SELECT * FROM mekanlar WHERE id = ? AND kullanici_id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$mekan_id, $_SESSION["id"]]);
$mekan = $sorgu->fetch();
if (!$mekan) die("Mekan bulunamadı");

require "../includes/header.php";
?>
<h2><?php echo htmlspecialchars($mekan["isim"]); ?> - Yeni Cihaz</h2>

<form action="kaydet.php" method="post">
    <input type="hidden" name="mekan_id" value="<?php echo $mekan_id; ?>">
    <div class="form-group">
        <label>Cihaz Adı</label>
        <input type="text" name="cihaz_adi" required>
    </div>
    <div class="form-group">
        <label>Cihaz Kodu (ESP ID)</label>
        <input type="text" name="cihaz_kodu" placeholder="Örn: SNCR_O8_6E79BA" required>
        <small style="color:#64748b;">Tip ve kanal sayısı otomatik algılanır.</small>
    </div>
    <button class="btn btn-mavi" type="submit">Kaydet</button>
    <a class="btn" href="liste.php?mekan_id=<?php echo $mekan_id; ?>">İptal</a>
</form>

<?php require "../includes/footer.php"; ?>