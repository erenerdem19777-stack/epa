package com.epa.akademi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

/**
 * EPA - Bireysel atletik performans ve akademi yönetimi.
 * Uygulama assets/index.html içinde çalışır; kayıtlar cihazda (WebView localStorage) tutulur.
 */
public class MainActivity extends Activity {
    private static final int REQ_FILE = 11;
    private static final int REQ_SAVE = 12;
    private WebView web;
    private ValueCallback<Uri[]> fileCb;
    private byte[] pendingData;
    private String incoming;
    private boolean pageReady;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (Build.VERSION.SDK_INT >= 21) getWindow().setStatusBarColor(Color.parseColor("#111111"));
        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#111111"));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setSupportMultipleWindows(false);
        s.setTextZoom(100);
        web.addJavascriptInterface(new Bridge(), "EPAAndroid");
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (fileCb != null) fileCb.onReceiveValue(null);
                fileCb = cb;
                try {
                    startActivityForResult(p.createIntent(), REQ_FILE);
                } catch (Exception e) {
                    fileCb = null;
                    return false;
                }
                return true;
            }
        });
        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView v, String url) {
                pageReady = true;
                deliverIncoming();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                String u = req.getUrl().toString();
                if (u.startsWith("file:")) return false;
                try {
                    Intent i = u.startsWith("tel:") ? new Intent(Intent.ACTION_DIAL, req.getUrl()) : new Intent(Intent.ACTION_VIEW, req.getUrl());
                    startActivity(i);
                } catch (Exception ignored) {
                    Toast.makeText(MainActivity.this, "Bağlantı açılamadı", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        setContentView(web);
        if (state != null) web.restoreState(state);
        else web.loadUrl("file:///android_asset/index.html");
        readIncoming(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        readIncoming(intent);
    }

    /** WhatsApp vb. uygulamadan "EPA ile aç / paylaş" ile gelen kayıt dosyasını okur. */
    private void readIncoming(Intent it) {
        if (it == null) return;
        Uri u = null;
        if (Intent.ACTION_VIEW.equals(it.getAction())) u = it.getData();
        else if (Intent.ACTION_SEND.equals(it.getAction())) u = it.getParcelableExtra(Intent.EXTRA_STREAM);
        if (u == null) return;
        try {
            InputStream in = getContentResolver().openInputStream(u);
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] b = new byte[16384];
            int n;
            while ((n = in.read(b)) > 0) bo.write(b, 0, n);
            in.close();
            incoming = new String(bo.toByteArray(), StandardCharsets.UTF_8);
            it.setAction(null);
            deliverIncoming();
        } catch (Exception e) {
            Toast.makeText(this, "Dosya okunamadı", Toast.LENGTH_SHORT).show();
        }
    }

    private void deliverIncoming() {
        if (!pageReady || incoming == null) return;
        String js = "window.epaIncoming&&window.epaIncoming(" + JSONObject.quote(incoming) + ")";
        incoming = null;
        web.evaluateJavascript(js, null);
    }

    /** Sayfadaki "dışa aktar / yedek / rapor indir" düğmeleri bu köprüyü kullanır. */
    private class Bridge {
        @JavascriptInterface
        public void shareBinary(final String name, final String b64, final String mime) {
            final byte[] bytes = android.util.Base64.decode(b64, android.util.Base64.DEFAULT);
            runOnUiThread(new Runnable() {
                @Override
                public void run() { shareBytes(name, bytes, mime); }
            });
        }

        @JavascriptInterface
        public void saveBinary(final String name, final String b64, final String mime) {
            final byte[] bytes = android.util.Base64.decode(b64, android.util.Base64.DEFAULT);
            runOnUiThread(new Runnable() {
                @Override
                public void run() { askSave(name, bytes, mime); }
            });
        }

        @JavascriptInterface
        public void share(final String name, final String data) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() { shareFile(name, data); }
            });
        }

        @JavascriptInterface
        public void save(final String name, final String data) {
            final String n = name.toLowerCase();
            final String mime = n.endsWith(".csv") ? "text/csv" : n.endsWith(".json") ? "application/json" : n.endsWith(".html") ? "text/html" : "text/plain";
            runOnUiThread(new Runnable() {
                @Override
                public void run() { askSave(name, data.getBytes(StandardCharsets.UTF_8), mime); }
            });
        }
    }

    private void askSave(String name, byte[] bytes, String mime) {
        pendingData = bytes;
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType(mime);
        i.putExtra(Intent.EXTRA_TITLE, name);
        try {
            startActivityForResult(i, REQ_SAVE);
        } catch (Exception e) {
            Toast.makeText(this, "Kaydetme açılamadı", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareFile(String name, String data) {
        shareBytes(name, data.getBytes(StandardCharsets.UTF_8), "application/json");
    }

    private void shareBytes(String name, byte[] bytes, String mime) {
        try {
            String safe = name.replaceAll("[^A-Za-z0-9._-]", "_");
            File f = new File(ShareProvider.dir(this), safe);
            FileOutputStream os = new FileOutputStream(f);
            os.write(bytes);
            os.close();
            Uri u = Uri.parse("content://" + ShareProvider.AUTH + "/" + safe);
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType(mime);
            i.putExtra(Intent.EXTRA_STREAM, u);
            i.putExtra(Intent.EXTRA_SUBJECT, name);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(i, "Gönder"));
        } catch (Exception e) {
            Toast.makeText(this, "Paylaşılamadı", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == REQ_FILE && fileCb != null) {
            fileCb.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(res, data));
            fileCb = null;
        } else if (req == REQ_SAVE) {
            if (res == RESULT_OK && data != null && data.getData() != null && pendingData != null) {
                try {
                    OutputStream os = getContentResolver().openOutputStream(data.getData());
                    os.write(pendingData);
                    os.close();
                    Toast.makeText(this, "Dosya kaydedildi", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Kaydedilemedi", Toast.LENGTH_SHORT).show();
                }
            }
            pendingData = null;
        }
    }

    /** Geri tuşu: açık pencere varsa onu kapatır, yoksa uygulamadan çıkar. */
    @Override
    public void onBackPressed() {
        String js = "(function(){var d=[].slice.call(document.querySelectorAll('dialog[open]')).pop();"
                + "if(d){d.close();if(!d.id)d.remove();return '1';}"
                + "if(location.hash&&location.hash!=='#panel'){location.hash='panel';return '1';}return '0';})()";
        web.evaluateJavascript(js, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String v) {
                if (!"\"1\"".equals(v)) finish();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    protected void onPause() {
        super.onPause();
        web.evaluateJavascript("window.epaPaused&&window.epaPaused()", null);
        web.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        web.onResume();
        web.evaluateJavascript("window.epaResumed&&window.epaResumed()", null);
    }
}
