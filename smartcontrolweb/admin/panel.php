<?php
session_start();
if (!isset($_SESSION["id"])) { header("Location: index.php"); exit; }
require "../db/db.php";

$kullanici_id = $_SESSION["id"];

$sql = "SELECT m.*, COUNT(c.id) as cihaz_sayisi
        FROM mekanlar m
        LEFT JOIN cihazlar c ON c.mekan_id = m.id AND c.aktif = 1
        WHERE m.kullanici_id = ?
        GROUP BY m.id
        ORDER BY m.sira ASC, m.id ASC";
$sorgu = $pdo->prepare($sql);
$sorgu->execute([$kullanici_id]);
$mekanlar = $sorgu->fetchAll(PDO::FETCH_ASSOC);

require "includes/header.php";
?>

<link rel="stylesheet" href="/smartcontrolweb/admin/css/dark.css">


<style>
/* Gerekli CSS kodları burada olabilir (isteğe bağlı) */
.son-gorulme {
    font-size: 12px;
    color: #94a3b8;
    margin-top: 4px;
}
.kanal-aciklama {
    font-size: 11px;
    color: #94a3b8;
    margin-left: 8px;
}
</style>



<script>
function updateCihazDurumu(card, durum) {
    console.log("Gelen durum:", durum);
    durum.split("|").forEach(function(kanal) {
        var parts = kanal.split("=");
        if (parts.length == 2) {
            var kanalNo = parts[0];
            var deger = parts[1];
            var durumElement = card.querySelector('.kanal-' + kanalNo);
            if (durumElement) {
                var durumMetin = deger == "ON" ? "🟢 Açık" : "🔴 Kapalı";
                durumElement.innerHTML = durumMetin;
                durumElement.style.color = deger == "ON" ? "#16a34a" : "#dc2626";
            } else {
                console.warn("Kanal " + kanalNo + " elemanı bulunamadı");
            }
        }
    });
}

function tumCihazlariGuncelle() {
    document.querySelectorAll('.cihaz-card').forEach(function(card) {
        var cihazKodu = card.dataset.cihazKodu;
        if (cihazKodu) {
            fetch('/smartcontrolweb/api/esp/deger_oku.php?kod=' + cihazKodu)
            .then(response => response.text())
            .then(data => {
                console.log("Cevap:", data);
                if (data && data !== "-1") {
                    updateCihazDurumu(card, data);
                }
            });
        }
    });
}

function cihazKontrol(id, kanal, deger) {
    fetch('/smartcontrolweb/api/android/komut.php', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'id=' + id + '&kanal=' + kanal + '&deger=' + deger + '&kullanici_id=<?php echo $_SESSION["id"]; ?>'
    })
    .then(response => response.text())
    .then(data => {
        if (data === 'OK') {
            // Komut gönderildi
			// Log ekle
            fetch('/smartcontrolweb/api/android/log.php', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'islem=Aç/Kapa&kullanici_id=<?php echo $_SESSION["id"]; ?>'
            });
        } else {
            alert('Hata: ' + data);
        }
    })
    .catch(error => {
        alert('Bağlantı hatası');
        console.error(error);
    });
}

function tumKanallariAc(id) {
    for (var i = 1; i <= 8; i++) {
        cihazKontrol(id, i, 1);
    }
}

function tumKanallariKapat(id) {
    for (var i = 1; i <= 8; i++) {
        cihazKontrol(id, i, 0);
    }
}

document.addEventListener('DOMContentLoaded', function() {
    setTimeout(tumCihazlariGuncelle, 1000);
    setInterval(tumCihazlariGuncelle, 3000);
});
</script>

<h2>Kontrol Paneli</h2>
<p class="alt-baslik">Kontrol etmek istediğin mekanı seç.</p>

<div class="mekan-grid">
    <?php if (empty($mekanlar)): ?>
        <p style="grid-column:1/-1; color:#94a3b8;">Henüz mekan eklenmemiş.</p>
    <?php else: ?>
        <?php foreach ($mekanlar as $m): ?>
            <a href="panel.php?mekan_id=<?php echo $m["id"]; ?>" class="mekan-card">
                <span class="icon">🏢</span>
                <div class="isim"><?php echo htmlspecialchars($m["isim"]); ?></div>
                <div class="adet"><?php echo $m["cihaz_sayisi"]; ?> cihaz</div>
            </a>
        <?php endforeach; ?>
    <?php endif; ?>
</div>

<?php
$mekan_id = isset($_GET["mekan_id"]) ? (int)$_GET["mekan_id"] : 0;
if ($mekan_id > 0):
    $sql = "SELECT * FROM cihazlar WHERE mekan_id = ? AND kullanici_id = ? AND aktif = 1 ORDER BY sira ASC";
    $sorgu = $pdo->prepare($sql);
    $sorgu->execute([$mekan_id, $kullanici_id]);
    $cihazlar = $sorgu->fetchAll(PDO::FETCH_ASSOC);

    $sql = "SELECT isim FROM mekanlar WHERE id = ? AND kullanici_id = ?";
    $sorgu = $pdo->prepare($sql);
    $sorgu->execute([$mekan_id, $kullanici_id]);
    $mekan_adi = $sorgu->fetchColumn();
