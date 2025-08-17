package xyz.moroku0519.shoppinghelper.presentation.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import xyz.moroku0519.shoppinghelper.BuildConfig
import xyz.moroku0519.shoppinghelper.data.test.SupabaseConnectionTest
import xyz.moroku0519.shoppinghelper.data.test.TestResult

/**
 * Supabase接続テスト用のデバッグ画面
 * 
 * 開発時にSupabaseプロジェクトとの接続を簡単にテストできるUI。
 * 本番ビルドからは除外することを推奨。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupabaseTestScreen(
    onBackClick: () -> Unit = {}
) {
    var testResults by remember { mutableStateOf<List<TestResult>>(emptyList()) }
    var isTestRunning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ヘッダー
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Supabase接続テスト",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            TextButton(onClick = onBackClick) {
                Text("戻る")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 設定情報表示
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "現在の設定",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "URL: ${BuildConfig.SUPABASE_URL}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Key: ${BuildConfig.SUPABASE_PUBLISHABLE_KEY.take(20)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // テスト実行ボタン
        Button(
            onClick = {
                scope.launch {
                    isTestRunning = true
                    testResults = emptyList()
                    
                    val test = SupabaseConnectionTest(
                        supabaseUrl = BuildConfig.SUPABASE_URL,
                        supabasePublishableKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
                    )
                    
                    testResults = test.runAllTests()
                    isTestRunning = false
                }
            },
            enabled = !isTestRunning,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isTestRunning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("テスト実行中...")
            } else {
                Text("接続テスト実行")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // テスト結果表示
        if (testResults.isNotEmpty()) {
            Text(
                text = "テスト結果",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(testResults) { result ->
                    TestResultCard(result = result)
                }
            }
        }
    }
}

@Composable
fun TestResultCard(result: TestResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (result) {
                is TestResult.Success -> MaterialTheme.colorScheme.primaryContainer
                is TestResult.Error -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (result) {
                    is TestResult.Success -> "✅"
                    is TestResult.Error -> "❌"
                },
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = when (result) {
                    is TestResult.Success -> result.message
                    is TestResult.Error -> result.message
                },
                style = MaterialTheme.typography.bodyMedium,
                color = when (result) {
                    is TestResult.Success -> MaterialTheme.colorScheme.onPrimaryContainer
                    is TestResult.Error -> MaterialTheme.colorScheme.onErrorContainer
                }
            )
        }
    }
}

/**
 * デバッグビルドでのみ表示されるSupabaseテストボタン
 */
@Composable
fun SupabaseTestButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // デバッグビルドの場合のみ表示
    if (BuildConfig.DEBUG) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
        ) {
            Text("🧪 Supabaseテスト")
        }
    }
}