<?php
session_start();
if (!isset($_SESSION["id"])) { header("Location: /smartcontrolweb/admin/index.php"); exit; }
require "../../db/db.php";

$id = (int)$_GET["id"];

$sql = "SELECT * FROM mekanlar WHERE id = ? AND kullanici_id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$id, $_SESSION["id"]]);
$mekan = $sorgu->fetch(PDO::FETCH_ASSOC);
if (!$mekan) die("Mekan bulunamadı");

require "../includes/header.php";
?>
<h2>Mekan Düzenle</h2>
<form action="guncelle.php" method="post">
    <input type="hidden" name="id" value="<?php echo $mekan["id"]; ?>">
    <div class="form-group">
        <label>Mekan Adı</label>
        <input type="text" name="isim" value="<?php echo htmlspecialchars($mekan["isim"]); ?>" required>
    </div>
    <button class="btn btn-mavi" type="submit">Kaydet</button>
    <a class="btn" href="liste.php">İptal</a>
</form>
<?php require "../includes/footer.php"; ?>