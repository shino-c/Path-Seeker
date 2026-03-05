package com.example.MAD;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

public class ForumFragment extends Fragment {

    private WebView webView;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forum, container, false);

        progressBar = view.findViewById(R.id.progressBar);
        webView = view.findViewById(R.id.webView);
        toolbar = view.findViewById(R.id.toolbar);

        ImageView backIcon = view.findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> {
            Activity activity = getActivity();
            if (activity != null) {
                activity.onBackPressed();
            }
        });

        setUpWebView();
        setUpToolbar(view);

        return view;
    }

    private void setUpWebView() {
        // Enable cookies for the WebView
        CookieManager.getInstance().setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }

        // Configure WebViewClient for handling page navigation
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (isAdded()) {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (isAdded()) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        // Load the URL
        webView.loadUrl("https://forum-pathseeker.blogspot.com/");

        // Enable JavaScript and other settings
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        // Set a custom user-agent string
        webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 10; Mobile; rv:84.0) Gecko/84.0 Firefox/84.0");

        // Allow mixed content
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
    }

    private void setUpToolbar(View view) {
        ImageButton refresh = view.findViewById(R.id.refresh);
        refresh.setOnClickListener(v -> restartFragment());
    }

    private void restartFragment() {
        if (isAdded() && getFragmentManager() != null) {
            getFragmentManager().beginTransaction()
                    .detach(this)
                    .attach(this)
                    .commit();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        webView = null;
        progressBar = null;
        toolbar = null;
    }

    public boolean handleBackPress(KeyEvent event) {
        if (webView != null && event.getAction() == KeyEvent.ACTION_DOWN && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return false;
    }
}