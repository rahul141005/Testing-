package com.lumenfall.echoes.combat

import com.lumenfall.echoes.game.Constants
import com.lumenfall.echoes.game.DamageType
import com.lumenfall.echoes.game.Rarity
import com.lumenfall.echoes.game.WeaponCategory
import com.lumenfall.echoes.utils.Vec2

data class WeaponStats(
    val damage: Int,
    val attackSpeed: Float, // attacks per second
    val range: Float,
    val knockback: Float,
    val critChance: Float,
    val critMultiplier: Float,
    val staminaCost: Float = 0f,
    val cooldown: Float = 0f
)

data class HitboxDef(
    val offsetX: Float,
    val offsetY: Float,
    val w: Float,
    val h: Float,
    val activeFrom: Float, // normalized time 0-1
    val activeTo: Float,
    val damageMult: Float = 1f
)

abstract class Weapon(
    val id: String,
    val name: String,
    val category: WeaponCategory,
    val rarity: Rarity,
    val stats: WeaponStats,
    val damageType: DamageType,
    val description: String,
    val lore: String
) {
    var comboIndex = 0
    var comboTimer = 0f
    var cooldownTimer = 0f
    var isAttacking = false
    var attackTimer = 0f

    abstract fun getHitboxes(combo: Int, facing: Int): List<HitboxDef>
    abstract fun getAttackDuration(combo: Int): Float
    abstract fun getMaxCombo(): Int

    open fun canAttack(): Boolean = cooldownTimer <= 0f && !isAttacking

    open fun startAttack(): Boolean {
        if (!canAttack()) return false
        isAttacking = true
        attackTimer = 0f
        return true
    }

    open fun update(dt: Float) {
        if (cooldownTimer > 0f) cooldownTimer -= dt
        if (isAttacking) {
            attackTimer += dt
            val dur = getAttackDuration(comboIndex)
            if (attackTimer >= dur) {
                isAttacking = false
                comboIndex = (comboIndex + 1) % getMaxCombo()
                comboTimer = 0.8f
                cooldownTimer = 1f / stats.attackSpeed * 0.3f
            }
        } else {
            if (comboTimer > 0f) {
                comboTimer -= dt
                if (comboTimer <= 0f) comboIndex = 0
            }
        }
    }

    open fun getCurrentDamage(): Int {
        val base = stats.damage
        val comboMult = when(comboIndex) {
            0 -> 1f
            1 -> 1.15f
            2 -> 1.35f
            else -> 1f
        }
        return (base * comboMult).toInt()
    }

    fun reset() {
        comboIndex = 0
        comboTimer = 0f
        cooldownTimer = 0f
        isAttacking = false
        attackTimer = 0f
    }
}

// Concrete weapons - original designs

class Dawnblade : Weapon(
    id = "dawnblade",
    name = "Dawnblade",
    category = WeaponCategory.SWORD,
    rarity = Rarity.COMMON,
    stats = WeaponStats(damage = 18, attackSpeed = 2.2f, range = 58f, knockback = 120f, critChance = 0.15f, critMultiplier = 1.8f),
    damageType = DamageType.PHYSICAL,
    description = "Balanced lumen-forged blade. Swift triple slash.",
    lore = "Forged from the first light that broke through the Citadel's dome."
) {
    override fun getHitboxes(combo: Int, facing: Int): List<HitboxDef> = when(combo) {
        0 -> listOf(HitboxDef( if(facing>0) 28f else -52f, 6f, 52f, 28f, 0.15f, 0.45f))
        1 -> listOf(HitboxDef( if(facing>0) 32f else -56f, 2f, 56f, 32f, 0.2f, 0.5f, 1.1f))
        2 -> listOf(HitboxDef( if(facing>0) 20f else -68f, -4f, 68f, 40f, 0.25f, 0.6f, 1.3f))
        else -> emptyList()
    }
    override fun getAttackDuration(combo: Int) = when(combo){0->0.28f 1->0.30f 2->0.38f else->0.3f}
    override fun getMaxCombo() = 3
}

