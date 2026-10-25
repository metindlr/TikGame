package com.tikkule.game;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

public class MainActivity extends Activity {
    // TEST reklam birimleri. Yayından önce kendi AdMob birim kimliklerinle değiştir.
    private static final String BANNER_ID = "ca-app-pub-3940256099942544/6300978111";
    private static final String INTER_ID = "ca-app-pub-3940256099942544/1033173712";

    private WebView web;
    private AdView banner;
    private InterstitialAd inter;
    private boolean adsStarted = false;

    public class Bridge {
        @JavascriptInterface
        public void showInterstitial() {
            runOnUiThread(() -> showInter());
        }

        @JavascriptInterface
        public void exitApp() {
            runOnUiThread(() -> finishAndRemoveTask());
        }
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#120d2b"));
        root.setOnApplyWindowInsetsListener((v, ins) -> {
            v.setPadding(0, ins.getSystemWindowInsetTop(), 0, ins.getSystemWindowInsetBottom());
            return ins;
        });

        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#120d2b"));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        web.addJavascriptInterface(new Bridge(), "AndroidAds");
        web.loadUrl("file:///android_asset/index.html");
        root.addView(web, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        banner = new AdView(this);
        banner.setAdUnitId(BANNER_ID);
        banner.setAdSize(AdSize.BANNER);
        banner.setVisibility(View.GONE);
        banner.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                banner.setVisibility(View.VISIBLE);
            }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(banner, lp);
        setContentView(root);

        final ConsentInformation ci = UserMessagingPlatform.getConsentInformation(this);
        ci.requestConsentInfoUpdate(this, new ConsentRequestParameters.Builder().build(),
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(this, err -> {
                    if (ci.canRequestAds()) startAds();
                }),
                err -> {
                    if (ci.canRequestAds()) startAds();
                });
        if (ci.canRequestAds()) startAds();
    }

    private void startAds() {
        if (adsStarted) return;
        adsStarted = true;
        MobileAds.initialize(this, status -> { });
        banner.loadAd(new AdRequest.Builder().build());
        loadInter();
    }

    private void loadInter() {
        InterstitialAd.load(this, INTER_ID, new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(InterstitialAd ad) {
                        inter = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError e) {
                        inter = null;
                    }
                });
    }

    private void showInter() {
        if (!adsStarted) return;
        if (inter == null) {
            loadInter();
            return;
        }
        InterstitialAd ad = inter;
        inter = null;
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                loadInter();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError e) {
                loadInter();
            }
        });
        ad.show(this);
    }

    @Override
    protected void onPause() {
        if (banner != null) banner.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (banner != null) banner.resume();
    }

    @Override
    protected void onDestroy() {
        if (banner != null) banner.destroy();
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }
}
