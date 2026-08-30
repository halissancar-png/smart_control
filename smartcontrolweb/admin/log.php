<?php
session_start();
if (!isset($_SESSION["id"])) { header("Location: index.php"); exit; }
require "../db/db.php";

$sql = "SELECT * FROM loglar WHERE kullanici_id = ? ORDER BY zaman DESC LIMIT 50";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$_SESSION["id"]]);
$loglar = $sorgu->fetchAll(PDO::FETCH_ASSOC);

require "includes/header.php";
?>
<h2>Son İşlemler</h2>
<table style="width:100%; border-collapse:collapse;">
    <thead>
        <tr style="background:#1e293b; color:white;">
            <th style="padding:10px;">Zaman</th>
            <th style="padding:10px;">İşlem</th>
        </tr>
    </thead>
    <tbody>
    <?php foreach ($loglar as $log): ?>
        <tr style="border-bottom:1px solid #e2e8f0;">
            <td style="padding:10px;"><?php echo $log["zaman"]; ?></td>
            <td style="padding:10px;"><?php echo htmlspecialchars($log["islem"]); ?></td>
        </tr>
    <?php endforeach; ?>
    </tbody>
</table>
<?php require "includes/footer.php"; ?>