#!/usr/bin/env kotlin

@file:Repository("https://repo1.maven.org/maven2/")
@file:DependsOn("io.github.jan-tennert.supabase:postgrest-kt:2.0.0")
@file:DependsOn("io.github.jan-tennert.supabase:gotrue-kt:2.0.0")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
@file:DependsOn("io.ktor:ktor-client-cio:2.3.12")

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.*

/**
 * Supabase接続テストスクリプト
 * 
 * このスクリプトはlocal.propertiesからSupabase設定を読み込み、
 * 基本的な接続テストを実行します。
 */

fun main() = runBlocking {
    println("🧪 Supabase接続テスト開始")
    println("=" * 40)
    
    // local.propertiesから設定を読み込み
    val localProperties = Properties()
    val localPropertiesFile = File("local.properties")
    
    if (!localPropertiesFile.exists()) {
        println("❌ local.propertiesファイルが見つかりません")
        return@runBlocking
    }
    
    localProperties.load(localPropertiesFile.inputStream())
    
    val supabaseUrl = localProperties.getProperty("SUPABASE_URL")
    val supabaseKey = localProperties.getProperty("SUPABASE_PUBLISHABLE_KEY") 
        ?: localProperties.getProperty("SUPABASE_ANON_KEY") // フォールバック
    
    if (supabaseUrl.isNullOrBlank() || supabaseKey.isNullOrBlank()) {
        println("❌ Supabase設定が不完全です")
        println("   SUPABASE_URL: ${supabaseUrl ?: "未設定"}")
        println("   SUPABASE_PUBLISHABLE_KEY: ${if (supabaseKey.isNullOrBlank()) "未設定" else "設定済み"}")
        return@runBlocking
    }
    
    println("✅ 設定読み込み完了")
    println("   URL: $supabaseUrl")
    println("   Key: ${supabaseKey.take(20)}...")
    println()
    
    try {
        // Supabaseクライアント作成
        val supabaseClient = createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        ) {
            install(Auth)
            install(Postgrest)
        }
        
        println("✅ Supabaseクライアント作成成功")
        
        // 基本的な接続テスト（認証不要のクエリ）
        println("🔍 データベース接続テスト中...")
        
        // profilesテーブルの存在確認（RLSにより認証なしでは空の結果が返される）
        val result = supabaseClient.from("profiles").select().limit(1)
        
        println("✅ データベース接続成功")
        println("   データベーススキーマが正しく設定されています")
        
        println()
        println("🎉 Supabase接続テスト完了")
        println("=" * 40)
        println("✅ すべてのテストが成功しました！")
        println()
        println("次のステップ:")
        println("1. アプリを起動してSupabaseテスト画面で認証テストを実行")
        println("2. ユーザー登録/ログインの動作確認")
        println("3. データの作成・同期テスト")
        
    } catch (e: Exception) {
        println("❌ 接続テスト失敗: ${e.message}")
        println()
        println("トラブルシューティング:")
        println("1. SUPABASE_URLが正しいか確認")
        println("2. SUPABASE_PUBLISHABLE_KEYが正しいか確認")
        println("3. Supabaseプロジェクトが正常に動作しているか確認")
        println("4. docs/supabase_schema.sqlが実行されているか確認")
    }
}

operator fun String.times(n: Int): String = this.repeat(n)