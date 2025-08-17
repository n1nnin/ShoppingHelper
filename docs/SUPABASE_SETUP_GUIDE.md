# Supabase セットアップガイド

ShoppingHelperアプリをSupabaseプロジェクトに接続するための詳細ガイドです。

## 📋 前提条件

- [Supabase](https://supabase.com/)アカウント（無料で作成可能）
- ブラウザでSupabaseダッシュボードにアクセス可能
- プロジェクトの`local.properties`が設定済み

## 🎯 Step 1: Supabaseプロジェクト作成

### 1.1 新規プロジェクト作成

1. **Supabaseダッシュボード**にアクセス: https://supabase.com/dashboard
2. **"New project"** をクリック
3. プロジェクト情報を入力：
   ```
   Name: shopping-helper
   Database Password: [安全なパスワードを生成]
   Region: Northeast Asia (Tokyo) - ap-northeast-1
   Pricing Plan: Free tier
   ```
4. **"Create new project"** をクリック
5. プロジェクト作成完了まで **2-3分** 待機

### 1.2 API設定の取得

1. プロジェクト作成後、**"Settings"** → **"API"** に移動
2. 以下の情報をコピー：
   - **Project URL**: `https://your-project-id.supabase.co`
   - **Publishable key (推奨)**: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
   
   **⚠️ 重要**: 従来の "anon key" は Legacy となっています。新しい **"Publishable key"** を使用してください。

## 🔧 Step 2: ローカル設定

### 2.1 local.propertiesの更新

プロジェクトルートの`local.properties`ファイルを編集：

```properties
# Android SDK location (automatically set by Android Studio)
sdk.dir=/path/to/your/android/sdk

# Google Maps API Key
MAPS_API_KEY=your_google_maps_api_key_here

# Supabase Configuration (Phase 2: Cloud Integration)
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_PUBLISHABLE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**⚠️ 重要**: 実際のURLとキーに置き換えてください

### 2.2 設定の確認

```bash
# ビルドして設定が正しく読み込まれるか確認
./gradlew :composeApp:assembleDebug

# BuildConfigが正しく生成されているか確認
cat composeApp/build/generated/source/buildConfig/debug/xyz/moroku0519/shoppinghelper/BuildConfig.java | grep SUPABASE
```

## 🗄️ Step 3: データベーススキーマの実行

### 3.1 SQL Editorでスキーマ実行

1. Supabaseダッシュボードで **"SQL Editor"** を開く
2. **"New query"** をクリック
3. `docs/supabase_schema.sql`の内容をコピー&ペースト
4. **"Run"** をクリックして実行

### 3.2 実行確認

以下のテーブルが作成されることを確認：

```sql
-- 作成されるテーブル一覧
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;
```

期待される結果：
- `item_templates`
- `list_shares`
- `profiles`
- `shopping_items`
- `shopping_lists`
- `shops`

### 3.3 Row Level Security (RLS) 確認

```sql
-- RLSが有効になっているか確認
SELECT schemaname, tablename, rowsecurity 
FROM pg_tables 
WHERE schemaname = 'public' AND rowsecurity = true;
```

## 🧪 Step 4: 接続テスト

### 4.1 基本的な接続テスト

簡単なテストコードを作成して接続を確認：

```kotlin
// テスト用のシンプルな接続確認
class SupabaseConnectionTest {
    private val supabaseClient = SupabaseConfig.createClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY
    )
    
    suspend fun testConnection(): Boolean {
        return try {
            // 基本的なクエリでテスト
            supabaseClient.from("profiles").select().limit(1)
            true
        } catch (e: Exception) {
            println("Connection failed: ${e.message}")
            false
        }
    }
}
```

### 4.2 認証テスト

```kotlin
// 認証機能のテスト
suspend fun testAuthentication() {
    val authManager = AuthenticationManager(supabaseClient)
    
    try {
        // テストユーザーでサインアップ
        val result = authManager.signUpWithEmail(
            email = "test@example.com",
            password = "password123"
        )
        
        if (result.isSuccess) {
            println("✅ Authentication working!")
        } else {
            println("❌ Authentication failed: ${result.exceptionOrNull()}")
        }
    } catch (e: Exception) {
        println("❌ Authentication error: ${e.message}")
    }
}
```

## 🔐 Step 5: セキュリティ設定

### 5.1 認証設定

1. **"Authentication"** → **"Settings"** に移動
2. **Email confirmationの設定**:
   - 開発時: `Confirm email` = OFF（テスト簡素化）
   - 本番時: `Confirm email` = ON（推奨）

### 5.2 プロバイダー設定

開発段階では Email/Password のみで十分ですが、将来的に以下も設定可能：

- **Google OAuth** (Android/iOS用)
- **Apple Sign-In** (iOS用)
- **その他のプロバイダー**

## 📱 Step 6: アプリ統合

### 6.1 DI (依存性注入) 設定

Koinモジュールでスupabaseクライアントを提供：

```kotlin
// AndroidModule.kt
val androidModule = module {
    // Supabase client
    single {
        SupabaseConfig.createClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY
        )
    }
    
    // Authentication manager
    single { AuthenticationManager(get()) }
    
    // 既存のローカルリポジトリ
    single<ShoppingRepository> { SqlDelightShoppingRepository(get()) }
}
```

### 6.2 段階的移行

1. **Phase 2a**: 認証機能のみ統合
2. **Phase 2b**: リモートデータソース統合
3. **Phase 2c**: ハイブリッドリポジトリで完全同期

## 🐛 トラブルシューティング

### よくある問題と解決方法

#### 1. 接続エラー: "Invalid API key"
```
解決方法:
- SUPABASE_ANON_KEYが正しいか確認
- キーに余分なスペースがないか確認
- Supabaseプロジェクトが正常に作成されているか確認
```

#### 2. 認証エラー: "Email not confirmed"
```
解決方法:
- Authentication → Settings で Email confirmation を OFF
- または受信メールの確認リンクをクリック
```

#### 3. データベースエラー: "permission denied"
```
解決方法:
- Row Level Security (RLS) ポリシーが正しく設定されているか確認
- ユーザーが認証されているか確認
- 必要に応じてポリシーを一時的に緩和してテスト
```

#### 4. ビルドエラー: "SUPABASE_URL not found"
```
解決方法:
- local.properties にSUPABASE_URLが設定されているか確認
- ./gradlew clean してから再ビルド
- BuildConfig.java が正しく生成されているか確認
```

## 📊 Step 7: 監視とログ

### 7.1 Supabaseダッシュボード監視

- **"Logs"**: リアルタイムのリクエストログ
- **"API"**: API使用状況とパフォーマンス
- **"Auth"**: ユーザー登録とログイン状況

### 7.2 アプリ側ログ

開発時は詳細なログを追加：

```kotlin
class AuthenticationManager {
    suspend fun signInWithEmail(email: String, password: String): Result<UserInfo> {
        println("🔐 Attempting sign in for: $email")
        return try {
            auth.signInWith(Email) { /* ... */ }
            println("✅ Sign in successful")
            // ...
        } catch (e: Exception) {
            println("❌ Sign in failed: ${e.message}")
            Result.failure(e)
        }
    }
}
```

## 🎉 完了確認

すべて設定完了後、以下が動作することを確認：

- [ ] ビルドが成功する
- [ ] Supabaseプロジェクトにアクセスできる
- [ ] データベーススキーマが正しく作成されている
- [ ] 基本認証（サインアップ/サインイン）が動作する
- [ ] RLSポリシーが適用されている

## 🚀 次のステップ

接続完了後は以下に進みます：

1. **認証UI実装**: ログイン/サインアップ画面
2. **データ同期実装**: ローカル↔リモート自動同期
3. **リアルタイム機能**: 共有リストのライブ更新
4. **本番環境準備**: プロダクション設定とセキュリティ強化

---

**💡 ヒント**: 開発時は Supabase のログを常に監視し、API呼び出しが正常に行われているかリアルタイムで確認することをお勧めします。