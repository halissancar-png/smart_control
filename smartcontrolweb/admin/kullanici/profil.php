<?php
session_start();
if (!isset($_SESSION["id"])) { header("Location: /smartcontrol/admin/index.php"); exit; }
require "../../db/db.php";

$sql = "SELECT * FROM kullanicilar WHERE id = ?";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$_SESSION["id"]]);
$kullanici = $sorgu->fetch(PDO::FETCH_ASSOC);

require "../includes/header.php";
require "../includes/sidebar.php";
?>
<h2>Profil Bilgileri</h2>
<form action="profil_guncelle.php" method="post">
    <label>Kullanıcı Adı</label><br>
    <input type="text" name="isim" value="<?php echo htmlspecialchars($kullanici["isim"]); ?>" maxlength="20" required><br><br>
    <input class="btn" type="submit" value="Kaydet">
</form>
<?php require "../includes/footer.php"; ?>