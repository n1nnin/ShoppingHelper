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

# Check Supabase configuration
echo ""
echo "🌐 Supabase Configuration (Phase 2)"
echo "==================================="

if grep -q "SUPABASE_URL=" local.properties; then
    echo "✅ SUPABASE_URL found in local.properties"
    supabase_url=$(grep "SUPABASE_URL=" local.properties | cut -d'=' -f2)
    if [[ $supabase_url == *"your-project-id"* ]]; then
        echo "⚠️  SUPABASE_URL is still a placeholder"
        echo "   Update with actual Supabase project URL"
    else
        echo "✅ SUPABASE_URL appears to be configured"
    fi
else
    echo "❌ SUPABASE_URL not found in local.properties"
    echo "   Add: SUPABASE_URL=https://your-project.supabase.co"
fi

if grep -q "SUPABASE_PUBLISHABLE_KEY=" local.properties; then
    echo "✅ SUPABASE_PUBLISHABLE_KEY found in local.properties"
    supabase_key=$(grep "SUPABASE_PUBLISHABLE_KEY=" local.properties | cut -d'=' -f2)
    if [[ $supabase_key == *"your_supabase_publishable_key"* ]]; then
        echo "⚠️  SUPABASE_PUBLISHABLE_KEY is still a placeholder"
        echo "   Update with actual Supabase Publishable key"
    else
        echo "✅ SUPABASE_PUBLISHABLE_KEY appears to be configured"
    fi
else
    echo "❌ SUPABASE_PUBLISHABLE_KEY not found in local.properties"
    echo "   Add: SUPABASE_PUBLISHABLE_KEY=your_publishable_key_here"
    
    # Check for legacy anon key
    if grep -q "SUPABASE_ANON_KEY=" local.properties; then
        echo "⚠️  Found legacy SUPABASE_ANON_KEY - please migrate to SUPABASE_PUBLISHABLE_KEY"
        echo "   The 'anon key' is now legacy. Use 'Publishable key' instead."
    fi
fi

echo ""
echo "🏁 Next Steps"
echo "============="
echo "1. Google Maps Setup:"
echo "   - Ensure APIs are enabled in Google Cloud Console"
echo "   - Package name: xyz.moroku0519.shoppinghelper.debug"
echo "   - SHA-1 fingerprint: (see above)"
echo ""
echo "2. Supabase Setup (Phase 2):"
echo "   - Create project at https://supabase.com/"
echo "   - Run database schema from docs/supabase_schema.sql"
echo "   - Update local.properties with actual URL and Publishable key"
echo "   - Note: Use 'Publishable key' not legacy 'anon key'"
echo "   - Test connection using in-app Supabase test screen"
echo ""
echo "3. Documentation:"
echo "   - See docs/SUPABASE_SETUP_GUIDE.md for detailed setup"
echo "   - Check README.md for troubleshooting guides"
echo ""
echo "4. Debug commands:"
echo "   adb logcat | grep -i 'maps\\|supabase\\|auth'"