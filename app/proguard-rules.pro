# JavaScript köprüsü R8 tarafından silinmesin / yeniden adlandırılmasın
-keep class com.tikkule.game.MainActivity$Bridge { *; }
-keepclassmembers class com.tikkule.game.MainActivity$Bridge {
    @android.webkit.JavascriptInterface <methods>;
}
