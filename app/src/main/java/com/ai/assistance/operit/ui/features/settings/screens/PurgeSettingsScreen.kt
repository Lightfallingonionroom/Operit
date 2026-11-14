package com.ai.assistance.operit.ui.features.settings.screens

import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.ai.assistance.operit.BuildConfig
import com.ai.assistance.operit.R
import com.ai.assistance.operit.data.preferences.PurgeSettings
import com.ai.assistance.operit.data.preferences.PurgeSettingsStore
import com.ai.assistance.operit.util.AppLogger
import kotlinx.coroutines.launch

/**
 * 二改破甲控制台（Purge Console）。
 *
 * 对标 dsh-purge 的 dock：标签页 + 底部操作按钮。
 *  - 状态：三层提示词开关 + 权限默认
 *  - 提示词：各层说明
 *  - 技能：内置技能与 rt 工具层
 *  - 版本：包名 / 版本 / 构建
 *  - 按钮：应用 / 还原 / 重启 / 卸载(关闭全部) / 刷新
 *
 * 所有读写都走 runCatching，设置异常不会影响应用。
 */
@Composable
fun PurgeSettingsScreen(navigateBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var settings by remember { mutableStateOf(PurgeSettings()) }
    var tab by remember { mutableIntStateOf(0) }
    var toast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        runCatching { settings = PurgeSettingsStore.load(context) }
            .onFailure { AppLogger.e("PurgeConsole", "读取破甲设置失败", it) }
    }

    fun persist(next: PurgeSettings, message: String) {
        settings = next
        scope.launch {
            runCatching { PurgeSettingsStore.save(context, next) }
                .onFailure { AppLogger.e("PurgeConsole", "保存破甲设置失败", it) }
            toast = message
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.purge_console_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.purge_console_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        val tabs = listOf(
            stringResource(R.string.purge_tab_status),
            stringResource(R.string.purge_tab_prompt),
            stringResource(R.string.purge_tab_skills),
            stringResource(R.string.purge_tab_version),
        )
        TabRow(selectedTabIndex = tab, modifier = Modifier.horizontalScroll(rememberScrollState())) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = tab == index,
                    onClick = { tab = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (tab) {
            0 -> StatusTab(settings) { next -> persist(next, context.getString(R.string.purge_action_applied)) }
            1 -> PromptTab(settings)
            2 -> SkillsTab()
            else -> VersionTab()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 底部操作按钮：应用 / 还原 / 重启 / 卸载 / 刷新
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { persist(settings, context.getString(R.string.purge_action_applied)) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.width(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.purge_action_apply), maxLines = 1)
            }
            OutlinedButton(onClick = {
                persist(PurgeSettings(), context.getString(R.string.purge_action_restored))
            }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.width(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.purge_action_restore), maxLines = 1)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = { restartApp(context) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.width(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.purge_action_restart), maxLines = 1)
            }
            OutlinedButton(onClick = {
                persist(
                    PurgeSettings(squareEnabled = false, layerEnabled = false, redteamCardEnabled = false),
                    context.getString(R.string.purge_action_uninstalled)
                )
            }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.width(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.purge_action_uninstall), maxLines = 1)
            }
            OutlinedButton(onClick = {
                runCatching { settings = PurgeSettingsStore.load(context) }
                toast = null
            }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.width(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.purge_action_refresh), maxLines = 1)
            }
        }

        toast?.let { message ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StatusTab(settings: PurgeSettings, onChange: (PurgeSettings) -> Unit) {
    ConsoleCard(title = stringResource(R.string.purge_status_prompt_layer)) {
        SwitchRow(
            title = stringResource(R.string.purge_item_square),
            subtitle = stringResource(R.string.purge_item_square_desc),
            checked = settings.squareEnabled,
            onCheckedChange = { enabled -> onChange(settings.copy(squareEnabled = enabled)) }
        )
        SwitchRow(
            title = stringResource(R.string.purge_item_layer),
            subtitle = stringResource(R.string.purge_item_layer_desc),
            checked = settings.layerEnabled,
            onCheckedChange = { enabled -> onChange(settings.copy(layerEnabled = enabled)) }
        )
        SwitchRow(
            title = stringResource(R.string.purge_item_redteam),
            subtitle = stringResource(R.string.purge_item_redteam_desc),
            checked = settings.redteamCardEnabled,
            onCheckedChange = { enabled -> onChange(settings.copy(redteamCardEnabled = enabled)) }
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    ConsoleCard(title = stringResource(R.string.purge_status_permission)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.purge_status_permission_value),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun PromptTab(settings: PurgeSettings) {
    ConsoleCard(title = stringResource(R.string.purge_item_square)) {
        Text(
            text = if (settings.squareEnabled) stringResource(R.string.purge_status_on) else stringResource(R.string.purge_status_off),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = stringResource(R.string.purge_item_square_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    ConsoleCard(title = stringResource(R.string.purge_item_layer)) {
        Text(
            text = if (settings.layerEnabled) stringResource(R.string.purge_status_on) else stringResource(R.string.purge_status_off),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = stringResource(R.string.purge_item_layer_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    ConsoleCard(title = stringResource(R.string.purge_item_redteam)) {
        Text(
            text = if (settings.redteamCardEnabled) stringResource(R.string.purge_status_on) else stringResource(R.string.purge_status_off),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = stringResource(R.string.purge_item_redteam_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SkillsTab() {
    ConsoleCard(title = stringResource(R.string.purge_skills_installed)) {
        Text(text = "24", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            text = "dsh-purge-redteam / active-scan / nuclei-scan / ...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    ConsoleCard(title = stringResource(R.string.purge_rt_path)) {
        Text(
            text = "/sdcard/Download/Operit/redteam/bin/rt",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun VersionTab() {
    ConsoleCard(title = stringResource(R.string.purge_tab_version)) {
        Text(text = "Operit ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium)
        Text(
            text = BuildConfig.APPLICATION_ID,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ConsoleCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun restartApp(context: android.content.Context) {
    runCatching {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
        Runtime.getRuntime().exit(0)
    }.onFailure { AppLogger.e("PurgeConsole", "重启失败", it) }
}
