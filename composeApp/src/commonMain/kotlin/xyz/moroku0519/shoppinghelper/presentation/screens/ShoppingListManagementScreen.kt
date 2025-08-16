package xyz.moroku0519.shoppinghelper.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import xyz.moroku0519.shoppinghelper.model.ShoppingList
import xyz.moroku0519.shoppinghelper.presentation.components.AddListDialog
import xyz.moroku0519.shoppinghelper.presentation.components.ShoppingListCard
import xyz.moroku0519.shoppinghelper.presentation.viewmodel.ShoppingListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListManagementScreen(
    onBackClick: () -> Unit,
    onListSelected: (String) -> Unit
) {
    val viewModel: ShoppingListViewModel = koinInject()
    
    // ViewModelからリストデータを取得
    val allLists by viewModel.allLists.collectAsState()
    val activeLists by viewModel.activeLists.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var listToDelete by remember { mutableStateOf<ShoppingList?>(null) }
    var showArchivedLists by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("買い物リスト管理")
                        Text(
                            text = "${activeLists.size}件のアクティブリスト",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { showArchivedLists = !showArchivedLists }
                    ) {
                        Text(if (showArchivedLists) "アクティブのみ" else "アーカイブも表示")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "リスト追加")
            }
        }
    ) { paddingValues ->
        val listsToShow = if (showArchivedLists) allLists else activeLists
        
        if (listsToShow.isEmpty()) {
            EmptyListsState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                showingArchived = showArchivedLists
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = listsToShow,
                    key = { it.id }
                ) { list ->
                    ShoppingListCard(
                        list = list,
                        itemCount = 0, // TODO: 実際のアイテム数を取得
                        completedItemCount = 0, // TODO: 実際の完了アイテム数を取得
                        onListClick = { onListSelected(list.id) },
                        onEditClick = { /* TODO: 編集機能 */ },
                        onDeleteClick = { listToDelete = list },
                        onArchiveClick = { 
                            viewModel.updateList(list.copy(isActive = !list.isActive))
                        }
                    )
                }
            }
        }

        // リスト追加ダイアログ
        AddListDialog(
            isVisible = showAddDialog,
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                viewModel.createList(name)
                showAddDialog = false
            }
        )

        // 削除確認ダイアログ
        listToDelete?.let { list ->
            AlertDialog(
                onDismissRequest = { listToDelete = null },
                title = { Text("リストを削除") },
                text = {
                    Text(
                        "「${list.name}」を削除しますか？\n" +
                        "このリストに含まれるアイテムもすべて削除されます。"
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteList(list.id)
                            listToDelete = null
                        }
                    ) {
                        Text("削除", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { listToDelete = null }) {
                        Text("キャンセル")
                    }
                }
            )
        }
    }
}

@Composable
private fun EmptyListsState(
    modifier: Modifier = Modifier,
    showingArchived: Boolean = false
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (showingArchived) {
                    "リストがありません"
                } else {
                    "アクティブなリストがありません"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (showingArchived) {
                    "右下の+ボタンで新しいリストを作成してください"
                } else {
                    "アーカイブされたリストを表示するか、新しいリストを作成してください"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}