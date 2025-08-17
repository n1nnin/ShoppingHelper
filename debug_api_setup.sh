#!/bin/bash

# Google Maps API Setup Debug Script
# This script helps diagnose common Google Maps API setup issues

echo "🗺️  Google Maps API Setup Diagnostics"
echo "======================================"

# Check if local.properties exists
if [[ -f "local.properties" ]]; then
    echo "✅ local.properties file exists"
    
    # Check if MAPS_API_KEY is set
    if grep -q "MAPS_API_KEY=" local.properties; then
        echo "✅ MAPS_API_KEY found in local.properties"
        api_key=$(grep "MAPS_API_KEY=" local.properties | cut -d'=' -f2)
        if [[ ${#api_key} -eq 39 && $api_key == AIza* ]]; then
            echo "✅ API key format looks correct (39 characters, starts with AIza)"
        else
            echo "⚠️  API key format might be incorrect"
            echo "   Expected: 39 characters starting with 'AIza'"
            echo "   Found: ${#api_key} characters starting with '${api_key:0:4}'"
        fi
    else
        echo "❌ MAPS_API_KEY not found in local.properties"
        echo "   Add: MAPS_API_KEY=your_api_key_here"
    fi
else
    echo "❌ local.properties file not found"
    echo "   Copy from: cp local.properties.sample local.properties"
fi

echo ""
echo "📱 Android Debug Information"
echo "============================"

# Get package name for debug build
echo "Debug package name: xyz.moroku0519.shoppinghelper.debug"

# Get SHA-1 fingerprint
if [[ -f ~/.android/debug.keystore ]]; then
    echo "✅ Debug keystore found"
    echo "SHA-1 fingerprint (add this to Google Cloud Console):"
    keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android 2>/dev/null | grep "SHA1:" | sed 's/.*SHA1: //'
else
    echo "❌ Debug keystore not found at ~/.android/debug.keystore"
fi

echo ""
echo "🔧 Build Check"
echo "=============="

# Check if build is successful
if ./gradlew :composeApp:assembleDebug --quiet; then
    echo "✅ Build successful"
    
    # Check if API key is properly injected into manifest
    manifest_file="composeApp/build/intermediates/merged_manifests/debug/processDebugManifest/AndroidManifest.xml"
    if [[ -f "$manifest_file" ]]; then
        if grep -q "com.google.android.geo.API_KEY" "$manifest_file"; then
            echo "✅ API key found in generated AndroidManifest.xml"
            api_in_manifest=$(grep -A 1 "com.google.android.geo.API_KEY" "$manifest_file" | grep "android:value" | sed 's/.*android:value="//;s/".*//')
            if [[ $api_in_manifest == "YOUR_API_KEY_HERE" ]]; then
                echo "⚠️  Default placeholder API key found in manifest"
                echo "   Check local.properties configuration"
            else
                echo "✅ Custom API key properly loaded into manifest"
            fi
        else
            echo "❌ API key meta-data not found in AndroidManifest.xml"
        fi
    else
        echo "❌ Generated AndroidManifest.xml not found"
    fi
else
    echo "❌ Build failed - check Gradle configuration"
fi

echo ""
echo "🏁 Next Steps"
echo "============="
echo "1. Ensure APIs are enabled in Google Cloud Console:"
echo "   - Maps SDK for Android"
echo "   - Places API"
echo ""
echo "2. Configure API key restrictions:"
echo "   - Package name: xyz.moroku0519.shoppinghelper.debug"
echo "   - SHA-1 fingerprint: (see above)"
echo ""
echo "3. If still having issues, check:"
echo "   - Google Cloud Console → APIs & Services → Quotas"
echo "   - Google Cloud Console → APIs & Services → Credentials"
echo ""
echo "4. Test in app and check logcat for detailed error messages:"
echo "   adb logcat | grep -i 'maps\\|google\\|auth'"