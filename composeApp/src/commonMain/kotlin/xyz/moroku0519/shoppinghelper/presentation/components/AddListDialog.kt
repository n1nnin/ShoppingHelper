package xyz.moroku0519.shoppinghelper.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AddListDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String) -> Unit
) {
    if (isVisible) {
        var listName by remember { mutableStateOf("") }
        var showError by remember { mutableStateOf(false) }

        // ダイアログが開くたびに状態をリセット
        LaunchedEffect(isVisible) {
            if (isVisible) {
                listName = ""
                showError = false
            }
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "新しいリストを作成",
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

                    Text(
                        text = "ヒント: 用途や期間でリストを分けると管理しやすくなります",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (listName.isNotBlank()) {
                            onConfirm(listName.trim())
                            onDismiss()
                        } else {
                            showError = true
                        }
                    }
                ) {
                    Text("作成")
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

@Preview
@Composable
private fun AddListDialogPreview() {
    MaterialTheme {
        AddListDialog(
            isVisible = true,
            onDismiss = {},
            onConfirm = {}
        )
    }
}