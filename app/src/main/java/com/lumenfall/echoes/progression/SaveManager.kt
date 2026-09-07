package com.lumenfall.echoes.progression

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class PlayerSave(
    var totalRuns: Int = 0,
    var totalDeaths: Int = 0,
    var totalCoinsEarned: Int = 0,
    var lumenShards: Int = 0,
    var unlockedWeapons: MutableSet<String> = mutableSetOf("dawnblade", "aetherbow"),
    var unlockedAbilities: MutableSet<String> = mutableSetOf("ember_trap", "phase_dash"),
    var unlockedBiomes: MutableSet<String> = mutableSetOf("ASHEN_RAMPARTS"),
    var bestBiomeReached: Int = 0,
    var hasSeenTutorial: Boolean = false,
    var settings: GameSettings = GameSettings()
)

data class GameSettings(
    var musicVolume: Float = 0.8f,
    var sfxVolume: Float = 0.9f,
    var screenShake: Boolean = true,
    var damageNumbers: Boolean = true,
    var vibration: Boolean = true,
    var leftHanded: Boolean = false,
    var controlScale: Float = 1f,
    var controlOpacity: Float = 0.7f,
    var reducedEffects: Boolean = false
)

data class RunSave(
    var seed: Long = 0L,
    var currentBiomeIndex: Int = 0,
    var currentRoomIndex: Int = 0,
    var playerHp: Int = 100,
    var playerMaxHp: Int = 100,
    var coins: Int = 0,
    var primaryWeaponId: String = "dawnblade",
    var secondaryWeaponId: String? = null,
    var rangedWeaponId: String = "aetherbow",
    var ability1Id: String = "ember_trap",
    var ability2Id: String = "phase_dash",
    var lumenShards: Int = 0,
    var isActive: Boolean = false
)

class SaveManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("lumenfall_save", Context.MODE_PRIVATE)

    fun loadPlayerSave(): PlayerSave {
        val jsonStr = prefs.getString("player_save", null) ?: return PlayerSave()
        return try {
            val json = JSONObject(jsonStr)
            PlayerSave(
                totalRuns = json.optInt("totalRuns", 0),
                totalDeaths = json.optInt("totalDeaths", 0),
                totalCoinsEarned = json.optInt("totalCoinsEarned", 0),
                lumenShards = json.optInt("lumenShards", 0),
                unlockedWeapons = json.optJSONArray("unlockedWeapons")?.toStringSet() ?: mutableSetOf("dawnblade", "aetherbow"),
                unlockedAbilities = json.optJSONArray("unlockedAbilities")?.toStringSet() ?: mutableSetOf("ember_trap", "phase_dash"),
                unlockedBiomes = json.optJSONArray("unlockedBiomes")?.toStringSet() ?: mutableSetOf("ASHEN_RAMPARTS"),
                bestBiomeReached = json.optInt("bestBiomeReached", 0),
                hasSeenTutorial = json.optBoolean("hasSeenTutorial", false),
                settings = loadSettings(json.optJSONObject("settings"))
            )
        } catch (e: Exception) {
            PlayerSave()
        }
    }

    fun savePlayerSave(save: PlayerSave) {
        val json = JSONObject().apply {
            put("totalRuns", save.totalRuns)
            put("totalDeaths", save.totalDeaths)
            put("totalCoinsEarned", save.totalCoinsEarned)
            put("lumenShards", save.lumenShards)
            put("unlockedWeapons", JSONArray(save.unlockedWeapons.toList()))
            put("unlockedAbilities", JSONArray(save.unlockedAbilities.toList()))
            put("unlockedBiomes", JSONArray(save.unlockedBiomes.toList()))
            put("bestBiomeReached", save.bestBiomeReached)
            put("hasSeenTutorial", save.hasSeenTutorial)
            put("settings", JSONObject().apply {
                put("musicVolume", save.settings.musicVolume)
                put("sfxVolume", save.settings.sfxVolume)
                put("screenShake", save.settings.screenShake)
                put("damageNumbers", save.settings.damageNumbers)
                put("vibration", save.settings.vibration)
                put("leftHanded", save.settings.leftHanded)
                put("controlScale", save.settings.controlScale)
                put("controlOpacity", save.settings.controlOpacity)
                put("reducedEffects", save.settings.reducedEffects)
            })
        }
        prefs.edit().putString("player_save", json.toString()).apply()
    }

    private fun loadSettings(obj: JSONObject?): GameSettings {
        if (obj == null) return GameSettings()
        return GameSettings(
            musicVolume = obj.optDouble("musicVolume", 0.8).toFloat(),
            sfxVolume = obj.optDouble("sfxVolume", 0.9).toFloat(),
            screenShake = obj.optBoolean("screenShake", true),
            damageNumbers = obj.optBoolean("damageNumbers", true),
            vibration = obj.optBoolean("vibration", true),
            leftHanded = obj.optBoolean("leftHanded", false),
            controlScale = obj.optDouble("controlScale", 1.0).toFloat(),
            controlOpacity = obj.optDouble("controlOpacity", 0.7).toFloat(),
            reducedEffects = obj.optBoolean("reducedEffects", false)
        )
    }

    fun loadRunSave(): RunSave? {
        val jsonStr = prefs.getString("run_save", null) ?: return null
        return try {
            val json = JSONObject(jsonStr)
            RunSave(
                seed = json.optLong("seed", System.currentTimeMillis()),
                currentBiomeIndex = json.optInt("currentBiomeIndex", 0),
                currentRoomIndex = json.optInt("currentRoomIndex", 0),
                playerHp = json.optInt("playerHp", 100),
                playerMaxHp = json.optInt("playerMaxHp", 100),
                coins = json.optInt("coins", 0),
                primaryWeaponId = json.optString("primaryWeaponId", "dawnblade"),
                secondaryWeaponId = json.optString("secondaryWeaponId", null),
                rangedWeaponId = json.optString("rangedWeaponId", "aetherbow"),
                ability1Id = json.optString("ability1Id", "ember_trap"),
                ability2Id = json.optString("ability2Id", "phase_dash"),
                lumenShards = json.optInt("lumenShards", 0),
                isActive = json.optBoolean("isActive", false)
            )
        } catch (e: Exception) { null }
    }

    fun saveRunSave(run: RunSave) {
        val json = JSONObject().apply {
            put("seed", run.seed)
            put("currentBiomeIndex", run.currentBiomeIndex)
            put("currentRoomIndex", run.currentRoomIndex)
            put("playerHp", run.playerHp)
            put("playerMaxHp", run.playerMaxHp)
            put("coins", run.coins)
            put("primaryWeaponId", run.primaryWeaponId)
            put("secondaryWeaponId", run.secondaryWeaponId)
            put("rangedWeaponId", run.rangedWeaponId)
            put("ability1Id", run.ability1Id)
            put("ability2Id", run.ability2Id)
            put("lumenShards", run.lumenShards)
            put("isActive", run.isActive)
        }
        prefs.edit().putString("run_save", json.toString()).apply()
    }

    fun clearRunSave() {
        prefs.edit().remove("run_save").apply()
    }

    private fun JSONArray.toStringSet(): MutableSet<String> {
        val set = mutableSetOf<String>()
        for (i in 0 until length()) set.add(optString(i))
        return set
    }
}
