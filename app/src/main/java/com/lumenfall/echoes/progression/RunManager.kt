package com.lumenfall.echoes.progression

import com.lumenfall.echoes.combat.Ability
import com.lumenfall.echoes.combat.AbilityRegistry
import com.lumenfall.echoes.combat.Weapon
import com.lumenfall.echoes.combat.WeaponRegistry
import com.lumenfall.echoes.utils.SeededRandom
import com.lumenfall.echoes.world.BiomeRegistry
import com.lumenfall.echoes.world.WorldGenerator

class RunManager(
    private val playerSave: PlayerSave,
    seed: Long = System.currentTimeMillis()
) {
    var currentSeed = seed
    val rng = SeededRandom(seed)
    val worldGen = WorldGenerator(rng)
    var fullRun = worldGen.generateFullRun(seed)
    var currentBiomeIndex = 0
    var currentRoomIndex = 0
    var coins = 0
    var lumenShards = 0

    var primaryWeapon: Weapon
    var rangedWeapon: Weapon
    var ability1: Ability
    var ability2: Ability

    var runStats = RunStats()

    init {
        // pick starting loadout from unlocked
        val unlockedWeapons = playerSave.unlockedWeapons.mapNotNull { WeaponRegistry.getById(it) }
        primaryWeapon = if (unlockedWeapons.isNotEmpty()) {
            unlockedWeapons[rng.nextInt(unlockedWeapons.size)].let {
                WeaponRegistry.getById(it.id) ?: WeaponRegistry.getById("dawnblade")!!
            }
        } else WeaponRegistry.getById("dawnblade")!!

        rangedWeapon = WeaponRegistry.getById("aetherbow")!!

        val unlockedAbilities = playerSave.unlockedAbilities.mapNotNull { id ->
            AbilityRegistry.getAll().find { it.id == id }
        }
        ability1 = if (unlockedAbilities.isNotEmpty()) AbilityRegistry.getRandom(rng) else AbilityRegistry.getAll().find { it.id=="ember_trap"}!!
        ability2 = AbilityRegistry.getAll().find { it.id=="phase_dash"}!!

        // ensure fullRun valid
        fullRun.forEach { run ->
            worldGen.validateRun(run)
        }
    }

    fun getCurrentBiomeRun() = fullRun.getOrNull(currentBiomeIndex)
    fun getCurrentRoom() = getCurrentBiomeRun()?.rooms?.getOrNull(currentRoomIndex)

    fun advanceRoom(): Boolean {
        val biomeRun = getCurrentBiomeRun() ?: return false
        if (currentRoomIndex < biomeRun.rooms.size -1) {
            currentRoomIndex++
            return true
        } else {
            // next biome
            if (currentBiomeIndex < fullRun.size -1) {
                currentBiomeIndex++
                currentRoomIndex = 0
                return true
            } else {
                // victory
                return false
            }
        }
    }

    fun generateShopOffer(): List<Any> {
        val offers = mutableListOf<Any>()
        repeat(3) {
            if (rng.nextBoolean()) {
                offers.add(WeaponRegistry.getRandom(com.lumenfall.echoes.game.Rarity.RARE, rng))
            } else {
                offers.add(AbilityRegistry.getRandom(rng))
            }
        }
        return offers
    }

    fun addCoins(amount: Int) {
        coins += amount
        runStats.coinsCollected += amount
    }

    fun toRunSave(): RunSave {
        return RunSave(
            seed = currentSeed,
            currentBiomeIndex = currentBiomeIndex,
            currentRoomIndex = currentRoomIndex,
            coins = coins,
            primaryWeaponId = primaryWeapon.id,
            rangedWeaponId = rangedWeapon.id,
            ability1Id = ability1.id,
            ability2Id = ability2.id,
            lumenShards = lumenShards,
            isActive = true
        )
    }

    data class RunStats(
        var enemiesKilled: Int = 0,
        var damageDealt: Int = 0,
        var damageTaken: Int = 0,
        var roomsCleared: Int = 0,
        var coinsCollected: Int = 0,
        var timePlayed: Float = 0f
    )
}
