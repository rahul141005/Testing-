package com.lumenfall.echoes.combat

import com.lumenfall.echoes.game.DamageType
import com.lumenfall.echoes.utils.Vec2
import kotlin.math.max

data class DamageEvent(
    val amount: Int,
    val type: DamageType,
    val isCrit: Boolean,
    val knockback: Vec2,
    val hitPos: Vec2,
    val fromPlayer: Boolean
)

object DamageCalculator {
    private val resistances = mapOf(
        DamageType.PHYSICAL to 1f,
        DamageType.FIRE to 1f,
        DamageType.FROST to 1f,
        DamageType.VOLT to 1f,
        DamageType.VOID to 1f,
        DamageType.TRUE_DAMAGE to 1f
    )

    fun calculate(baseDamage: Int, type: DamageType, crit: Boolean, critMult: Float, targetResist: Map<DamageType, Float> = emptyMap()): Int {
        var dmg = baseDamage.toFloat()
        if (crit) dmg *= critMult
        val resist = targetResist[type] ?: 1f
        dmg *= resist
        return max(1, dmg.toInt())
    }

    fun applyElementalEffects(type: DamageType, target: Any) {
        // Placeholder for status effects: burn, freeze, shock, etc.
        // Implemented in Enemy/Player update
    }
}

class StatusEffect(
    val type: DamageType,
    var duration: Float,
    var tickRate: Float = 0.5f,
    var tickDamage: Int = 0,
    var slowFactor: Float = 1f
) {
    var tickTimer = 0f
    var active = true
    fun update(dt: Float): Int {
        var dmg = 0
        duration -= dt
        if (duration <= 0f) active = false
        tickTimer += dt
        if (tickTimer >= tickRate) {
            tickTimer = 0f
            dmg = tickDamage
        }
        return dmg
    }
}
