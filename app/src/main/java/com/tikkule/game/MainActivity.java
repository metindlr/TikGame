package com.tikkule.game;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.window.OnBackInvokedDispatcher;

import androidx.webkit.WebViewAssetLoader;

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
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

public class MainActivity extends Activity {
    // TEST reklam birimleri. Yayından önce kendi AdMob birim kimliklerinle değiştir.
    private static final String BANNER_ID = "ca-app-pub-3940256099942544/6300978111";
    private static final String INTER_ID = "ca-app-pub-3940256099942544/1033173712";
    private static final String REWARDED_ID = "ca-app-pub-3940256099942544/5224354917";
    private static final String ASSET_HOST = "appassets.androidplatform.net";

    private WebView web;
    private AdView banner;
    private InterstitialAd inter;
    private RewardedAd rewarded;
    private boolean adsStarted = false;

    public class Bridge {
        @JavascriptInterface
        public void showInterstitial() {
            runOnUiThread(() -> showInter());
        }

        @JavascriptInterface
        public void showRewarded() {
            runOnUiThread(() -> showReward());
        }

        @JavascriptInterface
        public void requestReview() {
            runOnUiThread(() -> startReview());
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

        final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#120d2b"));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
                return loader.shouldInterceptRequest(r.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                if (ASSET_HOST.equals(u.getHost())) return false;
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, u));
                } catch (Exception e) {
                    // uygun uygulama yok
                }
                return true;
            }
        });
        web.addJavascriptInterface(new Bridge(), "AndroidAds");
        web.loadUrl("https://" + ASSET_HOST + "/assets/index.html");
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

        registerBack();

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

    private void registerBack() {
        if (Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT, () -> handleBack());
        }
    }

    private void handleBack() {
        if (web.canGoBack()) {
            web.goBack();
            return;
        }
        web.evaluateJavascript("(function(){return window.__back?window.__back():false})()", v -> {
            if (!"true".equals(v)) finishAndRemoveTask();
        });
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        handleBack();
    }

    private void js(String code) {
        web.evaluateJavascript(code, null);
    }

    private void startReview() {
        final ReviewManager manager = ReviewManagerFactory.create(this);
        manager.requestReviewFlow().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                manager.launchReviewFlow(this, task.getResult());
            }
        });
    }

    private void startAds() {
        if (adsStarted) return;
        adsStarted = true;
        MobileAds.initialize(this, status -> { });
        banner.loadAd(new AdRequest.Builder().build());
        loadInter();
        loadRewarded();
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

    private void loadRewarded() {
        RewardedAd.load(this, REWARDED_ID, new AdRequest.Builder().build(),
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(RewardedAd ad) {
                        rewarded = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError e) {
                        rewarded = null;
                    }
                });
    }

    private void showReward() {
        if (!adsStarted || rewarded == null) {
            js("window.onRewardFail&&window.onRewardFail()");
            if (adsStarted) loadRewarded();
            return;
        }
        RewardedAd ad = rewarded;
        rewarded = null;
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                loadRewarded();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError e) {
                js("window.onRewardFail&&window.onRewardFail()");
                loadRewarded();
            }
        });
        ad.show(this, item -> js("window.onRewarded&&window.onRewarded()"));
    }

    @Override
    protected void onPause() {
        if (banner != null) banner.pause();
        if (web != null) web.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (banner != null) banner.resume();
        if (web != null) web.onResume();
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
