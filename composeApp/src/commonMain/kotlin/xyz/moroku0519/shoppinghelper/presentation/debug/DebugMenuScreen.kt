package xyz.moroku0519.shoppinghelper.presentation.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * デバッグメニュー画面
 * 
 * デバッグビルドでのみ表示される開発者向け機能の一覧
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugMenuScreen(
    onBackClick: () -> Unit,
    onNavigateToSupabaseTest: () -> Unit,
    onNavigateToDatabaseTest: () -> Unit = {} // 将来の拡張用
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔧 デバッグメニュー") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    titleContentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "⚠️ 開発者向け機能",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "この画面はデバッグビルドでのみ表示されます。本番環境では利用できません。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            items(debugMenuItems) { item ->
                DebugMenuItem(
                    title = item.title,
                    description = item.description,
                    icon = item.icon,
                    onClick = {
                        when (item.id) {
                            "supabase_test" -> onNavigateToSupabaseTest()
                            "database_test" -> onNavigateToDatabaseTest()
                            // 将来の機能拡張用
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DebugMenuItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "移動",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * デバッグメニューアイテムのデータクラス
 */
private data class DebugMenuItemData(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector
)

/**
 * デバッグメニューアイテムの定義
 */
private val debugMenuItems = listOf(
    DebugMenuItemData(
        id = "supabase_test",
        title = "Supabase接続テスト",
        description = "クラウドデータベースの接続と認証をテスト",
        icon = Icons.Default.Settings
    ),
    DebugMenuItemData(
        id = "database_test",
        title = "SQLDelightデータベーステスト",
        description = "ローカルデータベースの動作確認（将来実装予定）",
        icon = Icons.Default.Info
    )
    // 将来の機能拡張:
    // - API接続テスト
    // - 位置情報テスト  
    // - プッシュ通知テスト
    // - パフォーマンステスト
)