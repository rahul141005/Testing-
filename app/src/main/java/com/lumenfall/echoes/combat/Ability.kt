package com.lumenfall.echoes.combat

import com.lumenfall.echoes.game.DamageType
import com.lumenfall.echoes.game.Rarity

enum class AbilityCategory {
    TRAP, SIGIL, VOLT, SUMMON, DEFENSE, MOBILITY, ULTIMATE
}

data class AbilityStats(
    val damage: Int,
    val cooldown: Float,
    val duration: Float,
    val range: Float,
    val charges: Int = 1
)

abstract class Ability(
    val id: String,
    val name: String,
    val category: AbilityCategory,
    val rarity: Rarity,
    val stats: AbilityStats,
    val damageType: DamageType,
    val description: String
) {
    var cooldownTimer = 0f
    var activeTimer = 0f
    var isActive = false
    var charges = stats.charges

    fun canUse(): Boolean = cooldownTimer <= 0f && charges > 0
    open fun update(dt: Float) {
        if (cooldownTimer > 0f) cooldownTimer -= dt
        if (isActive) {
            activeTimer -= dt
            if (activeTimer <= 0f) {
                isActive = false
                onDeactivate()
            }
        }
    }
    abstract fun activate(x: Float, y: Float, facing: Int): Boolean
    open fun onDeactivate() {}
    fun reset() {
        cooldownTimer = 0f
        activeTimer = 0f
        isActive = false
        charges = stats.charges
    }
}

class EmberTrap : Ability(
    id = "ember_trap",
    name = "Ember Trap",
    category = AbilityCategory.TRAP,
    rarity = Rarity.COMMON,
    stats = AbilityStats(damage = 28, cooldown = 8f, duration = 12f, range = 200f),
    damageType = DamageType.FIRE,
    description = "Places flame trap that explodes when enemies near."
) {
    var trapX = 0f
    var trapY = 0f
    override fun activate(x: Float, y: Float, facing: Int): Boolean {
        if (!canUse()) return false
        trapX = x + facing*40f
        trapY = y
        isActive = true
        activeTimer = stats.duration
        cooldownTimer = stats.cooldown
        charges--
        return true
    }
}

class FrostSigil : Ability(
    id = "frost_sigil",
    name = "Frost Sigil",
    category = AbilityCategory.SIGIL,
    rarity = Rarity.UNCOMMON,
    stats = AbilityStats(damage = 18, cooldown = 10f, duration = 4f, range = 140f),
    damageType = DamageType.FROST,
    description = "Creates freezing field that slows and damages."
) {
    var sigilX = 0f
    var sigilY = 0f
    override fun activate(x: Float, y: Float, facing: Int): Boolean {
        if (!canUse()) return false
        sigilX = x
        sigilY = y
        isActive = true
        activeTimer = stats.duration
        cooldownTimer = stats.cooldown
        return true
    }
}

class VoltChain : Ability(
    id = "volt_chain",
    name = "Volt Chain",
    category = AbilityCategory.VOLT,
    rarity = Rarity.RARE,
    stats = AbilityStats(damage = 22, cooldown = 6f, duration = 0.5f, range = 260f),
    damageType = DamageType.VOLT,
    description = "Lightning jumps between up to 5 enemies."
) {
    var originX = 0f
    var originY = 0f
    override fun activate(x: Float, y: Float, facing: Int): Boolean {
        if (!canUse()) return false
        originX = x
        originY = y
        isActive = true
        activeTimer = stats.duration
        cooldownTimer = stats.cooldown
        return true
    }
}

class SpectralWolves : Ability(
    id = "spectral_wolves",
    name = "Spectral Wolves",
    category = AbilityCategory.SUMMON,
    rarity = Rarity.EPIC,
    stats = AbilityStats(damage = 14, cooldown = 18f, duration = 12f, range = 300f, charges = 2),
    damageType = DamageType.VOID,
    description = "Summons 2 spectral wolves that hunt enemies."
) {
    override fun activate(x: Float, y: Float, facing: Int): Boolean {
        if (!canUse()) return false
        isActive = true
        activeTimer = stats.duration
        cooldownTimer = stats.cooldown
        charges--
        return true
    }
}

class AetherDome : Ability(
    id = "aether_dome",
    name = "Aether Dome",
    category = AbilityCategory.DEFENSE,
    rarity = Rarity.RARE,
    stats = AbilityStats(damage = 0, cooldown = 14f, duration = 3f, range = 0f),
    damageType = DamageType.TRUE_DAMAGE,
    description = "Invulnerable dome that reflects projectiles."
) {
    override fun activate(x: Float, y: Float, facing: Int): Boolean {
        if (!canUse()) return false
        isActive = true
        activeTimer = stats.duration
        cooldownTimer = stats.cooldown
        return true
    }
}

class PhaseDash : Ability(
    id = "phase_dash",
    name = "Phase Dash",
    category = AbilityCategory.MOBILITY,
    rarity = Rarity.UNCOMMON,
    stats = AbilityStats(damage = 24, cooldown = 5f, duration = 0.3f, range = 180f, charges = 2),
    damageType = DamageType.VOID,
    description = "Dash through enemies, dealing void damage. 2 charges."
) {
    var dashDir = 1
    override fun activate(x: Float, y: Float, facing: Int): Boolean {
        if (!canUse()) return false
        dashDir = facing
        isActive = true
        activeTimer = stats.duration
        cooldownTimer = stats.cooldown
        charges--
        // regen charges after cooldown
        return true
    }
    override fun update(dt: Float) {
        super.update(dt)
        if (cooldownTimer <= 0f && charges < stats.charges) {
            // slow recharge
            charges = stats.charges
        }
    }
}

object AbilityRegistry {
    private val all = listOf(
        EmberTrap(), FrostSigil(), VoltChain(), SpectralWolves(), AetherDome(), PhaseDash()
    )
    fun getAll() = all
    fun getRandom(rng: com.lumenfall.echoes.utils.SeededRandom): Ability {
        val id = all[rng.nextInt(all.size)].id
        return when(id){
            "ember_trap" -> EmberTrap()
            "frost_sigil" -> FrostSigil()
            "volt_chain" -> VoltChain()
            "spectral_wolves" -> SpectralWolves()
            "aether_dome" -> AetherDome()
            "phase_dash" -> PhaseDash()
            else -> EmberTrap()
        }
    }
}
