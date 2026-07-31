package com.ubuterm.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "ubuterm_prefs")

object PreferencesStore {
    private val KEY_PROFILE = stringPreferencesKey("distro_profile")
    private val KEY_INSTALLED = booleanPreferencesKey("distro_installed")
    private val KEY_DEFAULT_SHELL = stringPreferencesKey("default_shell")

    val installed: Flow<Boolean> = dataStore.data.map { it[KEY_INSTALLED] ?: false }
    val profile: Flow<String> = dataStore.data.map { it[KEY_PROFILE] ?: "ubuntu-minimal" }

    suspend fun markInstalled(profileName: String) {
        dataStore.edit {
            it[KEY_INSTALLED] = true
            it[KEY_PROFILE] = profileName
        }
    }

    suspend fun markRemoved() {
        dataStore.edit {
            it[KEY_INSTALLED] = false
        }
    }
}
