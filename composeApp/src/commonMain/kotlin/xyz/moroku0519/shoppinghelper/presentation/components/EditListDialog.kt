package xyz.moroku0519.shoppinghelper.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import xyz.moroku0519.shoppinghelper.model.ShoppingList
import xyz.moroku0519.shoppinghelper.util.currentTimeMillis

@Composable
fun EditListDialog(
    list: ShoppingList?,
    onDismiss: () -> Unit,
    onConfirm: (name: String) -> Unit
) {
    if (list != null) {
        var listName by remember(list) { mutableStateOf(list.name) }
        var showError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "リストを編集",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = listName,
                        onValueChange = {
                            listName = it
                            showError = false
                        },
                        label = { Text("リスト名") },
                        placeholder = { Text("例: 今週の買い物") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = showError && listName.isBlank(),
                        supportingText = if (showError && listName.isBlank()) {
                            { Text("リスト名は必須です", color = MaterialTheme.colorScheme.error) }
                        } else null
                    )

                    // リスト情報
                    Column {
                        Text(
                            text = "リスト情報",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ステータス: ${if (list.isActive) "アクティブ" else "アーカイブ"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "作成日: ${formatTimestamp(list.createdAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (listName.isNotBlank()) {
                            onConfirm(listName.trim())
                        } else {
                            showError = true
                        }
                    }
                ) {
                    Text("更新")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("キャンセル")
                }
            }
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val formatter = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.getDefault())
    return formatter.format(date)
}

@Preview
@Composable
private fun EditListDialogPreview() {
    MaterialTheme {
        EditListDialog(
            list = ShoppingList(
                id = "1",
                name = "今週の買い物",
                isActive = true,
                createdAt = currentTimeMillis(),
                updatedAt = currentTimeMillis()
            ),
            onDismiss = {},
            onConfirm = {}
        )
    }
}