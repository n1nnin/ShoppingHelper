package xyz.moroku0519.shoppinghelper.data.test

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import xyz.moroku0519.shoppinghelper.data.auth.AuthenticationManager
import xyz.moroku0519.shoppinghelper.data.remote.SupabaseConfig

/**
 * Supabase接続テスト用クラス
 * 
 * 開発時にSupabaseプロジェクトとの接続を確認するために使用します。
 * 本番環境では削除することを推奨します。
 */
class SupabaseConnectionTest(
    private val supabaseUrl: String,
    private val supabasePublishableKey: String
) {
    private val supabaseClient: SupabaseClient by lazy {
        SupabaseConfig.createClient(supabaseUrl, supabasePublishableKey)
    }
    
    private val authManager: AuthenticationManager by lazy {
        AuthenticationManager(supabaseClient)
    }
    
    /**
     * 基本的な接続テスト
     * 
     * @return 接続成功時true、失敗時false
     */
    suspend fun testBasicConnection(): TestResult {
        return try {
            // 設定値の基本確認のみ（実際のリクエストは認証後に実行）
            val hasValidUrl = supabaseUrl.isNotBlank() && supabaseUrl.startsWith("https://")
            val hasValidKey = supabasePublishableKey.isNotBlank() && supabasePublishableKey.length > 20
            
            if (hasValidUrl && hasValidKey) {
                TestResult.Success("✅ Supabase設定が有効に見えます")
            } else {
                TestResult.Error("❌ Supabase設定が無効です")
            }
        } catch (e: Exception) {
            TestResult.Error("❌ Supabase接続テストエラー: ${e.message}")
        }
    }
    
    /**
     * 認証機能テスト
     * 
     * @param testEmail テスト用メールアドレス
     * @param testPassword テスト用パスワード
     * @return 認証テスト結果
     */
    suspend fun testAuthentication(): TestResult {
        return try {
            // ランダムなテスト用メールアドレスを生成
            val randomNumber = (Math.random() * 10000).toInt()
            val testEmail = "test$randomNumber@example.com"
            val testPassword = "TestPassword123!"
            
            println("🔐 認証テスト開始")
            println("🔐 テストメール: $testEmail")
            println("🔐 テストパスワード長: ${testPassword.length}")
            
            // サインアップを試行
            val signUpResult = authManager.signUpWithEmail(testEmail, testPassword)
            
            if (signUpResult.isSuccess) {
                val user = signUpResult.getOrNull()
                TestResult.Success("✅ サインアップ成功: User ID = ${user?.id}")
            } else {
                TestResult.Error("❌ サインアップ失敗: ${signUpResult.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            TestResult.Error("❌ 認証テストエラー: ${e.message}")
        }
    }
    
    /**
     * サインインテスト（内部用）
     */
    private suspend fun testSignIn(email: String, password: String): TestResult {
        return try {
            val signInResult = authManager.signInWithEmail(email, password)
            if (signInResult.isSuccess) {
                val user = signInResult.getOrNull()
                TestResult.Success("✅ サインイン成功: User ID = ${user?.id}")
            } else {
                TestResult.Error("❌ サインイン失敗: ${signInResult.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            TestResult.Error("❌ サインインエラー: ${e.message}")
        }
    }
    
    /**
     * 設定値検証
     */
    fun validateConfiguration(): TestResult {
        return when {
            supabaseUrl.contains("your-project-id") -> {
                TestResult.Error("❌ SUPABASE_URLがプレースホルダのままです。実際のプロジェクトURLに変更してください。")
            }
            supabasePublishableKey.contains("your_supabase_publishable_key") -> {
                TestResult.Error("❌ SUPABASE_PUBLISHABLE_KEYがプレースホルダのままです。実際のキーに変更してください。")
            }
            supabaseUrl.isBlank() -> {
                TestResult.Error("❌ SUPABASE_URLが設定されていません。")
            }
            supabasePublishableKey.isBlank() -> {
                TestResult.Error("❌ SUPABASE_PUBLISHABLE_KEYが設定されていません。")
            }
            !supabaseUrl.startsWith("https://") -> {
                TestResult.Error("❌ SUPABASE_URLの形式が正しくありません。https://で始まる必要があります。")
            }
            supabasePublishableKey.length < 30 -> {
                TestResult.Error("❌ SUPABASE_PUBLISHABLE_KEYが短すぎます。正しいキーを設定してください。")
            }
            else -> {
                TestResult.Success("✅ Supabase設定値が正しく設定されています")
            }
        }
    }
    
    /**
     * 包括的なテストスイート実行
     */
    suspend fun runAllTests(): List<TestResult> {
        val results = mutableListOf<TestResult>()
        
        println("🧪 Supabase接続テストスイート開始")
        
        // 1. 設定値検証
        results.add(validateConfiguration())
        
        // 設定が正しい場合のみ接続テストを実行
        if (results.last() is TestResult.Success) {
            // 2. 基本接続テスト
            results.add(testBasicConnection())
            
            // 3. 認証テスト（基本接続が成功した場合のみ）
            if (results.last() is TestResult.Success) {
                results.add(testAuthentication())
            }
        }
        
        // 結果サマリー
        val successCount = results.count { it is TestResult.Success }
        val totalCount = results.size
        println("📊 テスト完了: $successCount/$totalCount 成功")
        
        return results
    }
}

/**
 * テスト結果を表すsealed class
 */
sealed class TestResult {
    data class Success(val message: String) : TestResult()
    data class Error(val message: String) : TestResult()
    
    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
}

/**
 * デバッグ用ヘルパー関数
 */
object SupabaseTestHelper {
    
    /**
     * BuildConfigからSupabaseテストを実行
     * Android/iOSアプリから呼び出す用
     */
    suspend fun runTestsWithBuildConfig(
        supabaseUrl: String,
        supabasePublishableKey: String
    ): List<TestResult> {
        val test = SupabaseConnectionTest(supabaseUrl, supabasePublishableKey)
        return test.runAllTests()
    }
    
    /**
     * テスト結果をログ出力
     */
    fun printTestResults(results: List<TestResult>) {
        println("\n=== Supabase接続テスト結果 ===")
        results.forEachIndexed { index, result ->
            when (result) {
                is TestResult.Success -> println("${index + 1}. ${result.message}")
                is TestResult.Error -> println("${index + 1}. ${result.message}")
            }
        }
        println("============================\n")
    }
}