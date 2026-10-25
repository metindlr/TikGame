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
        ".write": "!data.exists() || newData.child('s').val() > data.child('s').val()",
        ".validate": "newData.hasChildren(['n','s','c','t']) && newData.child('n').isString() && newData.child('n').val().length <= 14 && newData.child('s').isNumber() && newData.child('s').val() >= 0 && newData.child('s').val() <= 500"
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
