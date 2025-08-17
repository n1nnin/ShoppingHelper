package xyz.moroku0519.shoppinghelper.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email

/**
 * Supabaseとの通信を行うRepository
 * 
 * クラウドデータベースとの接続テストとCRUD操作を提供
 */
class SupabaseRepository(
    private val supabaseClient: SupabaseClient
) {
    companion object {
        private const val TABLE_SHOPS = "shops"
        private const val TABLE_SHOPPING_LISTS = "shopping_lists"
        private const val TABLE_SHOPPING_ITEMS = "shopping_items"
    }

    /**
     * 基本的な接続テスト
     */
    suspend fun testConnection(): Result<String> {
        return try {
            // 最も簡単な接続確認: 空の結果でもOK
            try {
                // まずは件数だけ取得を試みる
                val response = supabaseClient.from(TABLE_SHOPS)
                    .select(columns = Columns.list("id")) {
                        count(Count.EXACT)
                        limit(0)
                    }
                
                Result.success("接続成功: Supabaseに接続できました")
            } catch (selectError: Exception) {
                // SELECT失敗時は、より簡単なクエリを試す
                val emptyResult = supabaseClient.from(TABLE_SHOPS)
                    .select(columns = Columns.list("id")) {
                        limit(1)
                    }
                    .decodeList<Map<String, Any?>>()
                
                Result.success("接続成功: Supabaseに接続できました")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * すべてのお店を取得
     */
    suspend fun getShops(): Result<List<SupabaseShop>> {
        return try {
            val shops = supabaseClient.from(TABLE_SHOPS)
                .select(columns = Columns.ALL)
                .decodeList<SupabaseShop>()
            
            Result.success(shops)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * お店を作成（テスト用）
     */
    suspend fun createShop(shop: SupabaseShop): Result<SupabaseShop> {
        return try {
            // insertを使用し、selectで戻り値を取得
            val results = supabaseClient.from(TABLE_SHOPS)
                .insert(shop) {
                    select()
                }
                .decodeList<SupabaseShop>()
            
            if (results.isNotEmpty()) {
                Result.success(results.first())
            } else {
                Result.failure(Exception("お店作成に失敗しました: レスポンスが空です"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 認証付きでお店を作成（RLSテスト用）
     */
    suspend fun createShopWithAuth(shop: SupabaseShop): Result<SupabaseShop> {
        return try {
            // まず一時的なユーザーでサインアップ/ログイン
            val randomEmail = "test${System.currentTimeMillis()}@example.com"
            val password = "TestPassword123!"
            
            // サインアップを試行
            val signUpResult = supabaseClient.auth.signUpWith(Email) {
                email = randomEmail
                this.password = password
            }
            
            // 現在のユーザーIDを取得
            val currentUser = supabaseClient.auth.currentUserOrNull()
            val userId = currentUser?.id ?: throw Exception("ユーザーIDが取得できませんでした")
            
            // 認証されたユーザーのIDを使用してお店を作成
            val authenticatedShop = shop.copy(ownerId = userId)
            
            val results = supabaseClient.from(TABLE_SHOPS)
                .insert(authenticatedShop) {
                    select()
                }
                .decodeList<SupabaseShop>()
            
            val result = if (results.isNotEmpty()) {
                results.first()
            } else {
                throw Exception("認証付きお店作成に失敗しました: レスポンスが空です")
            }
            
            // テスト後はサインアウト
            supabaseClient.auth.signOut()
            
            Result.success(result)
        } catch (e: Exception) {
            // エラーが発生した場合もサインアウトを試行
            try {
                supabaseClient.auth.signOut()
            } catch (signOutError: Exception) {
                // サインアウトエラーは無視
            }
            Result.failure(e)
        }
    }

    /**
     * お店を削除（テスト用）
     */
    suspend fun deleteShop(shopId: String): Result<Unit> {
        return try {
            supabaseClient.from(TABLE_SHOPS)
                .delete {
                    filter {
                        eq("id", shopId)
                    }
                }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 買い物リストを取得
     */
    suspend fun getShoppingLists(): Result<List<SupabaseShoppingList>> {
        return try {
            val lists = supabaseClient.from(TABLE_SHOPPING_LISTS)
                .select()
                .decodeList<SupabaseShoppingList>()
            Result.success(lists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 買い物リストを作成
     */
    suspend fun createShoppingList(list: SupabaseShoppingList): Result<SupabaseShoppingList> {
        return try {
            // insertを使用し、selectで戻り値を取得
            val results = supabaseClient.from(TABLE_SHOPPING_LISTS)
                .insert(list) {
                    select()
                }
                .decodeList<SupabaseShoppingList>()
            
            if (results.isNotEmpty()) {
                Result.success(results.first())
            } else {
                Result.failure(Exception("買い物リスト作成に失敗しました: レスポンスが空です"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 買い物リストを削除
     */
    suspend fun deleteShoppingList(listId: String): Result<Unit> {
        return try {
            supabaseClient.from(TABLE_SHOPPING_LISTS)
                .delete {
                    filter {
                        eq("id", listId)
                    }
                }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 買い物アイテムを取得
     */
    suspend fun getShoppingItems(): Result<List<SupabaseShoppingItem>> {
        return try {
            val items = supabaseClient.from(TABLE_SHOPPING_ITEMS)
                .select()
                .decodeList<SupabaseShoppingItem>()
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 認証付きで買い物リストを作成（RLSテスト用）
     */
    suspend fun createShoppingListWithAuth(list: SupabaseShoppingList): Result<SupabaseShoppingList> {
        return try {
            // まず一時的なユーザーでサインアップ/ログイン
            val randomEmail = "test${System.currentTimeMillis()}@example.com"
            val password = "TestPassword123!"
            
            // サインアップを試行
            val signUpResult = supabaseClient.auth.signUpWith(Email) {
                email = randomEmail
                this.password = password
            }
            
            // 現在のユーザーIDを取得
            val currentUser = supabaseClient.auth.currentUserOrNull()
            val userId = currentUser?.id ?: throw Exception("ユーザーIDが取得できませんでした")
            
            // 認証されたユーザーのIDを使用してリストを作成
            val authenticatedList = list.copy(ownerId = userId)
            
            val results = supabaseClient.from(TABLE_SHOPPING_LISTS)
                .insert(authenticatedList) {
                    select()
                }
                .decodeList<SupabaseShoppingList>()
            
            val result = if (results.isNotEmpty()) {
                results.first()
            } else {
                throw Exception("認証付きリスト作成に失敗しました: レスポンスが空です")
            }
            
            // テスト後はサインアウト
            supabaseClient.auth.signOut()
            
            Result.success(result)
        } catch (e: Exception) {
            // エラーが発生した場合もサインアウトを試行
            try {
                supabaseClient.auth.signOut()
            } catch (signOutError: Exception) {
                // サインアウトエラーは無視
            }
            Result.failure(e)
        }
    }

    /**
     * 現在のユーザーセッション情報を取得
     */
    suspend fun getCurrentUserInfo(): Result<String> {
        return try {
            val session = supabaseClient.auth.currentSessionOrNull()
            val user = supabaseClient.auth.currentUserOrNull()
            
            if (session != null && user != null) {
                Result.success("認証済み: User ID = ${user.id}, Email = ${user.email}")
            } else {
                Result.success("未認証")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}