# Xubopos 🍽️📱

**Xubopos**, modern restoran, kafe ve barlar için geliştirilmiş; internet bağlantısına ihtiyaç duymadan aynı yerel Wi-Fi ağında çalışan yeni nesil bir **Android Adisyon (POS) ve Mutfak Takip Ekranı (KDS)** sistemidir.

---

## 🚀 Öne Çıkan Özellikler

### 1. 📶 İnternet Bağımsız Yerel Ağ Mimarisi (Local Wi-Fi Mesh)
* **IP Yazmaya Son (mDNS / Zeroconf):** Kasa cihazı ağda otomatik olarak yayın yapar (`_xubopos._tcp`). Garson ve mutfak tabletleri hiçbir IP adresi girmeden kasayı saniyeler içinde otomatik olarak bulur ve bağlanır.
* **3 Farklı Cihaz Modu:**
  * **Kasa (Sunucu):** Tüm veritabanını (Room SQLite) barındıran, masaları, adisyonları ve raporları yöneten ana cihaz.
  * **Garson Modu (İstemci):** Sipariş alma, masa açma, ürün ekleme ve siparişi anında mutfağa iletme ekranı.
  * **Mutfak Modu (KDS - İstemci):** Aşçı ve bar personeli için anlık fiş düşen, sipariş durumlarını güncelleyen takip ekranı.

### 2. 🍳 KDS (Kitchen Display System - Mutfak Ekranı)
* **Tablet & Yatay Mod (Landscape) Desteği:** Mutfak tezgâhlarında veya duvara asılı tabletlerde yatay kullanım için optimize edilmiş duyarlı ızgara (grid) tasarımı.
* **Canlı Dijital KDS Saati:** Mutfak ekibi için saniye hassasiyetinde akan canlı dijital saat (`HH:mm:ss`).
* **Canlı Geçen Süre Sayacı:** Her fiş ve her ürün için sipariş verildikten sonra geçen süre canlı hesaplanır. 15 dakikayı aşan fişlerde kırmızı gecikme uyarısı verilir.
* **Akıcı Animasyonlar:**
  * **Yeni Sipariş:** Fiş geldiğinde sesli zil uyarısı ve kehribar sarısı nabız (pulse) efekti.
  * **Tamamlama:** "Teslim Edildi" tıklandığında yumuşak yeşil onay ve akıcı küçülme animasyonu.
* **İnteraktif Kontrol Listesi:** Aşçı hazırladığı ürünün üzerine dokunarak üstünü çizebilir.

### 3. ⏱️ Çift Taraflı Süre Şeffaflığı
* Hem **Mutfak Ekranı (Aşçı)** hem de **Masa Detayı Ekranı (Garson/Kasa)**, hangi ürünün kaç dakika önce sipariş edildiğini canlı olarak görür (`⏱️ 14:25 (8 dk)`).

### 4. 📊 Kasa ve Yönetim Paneli
* Masalar, kat/bölge yönetimi.
* Kategori ve menü ürün yönetimi.
* Masa taşıma ve adisyon birleştirme.
* Parçalı veya tam ödeme (Nakit / Kredi Kartı).
* Günlük ciro, masa doluluk oranları ve kasa raporu.

---

## 🛠️ Teknolojiler & Kütüphaneler

* **Dil:** Kotlin 2.0+
* **Arayüz:** Jetpack Compose & Material 3
* **Yerel Veritabanı:** Room SQLite (Coroutines Flow & KSP)
* **Ağ & İletişim:** Java Sockets (TCP/IP), Gson Packet Serialization, Android Network Service Discovery (mDNS / Zeroconf)
* **Mimar:** MVVM (Model-View-ViewModel) + StateFlow
* **Minimum SDK:** Android 8.0 (API 26)
* **Hedef SDK:** Android 15 (API 35)

---

## 📦 Projeyi Derleme

```bash
# Projeyi klonlayın
git clone https://github.com/umurcelebi/xubopos.git
cd xubopos

# Debug APK derleyin
./gradlew assembleDebug
```

Derlenen APK çıktısı: `app/build/outputs/apk/debug/app-debug.apk`

---

## 📄 Lisans
Bu proje özel kullanım ve geliştirme amacıyla hazırlanmıştır.
