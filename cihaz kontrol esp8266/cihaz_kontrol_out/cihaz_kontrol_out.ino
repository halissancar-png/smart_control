#include <ESP8266WiFi.h>
#include <WiFiUdp.h>
#include <EEPROM.h>
#include <ESP8266HTTPClient.h>

// ============================================================
// 🔧 MODÜL AYARLARI (SADECE BURASI DEĞİŞTİRİLECEK)
// ============================================================

#define KANAL_SAYISI  8             // 1-8 arası
#define CIHAZ_ID      "6E79BA"      // Benzersiz ID

// ============================================================
// SABİT AYARLAR (DEĞİŞTİRME)
// ============================================================

#define UDP_PORT      4210
#define MAKS_KANAL    8
#define LED_PIN       2

const char* apPassword = "sncr_123";

// Web API sunucu adresi
const char* sunucu = "http://sancarteknik.com/smartcontrolweb";

// ============================================================
// FİZİKSEL KATMAN (DONANIMA GÖRE DEĞİŞEBİLİR)
// ============================================================

const int KANAL_GPIO[MAKS_KANAL] = {5, 4, 0, 2, 14, 12, 13, 15};
volatile byte kanalDurumu = 0x00;

void fizikselCikisGuncelle() {
  for (int i = 0; i < KANAL_SAYISI; i++) {
    bool durum = bitRead(kanalDurumu, i);
    digitalWrite(KANAL_GPIO[i], durum ? HIGH : LOW);
  }
  bool kanal1Durum = bitRead(kanalDurumu, 0);
  digitalWrite(LED_PIN, kanal1Durum ? HIGH : LOW);
}

// ============================================================
// ORTAK YARDIMCILAR
// ============================================================

String apSSID;
WiFiUDP Udp;
char packetBuffer[255];

String readStringFromEEPROM(int offset) {
  String text = "";
  for (int i = 0; i < 31; i++) {
    char ch = EEPROM.read(offset + i);
    if (ch == '\0' || ch == 0xFF) break;
    text += ch;
  }
  return text;
}

void writeStringToEEPROM(int offset, String text) {
  for (int i = 0; i < 32; i++) EEPROM.write(offset + i, 0);
  int uzunluk = text.length();
  if (uzunluk > 31) uzunluk = 31;
  for (int i = 0; i < uzunluk; i++) EEPROM.write(offset + i, text[i]);
  EEPROM.write(offset + uzunluk, '\0');
}

void udpCevap(IPAddress ip, uint16_t port, String mesaj) {
  String cevap = apSSID + "|" + mesaj;
  Udp.beginPacket(ip, port);
  Udp.write(cevap.c_str());
  Udp.endPacket();
  Serial.print("UDP CEVAP: ");
  Serial.println(cevap);
}

void statusGonder(IPAddress remoteIp, uint16_t remotePort) {
  String cevap = "STATUS";
  for (int i = 0; i < KANAL_SAYISI; i++) {
    bool durum = bitRead(kanalDurumu, i);
    cevap += "|" + String(i + 1) + "=" + (durum ? "ON" : "OFF");
  }
  udpCevap(remoteIp, remotePort, cevap);
}

// ============================================================
// KANAL GÜNCELLE
// ============================================================

void kanalGuncelle(int kanal, bool durum) {
  if (kanal < 1 || kanal > KANAL_SAYISI) {
    Serial.println("Geçersiz kanal!");
    return;
  }
  if (durum) {
    bitSet(kanalDurumu, kanal - 1);
  } else {
    bitClear(kanalDurumu, kanal - 1);
  }
  fizikselCikisGuncelle();
}

// ============================================================
// AP SSID OLUŞTUR
// ============================================================

void apSSIDOlustur() {
  apSSID = "SNCR_O" + String(KANAL_SAYISI) + "_" + CIHAZ_ID;
}

// ============================================================
// WEB API KOMUT OKU
// ============================================================
    String webApiKomutOku() {
      if (WiFi.status() != WL_CONNECTED) {
        Serial.println("❌ webApiKomutOku: Wi-Fi bagli degil");
        return "";
      }

      HTTPClient http;
      String url = String(sunucu) + "/api/esp/komut_oku.php?kod=" + apSSID;
      Serial.print("🌐 URL: ");
      Serial.println(url);

      http.begin(url);
      int httpCode = http.GET();
      Serial.print("📡 HTTP Kodu: ");
      Serial.println(httpCode);

      if (httpCode == 200) {
        String cevap = http.getString();
        http.end();
        cevap.trim();
        Serial.print("📥 Cevap: ");
        Serial.println(cevap);
        return cevap;
      } else {
        http.end();
        Serial.println("❌ HTTP HATASI");
        return "";
      }
    }

// ============================================================
// WEB API DURUM GONDER
// ============================================================

