include <ESP8266WiFi.h>
#include <WiFiUdp.h>
#include <EEPROM.h>

// ============================================================
// 🔧 MODÜL AYARLARI (SADECE BURASI DEĞİŞTİRİLECEK)
// ============================================================

#define KANAL_SAYISI  8             // 1-8 arası
#define CIHAZ_ID      "6E79BB"      // Benzersiz ID

// ============================================================
// SABİT AYARLAR (DEĞİŞTİRME)
// ============================================================

#define UDP_PORT      4210
#define MAKS_KANAL    8
#define LED_PIN       2

const char* apPassword = "sncr_123";

// ============================================================
// SICAKLIK DEĞERLERİ (float)
// ============================================================

float sicaklikDeger[MAKS_KANAL] = {0.0};

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

// ============================================================
// SICAKLIK ÖLÇÜMÜ (ŞİMDİLİK TEST)
// ============================================================

float sicaklikOku(int kanal) {
  // İleride gerçek sensörden okuma yapılacak
  // Şimdilik rastgele değer üret (negatif ve pozitif)
  static float deger = 25.0;
  deger += random(-10, 11) / 10.0;  // -1.0 ile +1.0 arası değişim
  if (deger > 50.0) deger = 50.0;
  if (deger < -20.0) deger = -20.0;
  return deger;
}

void sicakliklariGuncelle() {
  for (int i = 0; i < KANAL_SAYISI; i++) {
    sicaklikDeger[i] = sicaklikOku(i);
  }
}

// ============================================================
// STATUS CEVABI
// ============================================================

void statusGonder(IPAddress remoteIp, uint16_t remotePort) {
  sicakliklariGuncelle();
  String cevap = "STATUS";
  for (int i = 0; i < KANAL_SAYISI; i++) {
    cevap += "|" + String(i + 1) + "=" + String(sicaklikDeger[i], 1);
  }
  udpCevap(remoteIp, remotePort, cevap);
}

// ============================================================
// AP SSID OLUŞTUR
// ============================================================

void apSSIDOlustur() {
  apSSID = "SNCR_T" + String(KANAL_SAYISI) + "_" + CIHAZ_ID;
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
  Serial.println("SNCR SICAKLIK MODUL BASLATILIYOR");
  Serial.println("----------------------------------");
  
  EEPROM.begin(96);
  
  apSSIDOlustur();
  
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
  int packetSize = Udp.parsePacket();
  if (packetSize <= 0) return;
  
  IPAddress remoteIp = Udp.remoteIP();
  uint16_t remotePort = Udp.remotePort();
  
  int len = Udp.read(packetBuffer, 254);
  if (len <= 0) return;
  
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
  
  // ============================================================
  // CONFIG
  // ============================================================
  
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
  
  // ============================================================
  // STATUS
  // ============================================================
  
  if (veri.equalsIgnoreCase("STATUS")) {
    statusGonder(remoteIp, remotePort);
    return;
  }
  
  // ============================================================
  // KANAL KOMUTU (SICAKLIK MODÜLÜNDE GEÇERSİZ)
  // ============================================================
  
  int p2 = veri.indexOf('|');
  if (p2 != -1) {
    udpCevap(remoteIp, remotePort, "ERROR|NOT_APPLICABLE");
    return;
  }
  
  // ============================================================
  // BİLİNMEYEN KOMUT
  // ============================================================
  
  udpCevap(remoteIp, remotePort, "ERROR|UNKNOWN_COMMAND");
}

