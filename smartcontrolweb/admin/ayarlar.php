<?php
session_start();
if (!isset($_SESSION["id"])) { header("Location: index.php"); exit; }
require "includes/header.php";
?>

<style>
.ayarlar-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 20px;
    margin-top: 24px;
}
.ayar-kart {
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 14px;
    padding: 24px 20px;
    text-align: center;
    transition: 0.2s;
    text-decoration: none;
    color: #0f172a;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
}
.ayar-kart:hover {
    background: #f1f5f9;
    border-color: #94a3b8;
    transform: translateY(-2px);
}
.ayar-kart .icon { font-size: 32px; }
.ayar-kart .baslik { font-weight: 600; font-size: 16px; }
.ayar-kart .aciklama { font-size: 13px; color: #64748b; }
</style>

<h2>Ayarlar</h2>
<p class="alt-baslik">Sistem yönetim işlemleri.</p>

<div class="ayarlar-grid">

    <a href="/smartcontrolweb/admin/kullanici/profil.php" class="ayar-kart">
        <span class="icon">👤</span>
        <span class="baslik">Profil</span>
        <span class="aciklama">Bilgilerini güncelle</span>
    </a>

    <a href="/smartcontrolweb/admin/kullanici/sifre.php" class="ayar-kart">
        <span class="icon">🔒</span>
        <span class="baslik">Şifre Değiştir</span>
        <span class="aciklama">Güvenlik</span>
    </a>

    <a href="/smartcontrolweb/admin/cikis.php" class="ayar-kart" style="border-color:#fee2e2;">
        <span class="icon">🚪</span>
        <span class="baslik" style="color:#dc2626;">Çıkış</span>
        <span class="aciklama">Oturumu kapat</span>
    </a>

</div>

<?php require "includes/footer.php"; ?>