<?php
session_start();
if (!isset($_SESSION["id"])) exit;
require "../../db/db.php";

$id = $_GET["id"];
$sql = "SELECT * FROM cihazlar WHERE id = ? AND kullanici_id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$id, $_SESSION["id"]]);
$c = $sorgu->fetch();
if (!$c) die("Cihaz bulunamadı");

require "../includes/header.php";
?>
<h2>Cihaz Düzenle</h2>
<form action="guncelle.php" method="post">
    <input type="hidden" name="id" value="<?php echo $c["id"]; ?>">
    <div class="form-group">
        <label>Cihaz Adı</label>
        <input type="text" name="cihaz_adi" value="<?php echo htmlspecialchars($c["cihaz_adi"]); ?>">
    </div>
    <div class="form-group">
        <label>Aktif</label>
        <input type="checkbox" name="aktif" <?php if ($c["aktif"]) echo "checked"; ?>>
    </div>
    <button class="btn btn-mavi" type="submit">Kaydet</button>
    <a class="btn" href="liste.php?mekan_id=<?php echo $c["mekan_id"]; ?>">İptal</a>
</form>
<?php require "../includes/footer.php"; ?>