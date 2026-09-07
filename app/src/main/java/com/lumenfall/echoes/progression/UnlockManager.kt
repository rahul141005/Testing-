package com.lumenfall.echoes.progression

import com.lumenfall.echoes.game.Rarity

data class Unlockable(
    val id: String,
    val name: String,
    val description: String,
    val cost: Int,
    val rarity: Rarity,
    val prerequisites: List<String> = emptyList(),
    val type: String // weapon, ability, biome, upgrade
)

object UnlockManager {
    val allUnlocks = listOf(
        Unlockable("thorn_spear", "Thorn Spear", "Long reach spear from Gloomroot", 40, Rarity.UNCOMMON, type="weapon"),
        Unlockable("grav_hammer", "Grav Hammer", "Crushing hammer with shockwave", 90, Rarity.RARE, listOf("thorn_spear"), "weapon"),
        Unlockable("duskwind_blades", "Duskwind Blades", "Twin blades of wind", 85, Rarity.RARE, type="weapon"),
        Unlockable("riven_axe", "Riven Axe", "Wide cleaving axe", 50, Rarity.UNCOMMON, type="weapon"),
        Unlockable("needle_unmaking", "Needle of Unmaking", "Void stitch", 180, Rarity.EPIC, listOf("grav_hammer","duskwind_blades"), "weapon"),
        Unlockable("ironcast_xbow", "Ironcast Arbalest", "Piercing crossbow", 45, Rarity.UNCOMMON, type="weapon"),
        Unlockable("emberhand", "Emberhand", "Flame gauntlet", 80, Rarity.RARE, type="weapon"),
        Unlockable("shard_daggers", "Shard Daggers", "Seeking frost shards", 160, Rarity.EPIC, listOf("emberhand"), "weapon"),

        Unlockable("frost_sigil", "Frost Sigil", "Freezing field", 40, Rarity.UNCOMMON, type="ability"),
        Unlockable("volt_chain", "Volt Chain", "Chain lightning", 75, Rarity.RARE, type="ability"),
        Unlockable("spectral_wolves", "Spectral Wolves", "Summon wolves", 150, Rarity.EPIC, listOf("volt_chain"), "ability"),
        Unlockable("aether_dome", "Aether Dome", "Reflective shield", 70, Rarity.RARE, type="ability"),

        Unlockable("biome_gloomroot", "Gloomroot Warren", "Unlock second biome", 0, Rarity.COMMON, type="biome"),
        Unlockable("biome_emberworks", "Emberworks Foundry", "Unlock third biome", 120, Rarity.RARE, listOf("biome_gloomroot"), "biome"),
        Unlockable("biome_archive", "Lumen Archive", "Unlock fourth biome", 250, Rarity.EPIC, listOf("biome_emberworks"), "biome"),
        Unlockable("biome_voidspire", "Voidspire Apex", "Unlock final biome", 500, Rarity.LEGENDARY, listOf("biome_archive"), "biome"),

        Unlockable("upgrade_health_1", "Vital Echo I", "+20 Max HP", 60, Rarity.COMMON, type="upgrade"),
        Unlockable("upgrade_health_2", "Vital Echo II", "+30 Max HP", 120, Rarity.UNCOMMON, listOf("upgrade_health_1"), "upgrade"),
        Unlockable("upgrade_starting_coins", "Scavenger's Pouch", "Start with 30 coins", 80, Rarity.UNCOMMON, type="upgrade"),
        Unlockable("upgrade_double_jump", "Aether Wings", "Unlock double jump permanently", 200, Rarity.RARE, type="upgrade")
    )

    fun getAvailable(playerSave: PlayerSave): List<Unlockable> {
        return allUnlocks.filter { unlock ->
            !isUnlocked(unlock.id, playerSave) &&
            unlock.prerequisites.all { isUnlocked(it, playerSave) } &&
            playerSave.lumenShards >= unlock.cost
        }
    }

    fun isUnlocked(id: String, save: PlayerSave): Boolean {
        return save.unlockedWeapons.contains(id) ||
               save.unlockedAbilities.contains(id) ||
               save.unlockedBiomes.contains(id.uppercase()) ||
               save.unlockedBiomes.contains(id) ||
               save.bestBiomeReached > 0 // simplified for upgrades
    }

    fun unlock(id: String, save: PlayerSave): Boolean {
        val unlock = allUnlocks.find { it.id == id } ?: return false
        if (save.lumenShards < unlock.cost) return false
        if (isUnlocked(id, save)) return false
        if (!unlock.prerequisites.all { isUnlocked(it, save) }) return false

        save.lumenShards -= unlock.cost
        when(unlock.type) {
            "weapon" -> save.unlockedWeapons.add(id)
            "ability" -> save.unlockedAbilities.add(id)
            "biome" -> save.unlockedBiomes.add(id.uppercase())
            "upgrade" -> save.unlockedBiomes.add(id) // reuse set for simplicity
        }
        return true
    }
}