?>

    <h3 style="margin-top:40px;"><?php echo htmlspecialchars($mekan_adi); ?></h3>
    <p class="alt-baslik">Bu mekandaki cihazlar</p>

    <div class="cihaz-grid">
        <?php if (empty($cihazlar)): ?>
            <p style="color:#94a3b8;">Bu mekanda henüz cihaz yok.</p>
        <?php else: ?>
            <?php foreach ($cihazlar as $c): 
                $tip = $c["cihaz_tipi"] ?? 'O';
                $kanal_sayisi = (int)($c["kanal_sayisi"] ?? 1);
                
                // ✅ son_gorulme formatı
                $sonGorulme = strtotime($c["son_gorulme"]);
                if ($sonGorulme > 0) {
                    $fark = time() - $sonGorulme;
                    if ($fark < 60) {
                        $zaman = "Şimdi";
                    } elseif ($fark < 3600) {
                        $zaman = floor($fark / 60) . " dakika önce";
                    } elseif ($fark < 86400) {
                        $zaman = floor($fark / 3600) . " saat önce";
                    } else {
                        $zaman = date("d.m.Y H:i", $sonGorulme);
                    }
                } else {
                    $zaman = "Hiç bağlanmadı";
                }
                
                // ✅ kanal açıklamaları
                $aciklamaMap = [];
                if (!empty($c["kanal_aciklama"])) {
                    $parcalar = explode("|", $c["kanal_aciklama"]);
                    foreach ($parcalar as $p) {
                        if (strpos($p, ":") !== false) {
                            list($k, $v) = explode(":", $p);
                            $aciklamaMap[$k] = $v;
                        }
                    }
                }
                
                // ✅ kanal durumları
                $kanal_durumlari = [];
                if (!empty($c["deger"])) {
                    $parcalar = explode("|", $c["deger"]);
                    foreach ($parcalar as $p) {
                        if (strpos($p, "=") !== false) {
                            list($k, $v) = explode("=", $p);
                            $kanal_durumlari[$k] = $v;
                        }
                    }
                }
            ?>
                <div class="cihaz-card" data-cihaz-kodu="<?php echo htmlspecialchars($c["cihaz_kodu"]); ?>">
                    <div class="baslik">
                        <h3><?php echo htmlspecialchars($c["cihaz_adi"]); ?></h3>
                        <span style="font-size:13px; color:#64748b;"><?php echo $tip; ?> | <?php echo $kanal_sayisi; ?> kanal</span>
                    </div>
                    <div class="kod">Kod: <?php echo htmlspecialchars($c["cihaz_kodu"]); ?></div>
                    
                    <div class="son-gorulme">🕒 <?php echo $zaman; ?></div>

                    <?php if ($tip == 'O'): ?>
                        <?php for ($i = 1; $i <= $kanal_sayisi; $i++): ?>
                            <div style="display:flex; align-items:center; gap:10px; margin:6px 0; padding:6px 0; border-bottom:1px solid #f1f5f9;">
                                <span style="font-weight:600; width:30px;"><?php echo $i; ?></span>
                                <button class="btn btn-acik" style="padding:4px 14px; font-size:13px;" onclick="cihazKontrol(<?php echo $c['id']; ?>, <?php echo $i; ?>, 1)">Aç</button>
                                <button class="btn btn-kirmizi" style="padding:4px 14px; font-size:13px;" onclick="cihazKontrol(<?php echo $c['id']; ?>, <?php echo $i; ?>, 0)">Kapat</button>
                                <span style="font-size:14px; margin-left:auto;">
                                    <span class="kanal-<?php echo $i; ?>">
                                        <?php echo ($kanal_durumlari[$i] ?? 'OFF') == 'ON' ? '🟢 Açık' : '🔴 Kapalı'; ?>
                                    </span>
                                    <?php if (!empty($aciklamaMap[$i])): ?>
                                        <span class="kanal-aciklama"><?php echo htmlspecialchars($aciklamaMap[$i]); ?></span>
                                    <?php endif; ?>
                                </span>
                            </div>
                        <?php endfor; ?>
                        <div style="display:flex; gap:8px; margin-top:8px;">
                            <button class="btn btn-acik" onclick="tumKanallariAc(<?php echo $c['id']; ?>)">🔓 Tümünü Aç</button>
                            <button class="btn btn-kirmizi" onclick="tumKanallariKapat(<?php echo $c['id']; ?>)">🔒 Tümünü Kapat</button>
                        </div>
                    <?php else: ?>
                        <?php for ($i = 1; $i <= $kanal_sayisi; $i++): ?>
                            <div style="display:flex; align-items:center; gap:10px; margin:4px 0; padding:4px 0; border-bottom:1px solid #f1f5f9;">
                                <span style="font-weight:600; width:30px;"><?php echo $i; ?></span>
                                <span style="font-size:14px; margin-left:auto;">
                                    <?php 
                                    $kanal_durum = $kanal_durumlari[$i] ?? '0';
                                    if ($tip == 'T'): ?>
                                        <?php echo $kanal_durum; ?> °C
                                    <?php elseif ($tip == 'H'): ?>
                                        <?php echo $kanal_durum; ?> %
                                    <?php elseif ($tip == 'A'): ?>
                                        <?php echo $kanal_durum; ?> V
                                    <?php else: ?>
                                        <?php echo $kanal_durum == 'ON' ? '🟢 Açık' : '🔴 Kapalı'; ?>
                                    <?php endif; ?>
                                    <?php if (!empty($aciklamaMap[$i])): ?>
                                        <span class="kanal-aciklama"><?php echo htmlspecialchars($aciklamaMap[$i]); ?></span>
                                    <?php endif; ?>
                                </span>
                            </div>
                        <?php endfor; ?>
                    <?php endif; ?>
                </div>
            <?php endforeach; ?>
        <?php endif; ?>
    </div>
<?php endif; ?>

<?php require "includes/footer.php"; ?>