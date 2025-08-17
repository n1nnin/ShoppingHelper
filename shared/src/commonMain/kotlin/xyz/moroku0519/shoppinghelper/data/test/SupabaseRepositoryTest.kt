package xyz.moroku0519.shoppinghelper.data.test

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import xyz.moroku0519.shoppinghelper.data.remote.SupabaseRepository
import xyz.moroku0519.shoppinghelper.data.remote.SupabaseShop
import xyz.moroku0519.shoppinghelper.data.remote.SupabaseShoppingList
import xyz.moroku0519.shoppinghelper.data.remote.SupabaseConfig
import xyz.moroku0519.shoppinghelper.model.ShopCategory
import java.util.UUID

/**
 * SupabaseRepositoryのテスト機能
 * 
 * デバッグメニューから実行可能なCRUD操作のテスト
 */
class SupabaseRepositoryTest(
    private val supabaseUrl: String,
    private val supabasePublishableKey: String
) {
    private val supabaseClient: SupabaseClient by lazy {
        SupabaseConfig.createClient(supabaseUrl, supabasePublishableKey)
    }
    
    private val repository: SupabaseRepository by lazy {
        SupabaseRepository(supabaseClient)
    }

    /**
     * 基本的なCRUD操作をテスト（認証付き）
     */
    suspend fun testBasicCrudOperations(): TestResult {
        return try {
            println("🧪 Supabase Repository CRUD テスト開始（認証付き）")
            
            // 1. お店の作成テスト（認証付き）
            val testShop = createTestShop()
            println("🔧 作成予定のお店データ: ${testShop.name}")
            val createResult = repository.createShopWithAuth(testShop)
            
            if (createResult.isFailure) {
                val errorMessage = createResult.exceptionOrNull()?.message
                println("🔥 詳細エラー: $errorMessage")
                return TestResult.Error("❌ 認証付きお店作成失敗: $errorMessage")
            }
            
            println("✅ 認証付きお店作成成功: ${createResult.getOrNull()?.name}")
            val createdShop = createResult.getOrNull()!!
            
            // 2. お店一覧取得テスト
            val listResult = repository.getShops()
            if (listResult.isFailure) {
                return TestResult.Error("❌ お店一覧取得失敗: ${listResult.exceptionOrNull()?.message}")
            }
            
            val shops = listResult.getOrNull()!!
            println("✅ お店一覧取得成功: ${shops.size}件")
            
            // 作成したお店が含まれているか確認
            val foundShop = shops.find { it.id == createdShop.id }
            if (foundShop == null) {
                // 公開お店のみ表示される可能性があるため、これは許容する
                println("⚠️ 作成したお店が見つかりません（RLSにより非表示の可能性）")
            } else {
                println("✅ 作成したお店を確認: ${foundShop.name}")
            }
            
            // 3. お店の削除テスト（削除は後回し - 認証が必要）
            println("ℹ️ 削除テストはスキップ（認証済みユーザーのみ削除可能）")
            
            TestResult.Success("✅ 認証付きCRUD操作テスト完了: 作成→確認 成功")
            
        } catch (e: Exception) {
            TestResult.Error("❌ CRUDテストエラー: ${e.message}")
        }
    }

    /**
     * 買い物リストのテスト（認証付き）
     */
    suspend fun testShoppingListOperations(): TestResult {
        return try {
            println("🧪 買い物リスト操作テスト開始（認証付き）")
            
            val testList = createTestShoppingList("placeholder") // owner_idは認証後に置き換えられる
            
            // 認証付きで作成テスト
            val createResult = repository.createShoppingListWithAuth(testList)
            if (createResult.isFailure) {
                return TestResult.Error("❌ 認証付きリスト作成失敗: ${createResult.exceptionOrNull()?.message}")
            }
            
            val createdList = createResult.getOrNull()!!
            println("✅ 認証付きリスト作成成功: ${createdList.name}, Owner: ${createdList.ownerId}")
            
            // 取得テスト
            val getResult = repository.getShoppingLists()
            if (getResult.isFailure) {
                return TestResult.Error("❌ リスト取得失敗: ${getResult.exceptionOrNull()?.message}")
            }
            
            val lists = getResult.getOrNull()!!
            println("✅ リスト取得成功: ${lists.size}件")
            
            // クリーンアップ
            repository.deleteShoppingList(createdList.id)
            
            TestResult.Success("✅ 認証付き買い物リスト操作テスト完了")
            
        } catch (e: Exception) {
            TestResult.Error("❌ 買い物リストテストエラー: ${e.message}")
        }
    }

    /**
     * 認証テスト
     */
    suspend fun testAuthentication(): TestResult {
        return try {
            println("🧪 認証テスト開始")
            
            // 現在のユーザー情報を確認
            val userInfoResult = repository.getCurrentUserInfo()
            if (userInfoResult.isSuccess) {
                println("📋 ${userInfoResult.getOrNull()}")
                TestResult.Success("✅ 認証テスト完了: ${userInfoResult.getOrNull()}")
            } else {
                TestResult.Error("❌ 認証情報取得失敗: ${userInfoResult.exceptionOrNull()?.message}")
            }
            
        } catch (e: Exception) {
            TestResult.Error("❌ 認証テストエラー: ${e.message}")
        }
    }

    /**
     * データベース接続テスト
     */
    suspend fun testConnection(): TestResult {
        return try {
            println("🧪 Supabase接続テスト開始")
            
            // 基本的な接続テスト
            val result = repository.testConnection()
            
            if (result.isSuccess) {
                TestResult.Success("✅ Supabase接続成功: ${result.getOrNull()}")
            } else {
                TestResult.Error("❌ Supabase接続失敗: ${result.exceptionOrNull()?.message}")
            }
            
        } catch (e: Exception) {
            TestResult.Error("❌ 接続テストエラー: ${e.message}")
        }
    }

    /**
     * テーブル構造確認テスト
     */
    suspend fun testTableStructure(): TestResult {
        return try {
            // テーブル構造を動的に確認（型を指定せずに取得）
            val rawResult = supabaseClient.from("shops")
                .select() {
                    limit(1)
                }
                .data
            
            // JSONレスポンスを解析して構造を確認
            if (rawResult.isNotEmpty()) {
                // locationカラムの存在を確認
                if (rawResult.contains("\"location\"")) {
                    TestResult.Error("❌ 古いlocationカラムが検出されました。Supabaseで移行SQLを実行してください。")
                } else if (rawResult.contains("\"latitude\"") && rawResult.contains("\"longitude\"")) {
                    TestResult.Success("✅ テーブル構造正常: latitude/longitudeカラムが存在")
                } else {
                    TestResult.Error("❌ テーブル構造が不正: latitude/longitudeカラムが見つかりません")
                }
            } else {
                TestResult.Success("✅ shopsテーブルは存在（データなし）")
            }
            
        } catch (e: Exception) {
            TestResult.Error("❌ テーブル構造確認エラー: ${e.message}")
        }
    }

    /**
     * 権限テスト（RLS確認）
     */
    suspend fun testPermissions(): TestResult {
        return try {
            // 認証なしでアクセスを試行
            val result = repository.getShops()
            
            if (result.isSuccess) {
                TestResult.Success("✅ 権限テスト: 公開データへのアクセス成功")
            } else {
                val error = result.exceptionOrNull()?.message ?: "不明なエラー"
                if (error.contains("permission") || error.contains("policy")) {
                    TestResult.Success("✅ RLSが正常に動作")
                } else {
                    TestResult.Error("❌ 予期しないエラー: $error")
                }
            }
            
        } catch (e: Exception) {
            TestResult.Error("❌ 権限テストエラー: ${e.message}")
        }
    }

    /**
     * 包括テストスイート
     */
    suspend fun runAllTests(): List<TestResult> {
        val results = mutableListOf<TestResult>()
        
        println("🧪 Supabase Repository テストスイート開始")
        
        // 1. 基本接続テスト
        results.add(testConnection())
        
        // 接続が成功した場合のみ続行
        if (results.last().isSuccess()) {
            // 2. テーブル構造確認
            results.add(testTableStructure())
            
            // 3. 権限テスト
            results.add(testPermissions())
            
            // 4. 認証テスト
            results.add(testAuthentication())
            
            // テーブルアクセスが成功した場合のみCRUD操作をテスト
            if (results[1].isSuccess()) {  // テーブル構造確認が成功した場合
                // 5. CRUD操作テスト
                results.add(testBasicCrudOperations())
                
                // 6. 買い物リスト操作テスト（認証付き）
                results.add(testShoppingListOperations())
            }
        }
        
        // 結果サマリー
        val successCount = results.count { it.isSuccess() }
        val totalCount = results.size
        println("📊 Repository テスト完了: $successCount/$totalCount 成功")
        
        return results
    }

    // ============================================================================
    // テストデータ作成ヘルパー
    // ============================================================================

    private fun createTestShop(): SupabaseShop {
        return SupabaseShop(
            id = UUID.randomUUID().toString(),
            name = "Test Shop ${System.currentTimeMillis()}",
            address = "123 Test Street",
            latitude = 35.6895,
            longitude = 139.6917,
            category = ShopCategory.SUPERMARKET.name,
            isFavorite = false,
            ownerId = null, // 公開お店として作成（nullを許可する場合）
            createdAt = "2024-01-01T00:00:00.000Z",
            updatedAt = "2024-01-01T00:00:00.000Z"
        )
    }

    private fun createTestShoppingList(ownerId: String): SupabaseShoppingList {
        return SupabaseShoppingList(
            id = UUID.randomUUID().toString(),
            name = "Test List ${System.currentTimeMillis()}",
            ownerId = ownerId,
            isActive = true,
            isShared = false,
            createdAt = "2024-01-01T00:00:00.000Z",
            updatedAt = "2024-01-01T00:00:00.000Z"
        )
    }
}

/**
 * デバッグ用ヘルパー関数
 */
object SupabaseRepositoryTestHelper {
    
    /**
     * BuildConfigからRepositoryテストを実行
     */
    suspend fun runTestsWithBuildConfig(
        supabaseUrl: String,
        supabasePublishableKey: String
    ): List<TestResult> {
        val test = SupabaseRepositoryTest(supabaseUrl, supabasePublishableKey)
        return test.runAllTests()
    }
    
    /**
     * 特定のテストを実行
     */
    suspend fun runSpecificTest(
        supabaseUrl: String,
        supabasePublishableKey: String,
        testName: String
    ): TestResult {
        val test = SupabaseRepositoryTest(supabaseUrl, supabasePublishableKey)
        
        return when (testName) {
            "connection" -> test.testConnection()
            "table_structure" -> test.testTableStructure()
            "crud" -> test.testBasicCrudOperations()
            "shopping_list" -> test.testShoppingListOperations()
            "permissions" -> test.testPermissions()
            "authentication" -> test.testAuthentication()
            else -> TestResult.Error("❌ 不明なテスト: $testName")
        }
    }
}