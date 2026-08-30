function cihazKontrol(id, kanal, deger) {
    fetch("/smartcontrolweb/api/android/komut.php", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: "id=" + id + "&kanal=" + kanal + "&deger=" + deger
    })
    .then(response => response.text())
    .then(data => {
        if (data === "OK") {
            location.reload();
        } else {
            alert("Hata: " + data);
        }
    })
    .catch(error => {
        alert("Bağlantı hatası");
        console.error(error);
    });
};