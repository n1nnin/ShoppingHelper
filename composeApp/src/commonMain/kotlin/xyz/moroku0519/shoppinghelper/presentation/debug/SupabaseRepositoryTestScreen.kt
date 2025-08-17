package xyz.moroku0519.shoppinghelper.presentation.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import xyz.moroku0519.shoppinghelper.BuildConfig
import xyz.moroku0519.shoppinghelper.data.test.SupabaseRepositoryTestHelper
import xyz.moroku0519.shoppinghelper.data.test.TestResult
import xyz.moroku0519.shoppinghelper.presentation.debug.TestResultCard

/**
 * SupabaseRepositoryのテスト画面
 * 
 * データベースのCRUD操作とRLS権限をテスト
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupabaseRepositoryTestScreen(
    onBackClick: () -> Unit
) {
    var testResults by remember { mutableStateOf<List<TestResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedTest by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🧪 Repository CRUD テスト") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🗄️ Supabase Repository テスト",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "データベースのCRUD操作、Row Level Security、権限管理をテスト",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 全テスト実行ボタン
                    FilledTonalButton(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                selectedTest = null
                                try {
                                    testResults = SupabaseRepositoryTestHelper.runTestsWithBuildConfig(
                                        BuildConfig.SUPABASE_URL,
                                        BuildConfig.SUPABASE_PUBLISHABLE_KEY
                                    )
                                } catch (e: Exception) {
                                    testResults = listOf(
                                        TestResult.Error("テスト実行エラー: ${e.message}")
                                    )
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("全テスト実行")
                    }

                    // クリアボタン
                    OutlinedButton(
                        onClick = {
                            testResults = emptyList()
                            selectedTest = null
                        },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                    }
                }
            }

            // 個別テストボタン
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "個別テスト",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val individualTests = listOf(
                            "connection" to "接続テスト",
                            "table_structure" to "テーブル構造",
                            "authentication" to "認証テスト",
                            "crud" to "CRUD操作",
                            "shopping_list" to "買い物リスト（認証付き）",
                            "permissions" to "権限テスト"
                        )

                        individualTests.forEach { (testId, testName) ->
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        isLoading = true
                                        selectedTest = testId
                                        try {
                                            val result = SupabaseRepositoryTestHelper.runSpecificTest(
                                                BuildConfig.SUPABASE_URL,
                                                BuildConfig.SUPABASE_PUBLISHABLE_KEY,
                                                testId
                                            )
                                            testResults = listOf(result)
                                        } catch (e: Exception) {
                                            testResults = listOf(
                                                TestResult.Error("$testName エラー: ${e.message}")
                                            )
                                        } finally {
                                            isLoading = false
                                            selectedTest = null
                                        }
                                    }
                                },
                                enabled = !isLoading,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("$testName テスト")
                                if (isLoading && selectedTest == testId) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // テスト結果表示
            if (testResults.isNotEmpty()) {
                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "📊 テスト結果",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            val successCount = testResults.count { it.isSuccess() }
                            val totalCount = testResults.size

                            Text(
                                text = "成功: $successCount / $totalCount テスト",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (successCount == totalCount) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                        }
                    }
                }

                items(testResults) { result ->
                    TestResultCard(result = result)
                }
            }
        }
    }
}

