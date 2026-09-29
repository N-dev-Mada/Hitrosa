package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "carnet_pro_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val KEY_SHOP_NAME = stringPreferencesKey("key_shop_name")
        val KEY_CURRENCY = stringPreferencesKey("key_currency")
        val KEY_SHOP_PHONE = stringPreferencesKey("key_shop_phone")
        val KEY_SHOP_LOGO_URI = stringPreferencesKey("key_shop_logo_uri")
        val KEY_SECURITY_PIN_ENABLED = booleanPreferencesKey("key_security_pin_enabled")
        val KEY_SECURITY_PIN = stringPreferencesKey("key_security_pin")
        val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("key_biometric_enabled")
        val KEY_INITIAL_CLEAN_DONE = booleanPreferencesKey("key_initial_clean_done_v2")

        const val DEFAULT_SHOP_NAME = "Boutique Centrale"
        const val DEFAULT_CURRENCY = "Ar"
    }

    private val dataStore = context.dataStore

    val shopName: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_SHOP_NAME] ?: DEFAULT_SHOP_NAME
        }

    val currency: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_CURRENCY] ?: DEFAULT_CURRENCY
        }

    val shopPhone: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_SHOP_PHONE] ?: ""
        }

    val shopLogoUri: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_SHOP_LOGO_URI]
        }

    val isSecurityPinEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_SECURITY_PIN_ENABLED] ?: false
        }

    val securityPin: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_SECURITY_PIN] ?: ""
        }

    val isBiometricEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_BIOMETRIC_ENABLED] ?: true
        }

    val isInitialCleanDone: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_INITIAL_CLEAN_DONE] ?: false
        }

    suspend fun markInitialCleanDone() {
        dataStore.edit { preferences ->
            preferences[KEY_INITIAL_CLEAN_DONE] = true
        }
    }

    suspend fun updateShopSettings(name: String, currency: String, phone: String) {
        dataStore.edit { preferences ->
            preferences[KEY_SHOP_NAME] = name.ifBlank { DEFAULT_SHOP_NAME }
            preferences[KEY_CURRENCY] = currency.ifBlank { DEFAULT_CURRENCY }
            preferences[KEY_SHOP_PHONE] = phone.trim()
        }
    }

    suspend fun updateShopLogo(logoUri: String?) {
        dataStore.edit { preferences ->
            if (logoUri != null) {
                preferences[KEY_SHOP_LOGO_URI] = logoUri
            } else {
                preferences.remove(KEY_SHOP_LOGO_URI)
            }
        }
    }

    suspend fun setSecurityPin(pin: String) {
        dataStore.edit { preferences ->
            preferences[KEY_SECURITY_PIN] = pin
            preferences[KEY_SECURITY_PIN_ENABLED] = pin.isNotBlank()
        }
    }

    suspend fun setSecurityPinEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SECURITY_PIN_ENABLED] = enabled
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_BIOMETRIC_ENABLED] = enabled
        }
    }
}
