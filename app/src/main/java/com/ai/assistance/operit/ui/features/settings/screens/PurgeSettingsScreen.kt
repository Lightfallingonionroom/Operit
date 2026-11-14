package com.ai.assistance.operit.ui.features.settings.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ai.assistance.operit.R
import com.ai.assistance.operit.data.preferences.PurgeSettings
import com.ai.assistance.operit.data.preferences.PurgeSettingsStore
import com.ai.assistance.operit.util.AppLogger
import kotlinx.coroutines.launch

/**
 * 二改破甲面板：三层提示词层的开关 + 权限状态。
 *
 * 开关立即写入 DataStore，下一轮对话生效（系统提示词在会话组装时读取）。
 */
@Composable
fun PurgeSettingsScreen(navigateBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var settings by remember { mutableStateOf(PurgeSettings()) }

    // 进入页面时读取一次；读取失败保持默认值，不崩
    androidx.compose.runtime.LaunchedEffect(Unit) {
        runCatching { settings = PurgeSettingsStore.load(context) }
            .onFailure { AppLogger.e("PurgeSettingsScreen", "读取破甲设置失败", it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        PurgeSwitchItem(
            title = stringResource(R.string.purge_item_square),
            subtitle = stringResource(R.string.purge_item_square_desc),
            checked = settings.squareEnabled,
        ) { enabled ->
            settings = settings.copy(squareEnabled = enabled)
            scope.launch { runCatching { PurgeSettingsStore.save(context, settings) } }
        }

        PurgeSwitchItem(
            title = stringResource(R.string.purge_item_layer),
            subtitle = stringResource(R.string.purge_item_layer_desc),
            checked = settings.layerEnabled,
        ) { enabled ->
            settings = settings.copy(layerEnabled = enabled)
            scope.launch { runCatching { PurgeSettingsStore.save(context, settings) } }
        }

        PurgeSwitchItem(
            title = stringResource(R.string.purge_item_redteam),
            subtitle = stringResource(R.string.purge_item_redteam_desc),
            checked = settings.redteamCardEnabled,
        ) { enabled ->
            settings = settings.copy(redteamCardEnabled = enabled)
            scope.launch { runCatching { PurgeSettingsStore.save(context, settings) } }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.purge_item_permission),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(R.string.purge_item_permission_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PurgeSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }
}
