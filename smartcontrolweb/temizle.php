<?php
// ============================================================
// TÜM ÖNBELLEKLERİ TEMİZLE (KESİN ÇÖZÜM)
// ============================================================

// 1. Tarayıcı önbelleğini temizlemesi için başlıklar
header("Cache-Control: no-store, no-cache, must-revalidate, max-age=0");
header("Cache-Control: post-check=0, pre-check=0", false);
header("Pragma: no-cache");
header("Expires: Sat, 26 Jul 1997 05:00:00 GMT");

// 2. PHP Opcache temizle
if (function_exists('opcache_reset')) {
    opcache_reset();
    echo "✅ Opcache temizlendi.<br>";
}

// 3. PHP APCu temizle
if (function_exists('apc_clear_cache')) {
    apc_clear_cache();
    apc_clear_cache('user');
    echo "✅ APCu temizlendi.<br>";
}

// 4. Eğer varsa, geçici dosyaları temizle
$temp_dirs = [
    __DIR__ . '/cache',
    __DIR__ . '/tmp',
    __DIR__ . '/temp',
    sys_get_temp_dir()
];

foreach ($temp_dirs as $dir) {
    if (is_dir($dir)) {
        $files = glob($dir . '/*');
        foreach ($files as $file) {
            if (is_file($file) && basename($file) != 'index.html') {
                @unlink($file);
            }
        }
        echo "✅ $dir temizlendi.<br>";
    }
}

// 5. .htaccess ile önbellek kontrolü
$htaccess = __DIR__ . '/.htaccess';
if (file_exists($htaccess)) {
    $content = file_get_contents($htaccess);
    if (strpos($content, 'ExpiresActive Off') === false) {
        file_put_contents($htaccess, "\n<IfModule mod_expires.c>\n    ExpiresActive Off\n</IfModule>", FILE_APPEND);
        echo "✅ .htaccess güncellendi.<br>";
    }
}

// 6. Sonuç
echo "<br><strong>✅ Tüm önbellekler temizlendi.</strong>";
echo "<br>Şimdi web panelini yeniden yükle (Ctrl+F5).";
?>