class GravHammer : Weapon(
    id = "grav_hammer",
    name = "Grav Hammer",
    category = WeaponCategory.HEAVY,
    rarity = Rarity.RARE,
    stats = WeaponStats(damage = 42, attackSpeed = 0.9f, range = 64f, knockback = 420f, critChance = 0.08f, critMultiplier = 2.2f),
    damageType = DamageType.PHYSICAL,
    description = "Slow, crushing. Slams create shockwave.",
    lore = "A collapsed star heart, hammered into shape by the Forge Wardens."
) {
    override fun getHitboxes(combo: Int, facing: Int): List<HitboxDef> = listOf(
        HitboxDef(if(facing>0) 24f else -72f, 8f, 72f, 36f, 0.35f, 0.65f),
        HitboxDef(if(facing>0) -20f else -20f, 20f, 68f, 24f, 0.5f, 0.7f, 0.6f) // ground shock
    )
    override fun getAttackDuration(combo: Int) = 0.62f
    override fun getMaxCombo() = 1
}

class ThornSpear : Weapon(
    id = "thorn_spear",
    name = "Thorn Spear",
    category = WeaponCategory.SPEAR,
    rarity = Rarity.UNCOMMON,
    stats = WeaponStats(damage = 24, attackSpeed = 1.6f, range = 84f, knockback = 180f, critChance = 0.22f, critMultiplier = 1.9f),
    damageType = DamageType.PHYSICAL,
    description = "Long reach, precise thrusts. Crits on tip.",
    lore = "Grown from the heart-root of Gloomroot, it still seeks flesh."
) {
    override fun getHitboxes(combo: Int, facing: Int): List<HitboxDef> = when(combo){
        0 -> listOf(HitboxDef(if(facing>0) 30f else -90f, 12f, 84f, 14f, 0.2f, 0.5f))
        1 -> listOf(HitboxDef(if(facing>0) 36f else -96f, 10f, 90f, 16f, 0.25f, 0.55f, 1.2f))
        else -> emptyList()
    }
    override fun getAttackDuration(combo: Int) = 0.36f
    override fun getMaxCombo() = 2
}

class DuskwindBlades : Weapon(
    id = "duskwind_blades",
    name = "Duskwind Blades",
    category = WeaponCategory.DUAL,
    rarity = Rarity.RARE,
    stats = WeaponStats(damage = 13, attackSpeed = 3.4f, range = 48f, knockback = 70f, critChance = 0.28f, critMultiplier = 1.7f),
    damageType = DamageType.PHYSICAL,
    description = "Twin curved blades. Flurry of 5 rapid hits.",
    lore = "Worn by the Veil Dancers who could cut wind itself."
) {
    override fun getHitboxes(combo: Int, facing: Int): List<HitboxDef> = listOf(
        HitboxDef(if(facing>0) 22f else -46f, 6f+combo*2f, 46f, 20f, 0.1f + combo*0.05f, 0.3f + combo*0.05f, 1f + combo*0.1f)
    )
    override fun getAttackDuration(combo: Int) = 0.22f
    override fun getMaxCombo() = 5
}

class RivenAxe : Weapon(
    id = "riven_axe",
    name = "Riven Axe",
    category = WeaponCategory.AXE,
    rarity = Rarity.UNCOMMON,
    stats = WeaponStats(damage = 32, attackSpeed = 1.3f, range = 62f, knockback = 260f, critChance = 0.18f, critMultiplier = 2f),
    damageType = DamageType.PHYSICAL,
    description = "Cleave in wide arc. Hits all around on final combo.",
    lore = "Riven from a single slab of obsidian found in the Ashen Ramparts."
) {
    override fun getHitboxes(combo: Int, facing: Int): List<HitboxDef> = if (combo==2) listOf(
        HitboxDef(-62f, -10f, 152f, 58f, 0.3f, 0.65f, 1.4f)
    ) else listOf(
        HitboxDef(if(facing>0) 26f else -64f, 4f, 64f, 36f, 0.2f, 0.55f)
    )
    override fun getAttackDuration(combo: Int) = if(combo==2) 0.48f else 0.38f
    override fun getMaxCombo() = 3
}

class NeedleOfUnmaking : Weapon(
    id = "needle_unmaking",
    name = "Needle of Unmaking",
    category = WeaponCategory.UNCONVENTIONAL,
    rarity = Rarity.EPIC,
    stats = WeaponStats(damage = 55, attackSpeed = 0.8f, range = 72f, knockback = 100f, critChance = 0.35f, critMultiplier = 2.5f),
    damageType = DamageType.VOID,
    description = "Single precise stitch that unravels enemies. Teleports on crit.",
    lore = "Used by the Tailors of the Void to unmake reality's seams."
) {
    override fun getHitboxes(combo: Int, facing: Int): List<HitboxDef> = listOf(
        HitboxDef(if(facing>0) 28f else -72f, 14f, 72f, 8f, 0.4f, 0.55f, 2f)
    )
    override fun getAttackDuration(combo: Int) = 0.52f
    override fun getMaxCombo() = 1
}

