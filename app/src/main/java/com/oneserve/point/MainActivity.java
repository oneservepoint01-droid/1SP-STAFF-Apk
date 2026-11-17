package com.oneserve.point;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
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
    private static final int PERMISSION_REQUEST = 1002;

    private boolean waitingForPermission = false;

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
                    FileChooserParams params) {

                if (uploadCallback != null) {
                    uploadCallback.onReceiveValue(null);
                }

                uploadCallback = callback;

                if (requiresImagePermission(params)) {
                    requestImagePermission();
                } else {
                    openDocumentPicker(params);
                }

                return true;
            }
        });

        webView.loadUrl("file:///android_asset/index.html");
    }

    private boolean requiresImagePermission(FileChooserParams params) {
        if (params == null || Build.VERSION.SDK_INT < 23) {
            return false;
        }

        boolean acceptsImage = false;
        String[] types = params.getAcceptTypes();

        if (types != null) {
            for (String type : types) {
                if (type != null &&
                        (type.contains("image") || type.equals("*/*"))) {
                    acceptsImage = true;
                    break;
                }
            }
        }

        if (!acceptsImage) return false;

        if (Build.VERSION.SDK_INT >= 34) {
            return checkSelfPermission(
                    Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                    != PackageManager.PERMISSION_GRANTED;

        } else if (Build.VERSION.SDK_INT >= 33) {
            return checkSelfPermission(
                    Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED;

        } else {
            return checkSelfPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestImagePermission() {
        waitingForPermission = true;

        if (Build.VERSION.SDK_INT >= 34) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            }, PERMISSION_REQUEST);

        } else if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES
            }, PERMISSION_REQUEST);

        } else {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, PERMISSION_REQUEST);
        }
    }

    private void openDocumentPicker(FileChooserParams params) {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");

            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                    "application/pdf",
                    "image/jpeg",
                    "image/png",
                    "image/webp",
                    "image/*"
            });

            if (params != null &&
                    params.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            }

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivityForResult(intent, FILE_REQUEST);

        } catch (Exception e) {
            finishUpload(null);
            Toast.makeText(this,
                    "Unable to open documents",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST && waitingForPermission) {
            waitingForPermission = false;

            // Even if permission is denied, the system picker can open.
            openDocumentPicker(null);
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_REQUEST) {
            Uri[] results = null;

            if (resultCode == RESULT_OK && data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    results = new Uri[count];

                    for (int i = 0; i < count; i++) {
                        results[i] =
                                data.getClipData().getItemAt(i).getUri();
                    }

                } else if (data.getData() != null) {
                    results = new Uri[]{data.getData()};
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
