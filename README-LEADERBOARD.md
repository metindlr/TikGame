# Küresel puan tablosu kurulumu (Firebase, ücretsiz)

1. https://console.firebase.google.com → proje oluştur → **Build > Realtime Database > Create database**.
2. **Rules** sekmesine şunu yapıştır ve yayınla:

```json
{
  "rules": {
    "scores": {
      ".read": true,
      ".indexOn": ["s"],
      "$uid": {
        ".write": "!data.exists() || newData.child('s').val() >= data.child('s').val()",
        ".validate": "newData.hasChildren(['n','s','c','t']) && newData.child('n').isString() && newData.child('n').val().length <= 14 && newData.child('c').isString() && newData.child('c').val().length <= 2 && newData.child('s').isNumber() && newData.child('s').val() >= 0 && newData.child('s').val() <= 500 && newData.child('s').val() <= (data.exists() ? data.child('s').val() + 100 : 150) && newData.child('t').isNumber() && newData.child('t').val() <= now + 300000"
      }
    },
    "reports": {
      ".read": false,
      "$target": {
        "$reporter": {
          ".write": "!data.exists()",
          ".validate": "newData.isNumber()"
        }
      }
    }
  }
}
```

3. Database ekranındaki adresi kopyala (örn. `https://proje-adi-default-rtdb.firebaseio.com`).
4. `app/src/main/assets/index.html` dosyasında şu satıra yapıştır:
   `const BOARD_URL='https://proje-adi-default-rtdb.firebaseio.com';`
5. Yeniden derle (GitHub Actions ile APK).

Not: Skorlar istemciden gönderildiği için ileri düzey hile yapan biri sahte skor yazabilir. Ciddi bir yayın için Cloud Functions ile sunucu tarafı doğrulama eklemek gerekir.

## Uygunsuz isim bildirimleri
Oyuncular dünya tablosunda ⚑ düğmesiyle isim bildirir. Bildirimleri Firebase konsolunda **Data > reports** altında görürsün (her bildirilen oyuncunun kimliği altında bildirenler listelenir). Uygunsuz kaydı **Data > scores** altında aynı kimliği bulup silebilirsin.

## Sahte skor sınırı
Kurallar bir kaydın tek seferde en fazla 100 puan artmasına (ilk kayıt için en fazla 150) ve 500 puanı geçmemesine izin verir. Bu, "999 puan" gibi basit sahtekârlığı engeller ama kararlı bir kötü niyetli kişiyi tamamen durduramaz. Sınırları oyuna göre `150`, `100` ve `500` değerlerini değiştirerek ayarlayabilirsin.

## Çökme raporu (Crashlytics, isteğe bağlı)
1. Firebase konsolunda aynı projede **Add app > Android** de, paket adı: `com.tikkule.game`.
2. İnen `google-services.json` dosyasını GitHub'da `app/` klasörüne yükle.
3. Konsolda **Release & Monitor > Crashlytics > Enable**.
4. Dosya `app/` içinde olduğu sürece derleme Crashlytics'i otomatik ekler. Dosyayı eklersen gizlilik politikası ve Play "Data safety" formunda **Crash logs / Diagnostics** verisini de işaretle.
Eklenmezse uygulama çökme raporu göndermez.
