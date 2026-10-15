package app.zoro.prototype;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.View;
import java.io.ByteArrayInputStream;

/** A local shell with allowlisted Hugging Face metadata requests. No JS/native bridge, remote pages, or file picker. */
public final class MainActivity extends Activity {
    private static final String HOME = "https://zoro.local/index.html";
    private WebView webView;

    @Override @SuppressLint("SetJavaScriptEnabled")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(13, 15, 20));
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setSupportMultipleWindows(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return !HOME.equals(request.getUrl().toString());
            }
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (HOME.equals(request.getUrl().toString())) {
                    try {
                        return new WebResourceResponse("text/html", "UTF-8", getAssets().open("index.html"));
                    } catch (java.io.IOException error) {
                        return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream("Bundled prototype is missing.".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                    }
                }
                // Only public model metadata may use the network. Navigation, weights,
                // unrelated hosts and other subresources remain blocked.
                android.net.Uri uri = request.getUrl();
                if ("GET".equals(request.getMethod()) && "https".equals(uri.getScheme())
                    && "huggingface.co".equals(uri.getHost())
                    && ("/api/models".equals(uri.getPath()) || uri.getPath().startsWith("/api/models/"))) {
                    return null;
                }
                return new WebResourceResponse("text/plain", "UTF-8", 403, "Blocked", java.util.Collections.emptyMap(), new ByteArrayInputStream(new byte[0]));
            }
        });
        setContentView(webView);
        // The HTML supplies its own status bar and gesture indicator.
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(true);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
            webView.setOnApplyWindowInsetsListener((view, insets) -> {
                // Android 15 edge-to-edge: keep the composer above the software keyboard.
                android.graphics.Insets ime = insets.getInsets(WindowInsets.Type.ime());
                view.setPadding(0, 0, 0, ime.bottom);
                return insets;
            });
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        webView.loadUrl(HOME);
    }

    @Override public void onBackPressed() {
        webView.evaluateJavascript("window.zoroBack ? window.zoroBack() : false", handled -> {
            if (!"true".equals(handled)) finish();
        });
    }

    @Override protected void onDestroy() {
        if (webView != null) { webView.destroy(); webView = null; }
        super.onDestroy();
    }
}
