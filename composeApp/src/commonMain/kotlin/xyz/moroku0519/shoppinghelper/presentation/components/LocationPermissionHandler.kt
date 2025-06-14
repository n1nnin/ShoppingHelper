package xyz.moroku0519.shoppinghelper.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

// commonMainでは権限の状態を管理するためのインターフェース
interface LocationPermissionState {
    val isGranted: Boolean
    val shouldShowRationale: Boolean
    fun requestPermission()
}

@Composable
fun LocationPermissionHandler(
    permissionState: LocationPermissionState?,
    onPermissionGranted: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        permissionState == null -> {
            // 権限管理が利用できない場合（Preview等）
            onPermissionGranted()
        }

        permissionState.isGranted -> {
            // 権限が付与されている場合
            onPermissionGranted()
        }

        permissionState.shouldShowRationale -> {
            // 権限の説明が必要な場合
            LocationPermissionRationale(
                onRequestPermission = { permissionState.requestPermission() },
                modifier = modifier
            )
        }

        else -> {
            // 初回権限要求
            LocationPermissionRequest(
                onRequestPermission = { permissionState.requestPermission() },
                modifier = modifier
            )
        }
    }
}

@Composable
private fun LocationPermissionRequest(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "位置情報の使用許可",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = "お店に近づいたときに通知を送るため、位置情報の使用を許可してください。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("位置情報を許可")
                }
            }
        }
    }
}

@Composable
private fun LocationPermissionRationale(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )

                Text(
                    text = "位置情報が必要です",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = "買い物リマインダー機能を使用するには、位置情報の許可が必要です。設定から位置情報を有効にしてください。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRequestPermission,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("再度許可を求める")
                    }

                    OutlinedButton(
                        onClick = { /* TODO: 設定画面を開く */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("設定画面を開く")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun LocationPermissionRequestPreview() {
    MaterialTheme {
        LocationPermissionRequest(
            onRequestPermission = {}
        )
    }
}

@Preview
@Composable
private fun LocationPermissionRationalePreview() {
    MaterialTheme {
        LocationPermissionRationale(
            onRequestPermission = {}
        )
    }
}

@Preview
@Composable
private fun LocationPermissionHandlerPreview() {
    MaterialTheme {
        LocationPermissionHandler(
            permissionState = object : LocationPermissionState {
                override val isGranted: Boolean = false
                override val shouldShowRationale: Boolean = false
                override fun requestPermission() {}
            },
            onPermissionGranted = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("権限が許可されました！")
                }
            }
        )
    }
}