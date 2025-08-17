package xyz.moroku0519.shoppinghelper

import App
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.android.ext.android.inject
import xyz.moroku0519.shoppinghelper.database.ShoppingDatabase
import xyz.moroku0519.shoppinghelper.debug.DatabaseDebugHelper

class MainActivity : ComponentActivity() {
    
    private val database: ShoppingDatabase by inject()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // デバッグビルドでのみデータベース情報を出力
        if (BuildConfig.DEBUG) {
            setupDatabaseDebugging()
        }

        setContent {
            App()
        }
    }
    
    private fun setupDatabaseDebugging() {
        val debugHelper = DatabaseDebugHelper(this, database)
        
        Log.d("MainActivity", "=== SQLDelight Database Debug Info ===")
        Log.d("MainActivity", "Database path: ${getDatabasePath("shopping.db").absolutePath}")
        
        // データベース統計を出力
        debugHelper.printDatabaseStats()
        
        // 基本クエリをテスト
        debugHelper.testBasicQueries()
        
        // データベース整合性チェック
        debugHelper.validateDatabaseIntegrity()
        
        // データベースファイルをエクスポート
        val exportPath = debugHelper.exportDatabaseFile()
        if (exportPath != null) {
            Log.d("MainActivity", "Database exported for inspection: $exportPath")
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
