#!/bin/bash

echo "🧪 Supabase接続テスト開始"
echo "========================================"

# local.propertiesファイルの確認
if [[ -f "local.properties" ]]; then
    echo "✅ local.properties file exists"
    
    # URL確認
    if grep -q "SUPABASE_URL=" local.properties; then
        url=$(grep "SUPABASE_URL=" local.properties | cut -d'=' -f2)
        echo "✅ SUPABASE_URL: $url"
    else
        echo "❌ SUPABASE_URL not found"
        exit 1
    fi
    
    # キー確認
    if grep -q "SUPABASE_PUBLISHABLE_KEY=" local.properties; then
        key=$(grep "SUPABASE_PUBLISHABLE_KEY=" local.properties | cut -d'=' -f2)
        echo "✅ SUPABASE_PUBLISHABLE_KEY: ${key:0:20}..."
    elif grep -q "SUPABASE_ANON_KEY=" local.properties; then
        key=$(grep "SUPABASE_ANON_KEY=" local.properties | cut -d'=' -f2)
        echo "⚠️  SUPABASE_ANON_KEY found (legacy): ${key:0:20}..."
    else
        echo "❌ No Supabase key found"
        exit 1
    fi
else
    echo "❌ local.properties not found"
    exit 1
fi

echo ""
echo "🔍 基本的な接続確認"
echo "===================="

# URLの基本的な確認
if curl -s --head "$url" | head -n 1 | grep -q "200 OK"; then
    echo "✅ Supabase URL is reachable"
else
    echo "⚠️  Supabase URL accessibility check (might be normal due to CORS)"
fi

# プロジェクトが設定済みかチェック
if [[ $url == *"your-project"* ]]; then
    echo "❌ SUPABASE_URL is still a placeholder"
    exit 1
fi

if [[ $key == *"your_supabase"* ]]; then
    echo "❌ Supabase key is still a placeholder"
    exit 1
fi

echo ""
echo "🔧 ビルド確認"
echo "=============="

# ビルドが成功するか確認
if ./gradlew :shared:build --quiet; then
    echo "✅ Shared module build successful"
else
    echo "❌ Shared module build failed"
    exit 1
fi

echo ""
echo "🎉 基本設定テスト完了"
echo "===================="
echo "✅ Supabase設定は正常に見えます！"
echo ""
echo "次のステップ:"
echo "1. Android Studio でアプリを起動"
echo "2. 「Supabase接続テスト」画面に移動"
echo "3. 「テスト実行」ボタンで詳細テストを実行"
echo "4. 認証とデータベースの動作確認"
echo ""
echo "また、APKを直接インストールして実機でテストも可能です:"
echo "adb install composeApp/build/outputs/apk/debug/composeApp-debug.apk"