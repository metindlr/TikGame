# Tık Kule – Google Play yayın rehberi

## 1) İmza anahtarı (bir kez, kendi bilgisayarında)
```
keytool -genkeypair -v -keystore tikkule.jks -alias tikkule -keyalg RSA -keysize 2048 -validity 10000
base64 -w0 tikkule.jks      # (Mac: base64 -i tikkule.jks)
```
GitHub deposu > Settings > Secrets and variables > Actions altına 4 secret ekle:
`KEYSTORE_B64` (yukarıdaki çıktı), `KEYSTORE_PASSWORD`, `KEY_ALIAS` (tikkule), `KEY_PASSWORD`.
`tikkule.jks` dosyasını ve şifreleri güvenli yerde yedekle; kaybedersen uygulamayı güncelleyemezsin.
Actions bitince **tik-kule-build** içinde imzalı `app-release.aab` (Play Store'a bu yüklenir) ve `app-release.apk` çıkar.

## 2) Yayın öncesi kontrol
- `applicationId` (com.tikkule.game) benzersiz olmalı; başka biri aldıysa `app/build.gradle` içinde değiştir.
- Play Console'da Play App Signing'i kabul et; AAB'yi **Production** veya önce **Internal testing**'e yükle.
- Yeni kişisel geliştirici hesaplarında üretimden önce kapalı test şartı olabilir; Console'daki bildirimi kontrol et.
- Gizlilik politikası bağlantısı: yayımlanan privacy sayfasının adresi (içindeki e-posta alanını doldur).
- Mağaza görselleri: `store/icon-512.png`, `store/feature-1024x500.png`; en az 2 telefon ekran görüntüsü kendin almalısın.

## 3) Data safety (Veri güvenliği) formu için
- Veri toplanıyor mu? **Evet** (AdMob reklamları nedeniyle).
- Cihaz veya diğer kimlikler (reklam kimliği): Amaç = Reklamlar; Google AdMob ile paylaşılıyor.
- Yalnızca BOARD_URL (Firebase puan tablosu) açıksa ek olarak: Kullanıcı adı, oyun skoru, anonim kimlik, yaklaşık bölge (ülke kodu). Amaç = Uygulama işlevselliği.
- Aktarım şifreli (HTTPS): Evet. Silme talebi yolu: e-posta.
- Reklam kimliği beyanı (Advertising ID): Evet, reklam amacıyla kullanılıyor.
- Hedef kitle: **13+ veya 18+** seç. Çocuklara yönelik (Families) seçersen sertifikalı reklam ağı kuralları gerekir.

## 4) İçerik derecelendirmesi
Şiddet, kumar, kullanıcı etkileşimi yok (oyuncu adı hariç, herkese açık) → büyük olasılıkla herkes (3+). Anketi dürüstçe doldur.

## 5) Mağaza metni
**TR – Başlık:** Tık Kule: Blok İstifle
**Kısa (80):** Tek dokunuşla blokları üst üste diz, kuleyi gökyüzüne çıkar!
**Uzun:** Tık Kule, tek parmakla oynanan, bırakması zor bir istifleme oyunudur. Kayan bloğu doğru anda bırak, taşan kısım kesilir. Tam hizaya denk getirirsen "Mükemmel" serisi başlar ve bloklar yeniden büyür. Kule yükseldikçe hızlanır! • Tek dokunuşla oynanış • 9 dil • Dünya ve cihaz puan tablosu • Hesap gerektirmez • Çevrimdışı oynanır

**EN – Title:** Tik Kule: Stack Blocks
**Short (80):** One tap to stack blocks as high as you can. Hit perfect drops to grow!
**Long:** Tik Kule is a one-finger stacking game that's hard to put down. Drop the sliding block at the right moment; anything overhanging is sliced off. Land it perfectly to build a PERFECT streak and regrow your tower. It gets faster the higher you go! • One-tap gameplay • 9 languages • Global and on-device leaderboards • No account needed • Works offline

## 6) Reklamlar (AdMob) – yayından önce
1. admob.google.com'da hesap aç, uygulamayı ekle; bir **Banner** ve bir **Interstitial** birimi oluştur.
2. Şunları TEST kimliklerinden kendi kimliklerinle değiştir:
   - `app/src/main/AndroidManifest.xml` → `com.google.android.gms.ads.APPLICATION_ID`
   - `MainActivity.java` → `BANNER_ID` ve `INTER_ID`
3. Test kimlikleriyle yayına çıkma; kendi reklamlarına tıklamak hesabını kapattırır. Test cihazını AdMob'da tanımla.
4. AdMob > Gizlilik ve mesajlaşma bölümünde **AB kullanıcı onayı (GDPR) mesajını** oluşturup yayınla; yoksa AB'de reklam gelmez.
5. Reklam sıklığı: Her 3. "Tekrar dene" dokunuşunda tam ekran reklam (`index.html` içindeki `plays%3`).
6. Bağımlılık sürümleri (play-services-ads 23.6.0, UMP 3.1.0) derlemede bulunamazsa güncel sürümle değiştir.

## 7) Yeni özellikler için notlar
- **Sürüm numarası:** `versionCode` GitHub derleme numarasından otomatik gelir; her yeni AAB bir öncekinden büyüktür.
- **Kod küçültme (R8):** Yayın sürümünde açık. Yayından önce `app-release.apk`'yı telefona kurup reklam, puan tablosu ve geri tuşunu mutlaka dene.
- **Kullanıcı içeriği (oyuncu adları):** Play politikası gereği isim filtresi ve ⚑ bildirme düğmesi uygulamada var. Bildirimleri Firebase > Data > reports altında düzenli kontrol et.
- **Uygulama içi puanlama:** Rekor 15 ve üstü olunca, en fazla bir kez Google'ın puanlama penceresi açılır (test sürümlerinde görünmeyebilir).
- **Data safety:** Crashlytics eklersen "Crash logs" ve "Diagnostics" verisini, kullanıcı kimliği olarak da "Kurulum kimliği"ni beyan et.
- **WebView adresi:** Oyun artık güvenli `https://appassets.androidplatform.net` adresinden açılıyor. Önceki test kurulumlarında kayıtlı rekor ve jeton sıfırlanır.
