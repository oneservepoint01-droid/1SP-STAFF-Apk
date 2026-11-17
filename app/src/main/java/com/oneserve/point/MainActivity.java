package com.oneserve.point;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {

    private WebView webView;
    private ValueCallback<Uri[]> uploadCallback;

    private static final int FILE_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        webView.setWebViewClient(new WebViewClient());

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onShowFileChooser(
                    WebView view,
                    ValueCallback<Uri[]> callback,
                    WebChromeClient.FileChooserParams params) {

                if (uploadCallback != null) {
                    uploadCallback.onReceiveValue(null);
                }

                uploadCallback = callback;

                openDocumentPicker(params);

                return true;
            }
        });

        webView.loadUrl("file:///android_asset/index.html");
    }

    private void openDocumentPicker(
            WebChromeClient.FileChooserParams params) {

        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");

            intent.putExtra(Intent.EXTRA_MIME_TYPES,
                    new String[]{
                            "application/pdf",
                            "image/jpeg",
                            "image/png",
                            "image/webp"
                    });

            if (params != null &&
                    params.getMode() ==
                    WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE) {

                intent.putExtra(
                        Intent.EXTRA_ALLOW_MULTIPLE, true);
            }

            intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivityForResult(intent, FILE_REQUEST);

        } catch (Exception e) {

            finishUpload(null);

            Toast.makeText(this,
                    "Unable to open documents",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode, resultCode, data);

        if (requestCode == FILE_REQUEST) {

            Uri[] results = null;

            if (resultCode == RESULT_OK && data != null) {

                if (data.getClipData() != null) {

                    int count = data.getClipData().getItemCount();

                    results = new Uri[count];

                    for (int i = 0; i < count; i++) {
                        results[i] =
                                data.getClipData()
                                .getItemAt(i).getUri();
                    }

                } else if (data.getData() != null) {

                    results = new Uri[]{
                            data.getData()
                    };
                }
            }

            finishUpload(results);
        }
    }

    private void finishUpload(Uri[] results) {

        if (uploadCallback != null) {
            uploadCallback.onReceiveValue(results);
            uploadCallback = null;
        }
    }

    @Override
    public void onBackPressed() {

        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {

        finishUpload(null);

        if (webView != null) {
            webView.destroy();
            webView = null;
        }

        super.onDestroy();
    }
}