// Ranged
class Aetherbow : Weapon(
    id = "aetherbow",
    name = "Aetherbow",
    category = WeaponCategory.BOW,
    rarity = Rarity.COMMON,
    stats = WeaponStats(damage = 20, attackSpeed = 1.1f, range = 520f, knockback = 90f, critChance = 0.2f, critMultiplier = 2f),
    damageType = DamageType.PHYSICAL,
    description = "Lumen arrows. Hold to charge.",
    lore = "String made from condensed dawn light."
) {
    override fun getHitboxes(combo: Int, facing: Int) = emptyList<HitboxDef>() // projectile based
    override fun getAttackDuration(combo: Int) = 0.35f
    override fun getMaxCombo() = 1
}

class IroncastCrossbow : Weapon(
    id = "ironcast_xbow",
    name = "Ironcast Arbalest",
    category = WeaponCategory.CROSSBOW,
    rarity = Rarity.UNCOMMON,
    stats = WeaponStats(damage = 38, attackSpeed = 0.7f, range = 480f, knockback = 220f, critChance = 0.12f, critMultiplier = 2.3f),
    damageType = DamageType.PHYSICAL,
    description = "Heavy bolt, pierces two enemies.",
    lore = "Forgeworks defense weapon, repurposed."
) {
    override fun getHitboxes(combo: Int, facing: Int) = emptyList<HitboxDef>()
    override fun getAttackDuration(combo: Int) = 0.55f
    override fun getMaxCombo() = 1
}

class Emberhand : Weapon(
    id = "emberhand",
    name = "Emberhand",
    category = WeaponCategory.EMBERHAND,
    rarity = Rarity.RARE,
    stats = WeaponStats(damage = 16, attackSpeed = 2.8f, range = 320f, knockback = 60f, critChance = 0.15f, critMultiplier = 1.8f),
    damageType = DamageType.FIRE,
    description = "Rapid fire embers. Burns enemies.",
    lore = "A gauntlet that channels the Foundry's eternal flame."
) {
    override fun getHitboxes(combo: Int, facing: Int) = emptyList<HitboxDef>()
    override fun getAttackDuration(combo: Int) = 0.18f
    override fun getMaxCombo() = 1
}

class ShardDaggers : Weapon(
    id = "shard_daggers",
    name = "Shard Daggers",
    category = WeaponCategory.THROWABLE,
    rarity = Rarity.EPIC,
    stats = WeaponStats(damage = 14, attackSpeed = 2f, range = 280f, knockback = 40f, critChance = 0.3f, critMultiplier = 2.1f),
    damageType = DamageType.FROST,
    description = "Throws 3 seeking shards.",
    lore = "Splinters of the Lumen Archive's frozen knowledge."
) {
    override fun getHitboxes(combo: Int, facing: Int) = emptyList<HitboxDef>()
    override fun getAttackDuration(combo: Int) = 0.32f
    override fun getMaxCombo() = 1
}

object WeaponRegistry {
    private val all = listOf(
        Dawnblade(), GravHammer(), ThornSpear(), DuskwindBlades(),
        RivenAxe(), NeedleOfUnmaking(), Aetherbow(), IroncastCrossbow(),
        Emberhand(), ShardDaggers()
    )

    fun getAll(): List<Weapon> = all
    fun getById(id: String): Weapon? = all.find { it.id == id }
    fun getRandom(rarityUpTo: Rarity = Rarity.EPIC, rng: com.lumenfall.echoes.utils.SeededRandom): Weapon {
        val filtered = all.filter { it.rarity.ordinal <= rarityUpTo.ordinal }
        return filtered[rng.nextInt(filtered.size)].let {
            // Clone via new instance
            when(it.id){
                "dawnblade" -> Dawnblade()
                "grav_hammer" -> GravHammer()
                "thorn_spear" -> ThornSpear()
                "duskwind_blades" -> DuskwindBlades()
                "riven_axe" -> RivenAxe()
                "needle_unmaking" -> NeedleOfUnmaking()
                "aetherbow" -> Aetherbow()
                "ironcast_xbow" -> IroncastCrossbow()
                "emberhand" -> Emberhand()
                "shard_daggers" -> ShardDaggers()
                else -> Dawnblade()
            }
        }
    }
}
