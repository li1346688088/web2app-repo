package com.xgmod.web2app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

    private WebView webView;
    private ProgressBar progressBar;
    private View splash;
    private long splashStart;
    private String HOME_URL = "__URL__";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setStatusBarColor(0xFFB8A7E0);

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progress);
        splash = findViewById(R.id.splash);

        // 开屏：drawable/splash.png 存在则显示，否则隐藏
        boolean hasSplash = getResources().getIdentifier("splash", "drawable", getPackageName()) != 0;
        if (hasSplash) {
            ImageView splashImg = findViewById(R.id.splashImg);
            splashImg.setImageResource(R.drawable.splash);
            splash.setVisibility(View.VISIBLE);
            // 启动标题：非空且非占位符时显示
            TextView splashTitle = findViewById(R.id.splashTitle);
            String st = (splashTitle != null) ? splashTitle.getText().toString() : "";
            if (st != null && !st.isEmpty() && !st.contains("__SPLASH_TITLE__")) {
                splashTitle.setVisibility(View.VISIBLE);
            }
            splashStart = System.currentTimeMillis();
            // 兜底：开屏最多显示 3 秒后自动隐藏
            splash.postDelayed(new Runnable() {
                @Override public void run() { hideSplash(); }
            }, 3000);
        } else {
            splash.setVisibility(View.GONE);
        }

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setBuiltInZoomControls(false);
        s.setSupportZoom(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (uri != null) {
                    String u = uri.toString();
                    if (u.startsWith("http://") || u.startsWith("https://")) {
                        view.loadUrl(u);
                        return true;
                    } else if (u.startsWith("tel:")) {
                        startActivity(new Intent(Intent.ACTION_DIAL, uri));
                        return true;
                    } else if (u.startsWith("mailto:")) {
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                // 开屏至少显示满 3 秒：若页面完成得早，则延迟到满 3 秒再隐藏
                if (splash != null && splash.getVisibility() == View.VISIBLE) {
                    long remain = Math.max(0, 3000 - (System.currentTimeMillis() - splashStart));
                    splash.postDelayed(new Runnable() {
                        @Override public void run() { hideSplash(); }
                    }, remain);
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                // 容忍证书问题（自签/过期/未续签），强制继续加载，避免站点白屏
                handler.proceed();
            }
        });

        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl(HOME_URL);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private void hideSplash() {
        if (splash != null && splash.getVisibility() == View.VISIBLE) {
            splash.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) { webView.destroy(); }
        super.onDestroy();
    }
}
