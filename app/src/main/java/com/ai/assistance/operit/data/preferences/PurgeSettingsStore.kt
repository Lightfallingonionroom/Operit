package com.ai.assistance.operit.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.ai.assistance.operit.util.AppLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.purgeSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "purge_settings")

/**
 * 二改破甲开关。默认全部开启（与 dsh-purge 装完即生效的行为一致）。
 *
 *  - squareEnabled      正方形框架（zfx-emperor）
 *  - layerEnabled       dsh-purge 默认注入稿
 *  - redteamCardEnabled 红队角色卡（命中意图时追加）
 */
data class PurgeSettings(
    val squareEnabled: Boolean = true,
    val layerEnabled: Boolean = true,
    val redteamCardEnabled: Boolean = true,
)

object PurgeSettingsStore {

    private const val TAG = "PurgeSettingsStore"

    private val KEY_SQUARE = booleanPreferencesKey("square_enabled")
    private val KEY_LAYER = booleanPreferencesKey("layer_enabled")
    private val KEY_REDTEAM = booleanPreferencesKey("redteam_card_enabled")

    fun flow(context: Context): Flow<PurgeSettings> =
        context.purgeSettingsDataStore.data
            .catch { error ->
                if (error is IOException) {
                    AppLogger.w(TAG, "读取破甲设置失败，用默认值", error)
                    emit(emptyPreferences())
                } else {
                    throw error
                }
            }
            .map { preferences ->
                PurgeSettings(
                    squareEnabled = preferences[KEY_SQUARE] ?: true,
                    layerEnabled = preferences[KEY_LAYER] ?: true,
                    redteamCardEnabled = preferences[KEY_REDTEAM] ?: true,
                )
            }

    suspend fun load(context: Context): PurgeSettings = flow(context).first()

    suspend fun save(context: Context, settings: PurgeSettings) {
        context.purgeSettingsDataStore.edit { preferences ->
            preferences[KEY_SQUARE] = settings.squareEnabled
            preferences[KEY_LAYER] = settings.layerEnabled
            preferences[KEY_REDTEAM] = settings.redteamCardEnabled
        }
    }
}
