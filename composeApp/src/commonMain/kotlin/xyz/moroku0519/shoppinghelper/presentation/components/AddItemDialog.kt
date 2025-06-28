package xyz.moroku0519.shoppinghelper.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import xyz.moroku0519.shoppinghelper.model.Priority
import xyz.moroku0519.shoppinghelper.presentation.model.getDisplayName

@Composable
fun AddItemDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, shopName: String?, priority: Priority) -> Unit
) {
    if (isVisible) {
        var itemName by remember { mutableStateOf("") }
        var shopName by remember { mutableStateOf("") }
        var selectedPriority by remember { mutableStateOf(Priority.NORMAL) }
        var showError by remember { mutableStateOf(false) }

        // ダイアログが開くたびに状態をリセット
        LaunchedEffect(isVisible) {
            if (isVisible) {
                itemName = ""
                shopName = ""
                selectedPriority = Priority.NORMAL
                showError = false
            }
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text("アイテムを追加")
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 商品名入力
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = {
                            itemName = it
                            showError = false  // 入力時にエラーを消す
                        },
                        label = { Text("商品名") },
                        placeholder = { Text("例: 牛乳") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = showError && itemName.isBlank(),
                        supportingText = if (showError && itemName.isBlank()) {
                            { Text("商品名は必須です", color = MaterialTheme.colorScheme.error) }
                        } else null
                    )

                    // お店名入力（オプション）
                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text("お店名（オプション）") },
                        placeholder = { Text("例: スーパーマーケット") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // 優先度選択
                    PrioritySelector(
                        selectedPriority = selectedPriority,
                        onPrioritySelected = { selectedPriority = it }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (itemName.isNotBlank()) {
                            onConfirm(
                                itemName.trim(),
                                shopName.trim().takeIf { it.isNotEmpty() },
                                selectedPriority
                            )
                            onDismiss()
                        } else {
                            showError = true
                        }
                    }
                ) {
                    Text("追加")
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

@Composable
private fun PrioritySelector(
    selectedPriority: Priority,
    onPrioritySelected: (Priority) -> Unit
) {
    Column {
        Text(
            text = "優先度",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Priority.values().forEach { priority ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (selectedPriority == priority),
                        onClick = { onPrioritySelected(priority) }
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedPriority == priority),
                    onClick = { onPrioritySelected(priority) }
                )
                Spacer(modifier = Modifier.width(8.dp))

                // 優先度カラーインジケーター
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = priority.color,
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = priority.getDisplayName(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview
@Composable
private fun AddItemDialogPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            AddItemDialog(
                isVisible = true,
                onDismiss = {},
                onConfirm = { _, _, _ -> }
            )
        }
    }
}

@Preview
@Composable
private fun PrioritySelectorPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            PrioritySelector(
                selectedPriority = Priority.HIGH,
                onPrioritySelected = {}
            )
        }
    }
}