void webApiDurumGonder() {
  if (WiFi.status() != WL_CONNECTED) return;

  String durum = "";
  for (int i = 0; i < KANAL_SAYISI; i++) {
    bool durumBit = bitRead(kanalDurumu, i);
    durum += String(i + 1) + "=" + (durumBit ? "ON" : "OFF");
    if (i < KANAL_SAYISI - 1) durum += "|";
  }

  HTTPClient http;
  String url = String(sunucu) + "/api/esp/komut_tamam.php?kod=" + apSSID + "&deger=" + durum;
  http.begin(url);
  int httpCode = http.GET();

  if (httpCode == 200) {
    Serial.println("WEB API Durum gönderildi: " + durum);
  } else {
    Serial.println("WEB API Durum gönderilemedi");
  }
  http.end();
}

// ============================================================
// WEB API KOMUT İŞLE
// ============================================================

void webApiKomutIsle(String komut) {
  if (komut.length() == 0) return;

  Serial.print("WEB API Komut alındı: ");
  Serial.println(komut);

  int idx = 0;
  while (idx < komut.length()) {
    int esittir = komut.indexOf('=', idx);
    if (esittir == -1) break;

    int kanal = komut.substring(idx, esittir).toInt();
    int pipe = komut.indexOf('|', esittir);
    String deger = (pipe == -1) ? komut.substring(esittir + 1) : komut.substring(esittir + 1, pipe);

    if (kanal >= 1 && kanal <= KANAL_SAYISI) {
      if (deger == "ON") {
        bitSet(kanalDurumu, kanal - 1);
      } else if (deger == "OFF") {
        bitClear(kanalDurumu, kanal - 1);
      }
    }

    idx = (pipe == -1) ? komut.length() : pipe + 1;
  }

  fizikselCikisGuncelle();
}

// ============================================================
// SETUP
// ============================================================

void setup() {
  Serial.begin(115200);

  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, LOW);

  delay(1000);

  Serial.println();
  Serial.println("----------------------------------");
  Serial.println("SNCR OUTPUT MODUL BASLATILIYOR");
  Serial.println("----------------------------------");

  EEPROM.begin(96);

  apSSIDOlustur();

  for (int i = 0; i < KANAL_SAYISI; i++) {
    pinMode(KANAL_GPIO[i], OUTPUT);
    digitalWrite(KANAL_GPIO[i], LOW);
  }

  WiFi.mode(WIFI_AP_STA);
  WiFi.softAP(apSSID.c_str(), apPassword);

  Serial.println();
  Serial.println("AP BILGILERI");
  Serial.println("----------------------------------");
  Serial.print("SSID : ");
  Serial.println(apSSID);
  Serial.print("IP   : ");
  Serial.println(WiFi.softAPIP());
  Serial.print("PASS : ");
  Serial.println(apPassword);

  if (Udp.begin(UDP_PORT)) {
    Serial.print("UDP PORT : ");
    Serial.println(UDP_PORT);
    Serial.println("UDP DINLEME AKTIF");
  } else {
    Serial.println("UDP BASLATILAMADI");
  }

  String savedSSID = readStringFromEEPROM(0);
  String savedPass = readStringFromEEPROM(32);

  if (savedSSID.length() > 0) {
    Serial.println();
    Serial.println("KAYITLI WIFI BULUNDU");
    Serial.print("SSID : ");
    Serial.println(savedSSID);

    WiFi.begin(savedSSID.c_str(), savedPass.c_str());
    int counter = 0;
    while (WiFi.status() != WL_CONNECTED && counter < 30) {
      delay(500);
      Serial.print(".");
      counter++;
    }
    if (WiFi.status() == WL_CONNECTED) {
      Serial.println();
      Serial.println("ANA WIFI BAGLANTISI OK");
      Serial.print("LOCAL IP : ");
      Serial.println(WiFi.localIP());
    } else {
      Serial.println();
      Serial.println("ANA WIFI BAGLANAMADI");
    }
  } else {
    Serial.println();
    Serial.println("KAYITLI WIFI YOK");
  }

  Serial.println();
  Serial.println("----------------------------------");
  Serial.println("MODUL HAZIR");
  Serial.println("KANAL SAYISI : " + String(KANAL_SAYISI));
  Serial.println("----------------------------------");
}

// ============================================================
// LOOP
// ============================================================

