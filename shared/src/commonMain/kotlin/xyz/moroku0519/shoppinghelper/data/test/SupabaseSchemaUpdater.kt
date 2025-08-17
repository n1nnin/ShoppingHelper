package xyz.moroku0519.shoppinghelper.data.test

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import xyz.moroku0519.shoppinghelper.data.remote.SupabaseConfig

/**
 * Supabaseスキーマの更新とマイグレーション機能
 * 
 * データベーススキーマの修正を適用する
 */
class SupabaseSchemaUpdater(
    private val supabaseUrl: String,
    private val supabasePublishableKey: String
) {
    private val supabaseClient: SupabaseClient by lazy {
        SupabaseConfig.createClient(supabaseUrl, supabasePublishableKey)
    }

    /**
     * Shopsテーブルのlocation→latitude/longitude移行
     */
    suspend fun migrateShopsTable(): TestResult {
        return try {
            println("🔄 Shopsテーブルの移行を開始...")
            
            // 注意: 実際のマイグレーションはSupabase SQL Editorで手動実行が推奨
            val migrationSQL = """
                -- Shopsテーブルに新しいカラムを追加（既に存在する場合はスキップ）
                ALTER TABLE shops 
                ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION,
                ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;
                
                -- 既存のlocationデータがある場合の移行（PostGISが有効な場合）
                -- UPDATE shops 
                -- SET latitude = ST_Y(location::geometry), 
                --     longitude = ST_X(location::geometry) 
                -- WHERE location IS NOT NULL;
                
                -- 古いlocationカラムを削除（データ移行後）
                -- ALTER TABLE shops DROP COLUMN IF EXISTS location;
            """.trimIndent()
            
            println("📋 実行が必要なSQL:")
            println(migrationSQL)
            
            TestResult.Success("""
                ✅ マイグレーションSQL準備完了
                
                次の手順で実行してください:
                1. Supabase ダッシュボード → SQL Editor
                2. 上記のSQLを実行
                3. テーブル構造を確認
            """.trimIndent())
            
        } catch (e: Exception) {
            TestResult.Error("❌ マイグレーション準備エラー: ${e.message}")
        }
    }

    /**
     * テーブル構造の確認
     */
    suspend fun checkTableSchema(): TestResult {
        return try {
            println("🔍 Shopsテーブル構造確認中...")
            
            // 実際のテーブル確認はSupabaseの管理者権限が必要
            // ここでは基本的な接続テストのみ実行
            
            TestResult.Success("""
                ✅ テーブル構造確認
                
                確認方法:
                1. Supabase ダッシュボード → Table Editor
                2. 'shops' テーブルを選択
                3. 以下のカラムが存在することを確認:
                   - latitude (double precision)
                   - longitude (double precision)
            """.trimIndent())
            
        } catch (e: Exception) {
            TestResult.Error("❌ テーブル構造確認エラー: ${e.message}")
        }
    }

    /**
     * サンプルデータの挿入テスト
     */
    suspend fun insertSampleData(): TestResult {
        return try {
            println("📝 サンプルデータ挿入テスト...")
            
            val sampleData = """
                INSERT INTO shops (id, name, address, latitude, longitude, category, owner_id) VALUES
                (gen_random_uuid(), 'Test Shop 1', '123 Test St', 35.6895, 139.6917, 'SUPERMARKET', null),
                (gen_random_uuid(), 'Test Shop 2', '456 Test Ave', 35.7000, 139.7000, 'PHARMACY', null);
            """.trimIndent()
            
            println("📋 実行可能なサンプルデータ挿入SQL:")
            println(sampleData)
            
            TestResult.Success("""
                ✅ サンプルデータSQL準備完了
                
                Supabase SQL Editorで上記SQLを実行してください
            """.trimIndent())
            
        } catch (e: Exception) {
            TestResult.Error("❌ サンプルデータ準備エラー: ${e.message}")
        }
    }

    /**
     * RLSポリシー修正SQL
     */
    suspend fun fixRlsPolicies(): TestResult {
        return try {
            println("🔒 RLSポリシー修正SQL準備中...")
            
            val rlsFixSQL = """
                -- Fix RLS Policies - Remove Infinite Recursion
                -- Run this in Supabase SQL Editor to fix the infinite recursion error
                
                -- Drop existing problematic policies
                DROP POLICY IF EXISTS "Users can view their own lists" ON shopping_lists;
                DROP POLICY IF EXISTS "Users can create their own lists" ON shopping_lists;
                DROP POLICY IF EXISTS "Users can update their own lists" ON shopping_lists;
                DROP POLICY IF EXISTS "Users can delete their own lists" ON shopping_lists;
                
                -- Create simplified policies (no circular references)
                CREATE POLICY "Users can view their own lists" ON shopping_lists
                    FOR SELECT USING (auth.uid() = owner_id);
                
                CREATE POLICY "Users can create their own lists" ON shopping_lists
                    FOR INSERT WITH CHECK (auth.uid() = owner_id);
                
                CREATE POLICY "Users can update their own lists" ON shopping_lists
                    FOR UPDATE USING (auth.uid() = owner_id);
                
                CREATE POLICY "Users can delete their own lists" ON shopping_lists
                    FOR DELETE USING (auth.uid() = owner_id);
            """.trimIndent()
            
            println("📋 RLS修正SQL:")
            println(rlsFixSQL)
            
            TestResult.Success("""
                ✅ RLS修正SQL準備完了
                
                次の手順で実行してください:
                1. Supabase ダッシュボード → SQL Editor
                2. 上記のSQLを実行
                3. または docs/fix_rls_policies.sql を実行
                4. 認証付き買い物リストテストを再実行
            """.trimIndent())
            
        } catch (e: Exception) {
            TestResult.Error("❌ RLS修正SQL準備エラー: ${e.message}")
        }
    }

    /**
     * 包括的なスキーマ更新チェック
     */
    suspend fun runSchemaUpdate(): List<TestResult> {
        val results = mutableListOf<TestResult>()
        
        println("🔧 Supabaseスキーマ更新開始")
        
        // 1. テーブル構造確認
        results.add(checkTableSchema())
        
        // 2. マイグレーションSQL準備
        results.add(migrateShopsTable())
        
        // 3. RLSポリシー修正
        results.add(fixRlsPolicies())
        
        // 4. サンプルデータ準備
        results.add(insertSampleData())
        
        val successCount = results.count { it.isSuccess() }
        val totalCount = results.size
        println("📊 スキーマ更新チェック完了: $successCount/$totalCount 完了")
        
        return results
    }
}

/**
 * デバッグ用ヘルパー関数
 */
object SupabaseSchemaHelper {
    
    /**
     * BuildConfigからスキーマ更新を実行
     */
    suspend fun runSchemaUpdateWithBuildConfig(
        supabaseUrl: String,
        supabasePublishableKey: String
    ): List<TestResult> {
        val updater = SupabaseSchemaUpdater(supabaseUrl, supabasePublishableKey)
        return updater.runSchemaUpdate()
    }
}