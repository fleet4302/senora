package com.example.data.settings

import android.content.Context
import android.content.SharedPreferences

enum class PreferredFormat(val label: String) {
    LOSSLESS("Lossless (FLAC / 24-bit)"),
    HIGH_QUALITY_MP3("High Quality MP3 (320 kbps)"),
    ANY("Any Available Quality")
}

class UserSettings(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sonora_settings", Context.MODE_PRIVATE)

    var soulseekUsername: String
        get() = prefs.getString("slsk_user", "sonora_listener") ?: "sonora_listener"
        set(value) = prefs.edit().putString("slsk_user", value).apply()

    var soulseekServer: String
        get() = prefs.getString("slsk_server", "server.slsknet.org:2242") ?: "server.slsknet.org:2242"
        set(value) = prefs.edit().putString("slsk_server", value).apply()

    var listenPort: Int
        get() = prefs.getInt("slsk_port", 2234)
        set(value) = prefs.edit().putInt("slsk_port", value).apply()

    var upnpEnabled: Boolean
        get() = prefs.getBoolean("slsk_upnp", true)
        set(value) = prefs.edit().putBoolean("slsk_upnp", value).apply()

    var preferredFormat: PreferredFormat
        get() {
            val name = prefs.getString("slsk_pref_format", PreferredFormat.LOSSLESS.name)
            return try { PreferredFormat.valueOf(name!!) } catch (_: Exception) { PreferredFormat.LOSSLESS }
        }
        set(value) = prefs.edit().putString("slsk_pref_format", value.name).apply()

    var cacheCapGb: Int
        get() = prefs.getInt("cache_cap_gb", 5)
        set(value) = prefs.edit().putInt("cache_cap_gb", value).apply()

    var sharingEnabled: Boolean
        get() = prefs.getBoolean("sharing_enabled", true)
        set(value) = prefs.edit().putBoolean("sharing_enabled", value).apply()

    var providerMode: String
        get() = prefs.getString("provider_mode", "Simulated Swarm") ?: "Simulated Swarm"
        set(value) = prefs.edit().putString("provider_mode", value).apply()
}