void loop() {
  // ==========================================================
  // 1. UDP PAKET KONTROLÜ
  // ==========================================================

  int packetSize = Udp.parsePacket();
  if (packetSize > 0) {
    IPAddress remoteIp = Udp.remoteIP();
    uint16_t remotePort = Udp.remotePort();

    int len = Udp.read(packetBuffer, 254);
    if (len > 0) {
      packetBuffer[len] = '\0';
      String mesaj = String(packetBuffer);
      mesaj.trim();

      Serial.println();
      Serial.println("----------------------------------");
      Serial.println("UDP PAKET GELDI");
      Serial.print("Gonderen IP   : ");
      Serial.println(remoteIp);
      Serial.print("Gonderen Port : ");
      Serial.println(remotePort);
      Serial.print("Mesaj         : ");
      Serial.println(mesaj);
      Serial.println("----------------------------------");

      int p1 = mesaj.indexOf('|');
      if (p1 == -1) {
        Serial.println("Gecersiz UDP paketi");
        return;
      }

      String hedef = mesaj.substring(0, p1);
      hedef.trim();

      if (!hedef.equalsIgnoreCase(apSSID)) {
        Serial.print("Paket baska modulu hedefliyor: ");
        Serial.println(hedef);
        return;
      }

      Serial.println("KENDI PAKETIMIZ");

      String veri = mesaj.substring(p1 + 1);
      veri.trim();

      // ---- CONFIG ----
      if (veri.startsWith("CONFIG|") || veri.startsWith("config|")) {
        int p2 = veri.indexOf('|');
        int p3 = veri.indexOf('|', p2 + 1);
        if (p2 == -1 || p3 == -1) {
          udpCevap(remoteIp, remotePort, "CONFIG_ERROR");
          return;
        }
        String hedefSSID = veri.substring(p2 + 1, p3);
        String hedefPass = veri.substring(p3 + 1);
        hedefSSID.trim();
        hedefPass.trim();

        Serial.println();
        Serial.println("CONFIG");
        Serial.print("SSID : ");
        Serial.println(hedefSSID);

        WiFi.disconnect();
        delay(100);
        WiFi.begin(hedefSSID.c_str(), hedefPass.c_str());
        int deneme = 0;
        while (WiFi.status() != WL_CONNECTED && deneme < 20) {
          delay(500);
          Serial.print(".");
          deneme++;
        }

        if (WiFi.status() == WL_CONNECTED) {
          Serial.println();
          Serial.println("WIFI BAGLANTISI BASARILI");
          Serial.print("LOCAL IP : ");
          Serial.println(WiFi.localIP());
          writeStringToEEPROM(0, hedefSSID);
          writeStringToEEPROM(32, hedefPass);
          EEPROM.commit();
          udpCevap(remoteIp, remotePort, "CONFIG_OK|" + WiFi.localIP().toString());
        } else {
          Serial.println();
          Serial.println("WIFI BAGLANTISI BASARISIZ");
          udpCevap(remoteIp, remotePort, "CONFIG_FAIL");
        }
        return;
      }

      // ---- STATUS ----
      if (veri.equalsIgnoreCase("STATUS")) {
        statusGonder(remoteIp, remotePort);
        return;
      }

      // ---- KANAL KOMUTU ----
      int p2 = veri.indexOf('|');
      if (p2 == -1) {
        udpCevap(remoteIp, remotePort, "ERROR|UNKNOWN_COMMAND");
        return;
      }

      String kanalStr = veri.substring(0, p2);
      String komut = veri.substring(p2 + 1);
      komut.trim();

      int kanal = kanalStr.toInt();

      if (kanal < 1 || kanal > KANAL_SAYISI) {
        udpCevap(remoteIp, remotePort, "ERROR|INVALID_CHANNEL");
        return;
      }

      if (komut.equalsIgnoreCase("ON")) {
        kanalGuncelle(kanal, true);
        udpCevap(remoteIp, remotePort, String(kanal) + "|ON");
      } else if (komut.equalsIgnoreCase("OFF")) {
        kanalGuncelle(kanal, false);
        udpCevap(remoteIp, remotePort, String(kanal) + "|OFF");
      } else {
        udpCevap(remoteIp, remotePort, "ERROR|UNKNOWN_COMMAND");
      }
    }
  }

  // ==========================================================
  // 2. WEB API KOMUT KONTROLÜ (Periyodik)
  // ==========================================================

// ==========================================================
// 2. WEB API KOMUT KONTROLÜ (Periyodik)
// ==========================================================

  static unsigned long sonWebApiKontrol = 0;
  if (millis() - sonWebApiKontrol > 5000) {  // 5 saniyede bir
    sonWebApiKontrol = millis();

    Serial.println("🔄 WEB API KONTROL BASLIYOR...");

    if (WiFi.status() != WL_CONNECTED) {
      Serial.println("❌ Wi-Fi bagli degil!");
      return;
    }

    // 1. Komut oku
    String komut = webApiKomutOku();
    if (komut.length() > 0) {
      Serial.println("⚙️ Komut isleniyor...");
      webApiKomutIsle(komut);
    }

    // 2. HER ZAMAN DURUM GÖNDER (komut olsun veya olmasın)
    Serial.println("📤 Durum gonderiliyor...");
    webApiDurumGonder();
  }
}