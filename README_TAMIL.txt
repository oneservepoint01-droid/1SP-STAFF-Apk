ONESERVE POINT Android Project

உள்ளடக்கம்:
- app/src/main/assets/index.html : உங்கள் ONESERVE POINT mobile HTML
- MainActivity.java : Android WebView + PDF / JPG / PNG file upload chooser
- AndroidManifest.xml : Internet + image permission declarations
- .github/workflows/build-apk.yml : GitHub Actions APK build

APK உருவாக்குவது:
1. இந்த ZIP-ஐ extract செய்யவும்.
2. உள்ள files அனைத்தையும் GitHub repository-யின் root-ல் upload செய்யவும்.
3. GitHub -> Actions -> Build ONESERVE POINT APK -> Run workflow.
4. Build முடிந்ததும் ONESERVE-POINT-APK artifact-ஐ download செய்யவும்.

Permission குறிப்பு:
- Image upload செய்யும்போது Android photo permission prompt கேட்கப்படும்.
- PDF / document upload Android system file picker மூலம் நடக்கும். Android-ல் PDF-க்கென தனி App Info storage permission இல்லை.
- Permission deny செய்தாலும் system picker-ல் பயனர் தேர்வு செய்யும் குறிப்பிட்ட file-ஐ upload செய்ய முடியும்.
- இது Debug APK build project; Play Store release signing சேர்க்கப்படவில்லை.

App Logo:
- Uploaded 1SP logo added as Android launcher icon (normal + adaptive icon).
- Logo image also copied to app/src/main/assets/oneserve_point_logo.jpg.
