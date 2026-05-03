package com.app.habitflow.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val FILTER_KEY = stringPreferencesKey("active_filter")

    val activeFilter: Flow<String> = context.dataStore.data
        .map { it[FILTER_KEY] ?: "ALL" }

    suspend fun saveFilter(filter: String) {
        context.dataStore.edit { it[FILTER_KEY] = filter }
    }
}

