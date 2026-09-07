package com.lumenfall.echoes.entities

import com.lumenfall.echoes.game.Constants
import com.lumenfall.echoes.game.DamageType
import com.lumenfall.echoes.utils.Rect
import com.lumenfall.echoes.utils.Vec2
import com.lumenfall.echoes.combat.StatusEffect
import kotlin.math.abs

enum class EnemyState {
    IDLE, PATROL, CHASE, ATTACK, HURT, DEAD, SPECIAL
}

abstract class Enemy : Entity() {
    var maxHp = 40
    var hp = 40
    var damage = 12
    var state = EnemyState.IDLE
    var stateTime = 0f
    var detectionRange = Constants.ENEMY_DETECTION_RANGE
    var attackRange = Constants.ENEMY_ATTACK_RANGE_MELEE
    var attackCooldown = 0f
    var hurtTimer = 0f
    var isElite = false
    var coinValue = 5
    var statusEffects = mutableListOf<StatusEffect>()
    var knockbackResist = 0.5f

    abstract val archetype: com.lumenfall.echoes.game.EnemyArchetype
    abstract fun getWidth(): Float
    abstract fun getHeight(): Float

    init {
        width = getWidth()
        height = getHeight()
    }

    open fun canSeePlayer(player: Player): Boolean {
        val dist = center.distance(player.center)
        return dist < detectionRange
    }

    override fun update(dt: Float) {
        if (hp <= 0) {
            state = EnemyState.DEAD
            active = false
            return
        }
        stateTime += dt
        if (attackCooldown > 0f) attackCooldown -= dt
        if (hurtTimer > 0f) hurtTimer -= dt

        // status effects
        val iter = statusEffects.iterator()
        while (iter.hasNext()) {
            val eff = iter.next()
            val dmg = eff.update(dt)
            if (dmg > 0) hp -= dmg
            if (!eff.active) iter.remove()
        }

        // gravity
        if (!grounded) {
            vel.y += Constants.PLAYER_GRAVITY * 0.8f * dt
            if (vel.y > Constants.PLAYER_MAX_FALL) vel.y = Constants.PLAYER_MAX_FALL
        }

        updateAI(dt)
    }

    abstract fun updateAI(dt: Float)

    override fun takeDamage(amount: Int, knockback: Vec2, from: Entity?) {
        if (state == EnemyState.DEAD) return
        hp -= amount
        vel.x += knockback.x * (1f - knockbackResist) * 0.12f
        vel.y += knockback.y * (1f - knockbackResist) * 0.08f
        state = EnemyState.HURT
        stateTime = 0f
        hurtTimer = 0.22f
        if (hp <= 0) {
            state = EnemyState.DEAD
            active = false
        }
    }

    fun isAlive() = hp > 0 && state != EnemyState.DEAD

    fun applyStatus(effect: StatusEffect) {
        statusEffects.add(effect)
    }
}

// Concrete enemy types - original designs

class ShamblerHusk : Enemy() {
    override val archetype = com.lumenfall.echoes.game.EnemyArchetype.SHAMBLER_HUSK
    override fun getWidth() = 30f
    override fun getHeight() = 38f
    private var patrolDir = 1
    private var patrolTimer = 0f
    var playerRef: Player? = null

    init {
        maxHp = 38
        hp = maxHp
        damage = 14
        attackRange = 48f
        detectionRange = 320f
        coinValue = 6
    }

    override fun updateAI(dt: Float) {
        val player = playerRef ?: return
        val dist = center.distance(player.center)

        when(state) {
            EnemyState.IDLE, EnemyState.PATROL -> {
                // patrol
                patrolTimer += dt
                if (patrolTimer > 2f) {
                    patrolDir *= -1
                    patrolTimer = 0f
                    facing = patrolDir
                }
                vel.x = patrolDir * 45f
                if (canSeePlayer(player)) {
                    state = EnemyState.CHASE
                    stateTime = 0f
                }
            }
            EnemyState.CHASE -> {
                facing = if (player.pos.x > pos.x) 1 else -1
                val dir = if (player.pos.x > pos.x) 1f else -1f
                vel.x = dir * 110f
                if (dist < attackRange && attackCooldown <= 0f) {
                    state = EnemyState.ATTACK
                    stateTime = 0f
                }
                if (dist > detectionRange*1.4f) {
                    state = EnemyState.PATROL
                }
            }
            EnemyState.ATTACK -> {
                vel.x *= 0.8f
                if (stateTime > 0.35f) {
                    // attack hit window
                    if (dist < attackRange + 12f && playerRef != null && !playerRef!!.isInvulnerable()) {
                        playerRef!!.takeDamage(damage, Vec2(facing*180f, -100f), this)
                    }
                }
                if (stateTime > 0.6f) {
                    state = EnemyState.CHASE
                    attackCooldown = 1.1f
                }
            }
            EnemyState.HURT -> {
                vel.x *= 0.85f
                if (hurtTimer <= 0f) state = EnemyState.CHASE
            }
            else -> {}
        }
    }
}

class Thornling : Enemy() {
    override val archetype = com.lumenfall.echoes.game.EnemyArchetype.THORNLING
    override fun getWidth() = 26f
    override fun getHeight() = 26f
    var playerRef: Player? = null
    var shootTimer = 0f

    init {
        maxHp = 24
        hp = maxHp
        damage = 10
        detectionRange = 420f
        attackRange = 320f
        coinValue = 7
    }

    override fun updateAI(dt: Float) {
        val player = playerRef ?: return
        val dist = center.distance(player.center)
        when(state) {
            EnemyState.IDLE -> {
                if (canSeePlayer(player)) state = EnemyState.CHASE
            }
            EnemyState.CHASE -> {
                // keep distance
                if (dist < 200f) {
                    val dir = if (pos.x < player.pos.x) -1f else 1f
                    vel.x = dir * 80f
                    facing = if (dir>0) 1 else -1
                } else if (dist > 340f) {
                    val dir = if (pos.x < player.pos.x) 1f else -1f
                    vel.x = dir * 70f
                    facing = if (dir>0) 1 else -1
                } else {
                    vel.x *= 0.8f
                }
                shootTimer += dt
                if (shootTimer > 1.8f && dist < attackRange) {
                    state = EnemyState.ATTACK
                    stateTime = 0f
                    shootTimer = 0f
                }
            }
            EnemyState.ATTACK -> {
                vel.x *= 0.9f
                if (stateTime > 0.4f) {
                    // spawn projectile handled by game engine via callback
                    state = EnemyState.CHASE
                    attackCooldown = 0.5f
                }
            }
            EnemyState.HURT -> {
                if (hurtTimer <= 0f) state = EnemyState.CHASE
            }
            else -> {}
        }
    }
}

class GloomMoth : Enemy() {
    override val archetype = com.lumenfall.echoes.game.EnemyArchetype.GLOOM_MOTH
    override fun getWidth() = 32f
    override fun getHeight() = 22f
    var playerRef: Player? = null
    var floatOffset = 0f

    init {
        maxHp = 18
        hp = maxHp
        damage = 8
        detectionRange = 380f
        attackRange = 40f
        coinValue = 5
    }

    override fun updateAI(dt: Float) {
        val player = playerRef ?: return
        floatOffset += dt * 3f
        // flying - no gravity
        vel.y = kotlin.math.sin(floatOffset) * 30f

        val dist = center.distance(player.center)
        when(state) {
            EnemyState.IDLE -> {
                if (canSeePlayer(player)) state = EnemyState.CHASE
                vel.x = kotlin.math.sin(stateTime*0.7f)*40f
            }
            EnemyState.CHASE -> {
                val dx = player.center.x - center.x
                val dy = player.center.y - center.y - 20f
                vel.x += (dx*0.8f - vel.x)*dt*2f
                vel.y += (dy*0.8f - vel.y)*dt*2f
                facing = if (dx>0) 1 else -1
                if (dist < attackRange) {
                    state = EnemyState.ATTACK
                    stateTime = 0f
                }
            }
            EnemyState.ATTACK -> {
                if (stateTime < 0.15f) {
                    // dive
                    val dir = if (player.pos.x > pos.x) 1f else -1f
                    vel.x = dir * 220f
                    vel.y = 180f
                }
                if (stateTime > 0.5f) {
                    if (dist < 60f && !player.isInvulnerable()) {
                        player.takeDamage(damage, Vec2(facing*120f, -80f), this)
                    }
                    state = EnemyState.CHASE
                }
            }
            EnemyState.HURT -> {
                if (hurtTimer <= 0f) state = EnemyState.CHASE
            }
            else -> {}
        }
        // clamp
        vel.x = vel.x.coerceIn(-180f,180f)
        vel.y = vel.y.coerceIn(-200f,200f)
    }

    override fun update(dt: Float) {
        // override to not apply gravity
        if (hp <= 0) { state = EnemyState.DEAD; active=false; return }
        stateTime += dt
        if (attackCooldown>0f) attackCooldown-=dt
        if (hurtTimer>0f) hurtTimer-=dt
        updateAI(dt)
    }
}

class AegisRemnant : Enemy() {
    override val archetype = com.lumenfall.echoes.game.EnemyArchetype.AEGIS_REMNANT
    override fun getWidth() = 36f
    override fun getHeight() = 48f
    var playerRef: Player? = null
    var isBlocking = false
    var blockTimer = 0f

    init {
        maxHp = 85
        hp = maxHp
        damage = 18
        detectionRange = 300f
        attackRange = 54f
        coinValue = 12
        knockbackResist = 0.8f
    }

    override fun updateAI(dt: Float) {
        val player = playerRef ?: return
        val dist = center.distance(player.center)

        if (isBlocking) {
            blockTimer -= dt
            vel.x *= 0.85f
            if (blockTimer <= 0f) {
                isBlocking = false
                state = EnemyState.CHASE
            }
            return
        }

        when(state) {
            EnemyState.IDLE, EnemyState.PATROL -> {
                if (canSeePlayer(player)) state = EnemyState.CHASE
                vel.x = facing * 30f
                if (stateTime > 3f) { facing *= -1; stateTime=0f }
            }
            EnemyState.CHASE -> {
                facing = if (player.pos.x > pos.x) 1 else -1
                vel.x = facing * 65f
                if (dist < attackRange) {
                    // 30% chance to block if player attacking
                    if (Math.random() < 0.3 && player.state == PlayerState.ATTACK) {
                        isBlocking = true
                        blockTimer = 1.2f
                        state = EnemyState.SPECIAL
                    } else {
                        state = EnemyState.ATTACK
                        stateTime = 0f
                    }
                }
            }
            EnemyState.ATTACK -> {
                vel.x *= 0.7f
                if (stateTime > 0.45f && stateTime < 0.55f) {
                    if (dist < attackRange+10f && !player.isInvulnerable()) {
                        player.takeDamage(damage, Vec2(facing*200f, -60f), this)
                    }
                }
                if (stateTime > 0.85f) {
                    state = EnemyState.CHASE
                    attackCooldown = 1.4f
                }
            }
            EnemyState.HURT -> {
                if (!isBlocking) {
                    if (hurtTimer <= 0f) state = EnemyState.CHASE
                } else {
                    // blocked - no hurt
                    state = EnemyState.CHASE
                    hurtTimer = 0f
                }
            }
            else -> {}
        }
    }

    override fun takeDamage(amount: Int, knockback: Vec2, from: Entity?) {
        if (isBlocking) {
            // reduced damage
            super.takeDamage((amount*0.25f).toInt(), Vec2(knockback.x*0.2f, knockback.y*0.2f), from)
            return
        }
        super.takeDamage(amount, knockback, from)
    }
}

class CinderCharger : Enemy() {
    override val archetype = com.lumenfall.echoes.game.EnemyArchetype.CINDER_CHARGER
    override fun getWidth() = 42f
    override fun getHeight() = 32f
    var playerRef: Player? = null
    var chargeDir = 1
    var isCharging = false
    var chargeTimer = 0f

    init {
        maxHp = 52
        hp = maxHp
        damage = 22
        detectionRange = 400f
        attackRange = 300f
        coinValue = 10
    }

    override fun updateAI(dt: Float) {
        val player = playerRef ?: return
        val dist = center.distance(player.center)

        when(state) {
            EnemyState.IDLE -> {
                if (canSeePlayer(player)) state = EnemyState.CHASE
            }
            EnemyState.CHASE -> {
                if (!isCharging) {
                    facing = if (player.pos.x > pos.x) 1 else -1
                    vel.x = facing * 55f
                    if (dist < attackRange && abs(player.pos.y - pos.y) < 50f && attackCooldown <= 0f) {
                        // start charge
                        isCharging = true
                        chargeDir = facing
                        chargeTimer = 0f
                        state = EnemyState.SPECIAL
                        stateTime = 0f
                    }
                }
            }
            EnemyState.SPECIAL -> {
                chargeTimer += dt
                if (chargeTimer < 0.5f) {
                    // windup
                    vel.x *= 0.9f
                } else if (chargeTimer < 1.6f) {
                    vel.x = chargeDir * 360f
                    // damage player if collide
                    if (dist < 50f && !player.isInvulnerable()) {
                        player.takeDamage(damage, Vec2(chargeDir*320f, -120f), this)
                        isCharging = false
                        state = EnemyState.IDLE
                        attackCooldown = 2f
                        vel.x *= -0.3f
                    }
                } else {
                    isCharging = false
                    state = EnemyState.IDLE
                    attackCooldown = 2f
                }
            }
            EnemyState.HURT -> {
                if (hurtTimer <= 0f) {
                    state = EnemyState.CHASE
                    isCharging = false
                }
            }
            else -> {}
        }
    }
}

class RiftStalker : Enemy() {
    override val archetype = com.lumenfall.echoes.game.EnemyArchetype.RIFT_STALKER
    override fun getWidth() = 28f
    override fun getHeight() = 40f
    var playerRef: Player? = null
    var teleportCooldown = 0f

    init {
        maxHp = 34
        hp = maxHp
        damage = 16
        detectionRange = 360f
        attackRange = 56f
        coinValue = 9
    }

    override fun updateAI(dt: Float) {
        val player = playerRef ?: return
        if (teleportCooldown>0f) teleportCooldown-=dt
        val dist = center.distance(player.center)

        when(state) {
            EnemyState.IDLE -> {
                if (canSeePlayer(player)) state = EnemyState.CHASE
            }
            EnemyState.CHASE -> {
                // teleport behind player occasionally
                if (teleportCooldown <= 0f && dist > 120f && Math.random()<0.015) {
                    val behind = if (player.facing>0) -1 else 1
                    pos.x = player.pos.x + behind*40f
                    pos.y = player.pos.y
                    teleportCooldown = 4f
                    state = EnemyState.ATTACK
                    stateTime = 0f
                } else {
                    facing = if (player.pos.x > pos.x) 1 else -1
                    vel.x = facing * 95f
                    if (dist < attackRange) {
                        state = EnemyState.ATTACK
                        stateTime = 0f
                    }
                }
            }
            EnemyState.ATTACK -> {
                vel.x *= 0.8f
                if (stateTime > 0.25f && stateTime < 0.35f) {
                    if (dist < attackRange+12f && !player.isInvulnerable()) {
                        player.takeDamage(damage, Vec2(facing*160f, -90f), this)
                    }
                }
                if (stateTime > 0.6f) {
                    state = EnemyState.CHASE
                    attackCooldown = 0.9f
                    // teleport away
                    if (Math.random()<0.6) {
                        pos.x += -facing*80f
                        teleportCooldown = 3f
                    }
                }
            }
            EnemyState.HURT -> {
                if (hurtTimer <= 0f) state = EnemyState.CHASE
                // chance to teleport when hurt
                if (Math.random()<0.4 && teleportCooldown<=0f) {
                    pos.x += (if (Math.random()<0.5) 1 else -1)*100f
                    teleportCooldown = 2.5f
                }
            }
            else -> {}
        }
    }
}

class SorrowSower : Enemy() {
    override val archetype = com.lumenfall.echoes.game.EnemyArchetype.SORROW_SOWER
    override fun getWidth() = 34f
    override fun getHeight() = 46f
    var playerRef: Player? = null
    var summonTimer = 0f
    var hasSummoned = false

    init {
        maxHp = 60
        hp = maxHp
        damage = 12
        detectionRange = 420f
        attackRange = 280f
        coinValue = 15
    }

    override fun updateAI(dt: Float) {
        val player = playerRef ?: return
        val dist = center.distance(player.center)
        summonTimer += dt

        when(state) {
            EnemyState.IDLE -> {
                if (canSeePlayer(player)) state = EnemyState.CHASE
            }
            EnemyState.CHASE -> {
                // keep distance, summon
                if (dist < 180f) {
                    val dir = if (pos.x < player.pos.x) -1 else 1
                    vel.x = dir * 60f
                } else {
                    vel.x *= 0.85f
                }
                if (summonTimer > 5f) {
                    state = EnemyState.SPECIAL
                    stateTime = 0f
                    summonTimer = 0f
                } else if (dist < attackRange && attackCooldown <=0f) {
                    state = EnemyState.ATTACK
                    stateTime = 0f
                }
            }
            EnemyState.ATTACK -> {
                // ranged sorrow projectile
                if (stateTime > 0.5f) {
                    state = EnemyState.CHASE
                    attackCooldown = 1.5f
                }
            }
            EnemyState.SPECIAL -> {
                vel.x *= 0.9f
                if (stateTime > 1f && !hasSummoned) {
                    hasSummoned = true
                    // spawn handled by engine
                }
                if (stateTime > 1.4f) {
                    state = EnemyState.CHASE
                    hasSummoned = false
                }
            }
            EnemyState.HURT -> {
                if (hurtTimer <=0f) state = EnemyState.CHASE
            }
            else -> {}
        }
    }
}

class SpireWarden : Enemy() {
    override val archetype = com.lumenfall.echoes.game.EnemyArchetype.SPIRE_WARDEN
    override fun getWidth() = 40f
    override fun getHeight() = 52f
    var playerRef: Player? = null
    var slamCooldown = 0f

    init {
        maxHp = 95
        hp = maxHp
        damage = 20
        detectionRange = 340f
        attackRange = 70f
        coinValue = 14
        knockbackResist = 0.75f
    }

    override fun updateAI(dt: Float) {
        val player = playerRef ?: return
        val dist = center.distance(player.center)
        if (slamCooldown>0f) slamCooldown-=dt

        when(state) {
            EnemyState.IDLE -> {
                if (canSeePlayer(player)) state = EnemyState.CHASE
            }
            EnemyState.CHASE -> {
                facing = if (player.pos.x > pos.x) 1 else -1
                vel.x = facing * 50f
                if (dist < attackRange && attackCooldown<=0f) {
                    state = EnemyState.ATTACK
                    stateTime = 0f
                } else if (dist < 160f && slamCooldown<=0f && Math.random()<0.008) {
                    state = EnemyState.SPECIAL
                    stateTime = 0f
                }
            }
            EnemyState.ATTACK -> {
                vel.x *= 0.75f
                if (stateTime>0.6f && stateTime<0.7f) {
                    if (dist<attackRange+16f && !player.isInvulnerable()) {
                        player.takeDamage(damage, Vec2(facing*220f, -70f), this)
                    }
                }
                if (stateTime>1f) {
                    state = EnemyState.CHASE
                    attackCooldown=1.2f
                }
            }
            EnemyState.SPECIAL -> {
                // ground slam - area denial
                vel.x *= 0.8f
                if (stateTime>0.8f && stateTime<0.9f) {
                    if (dist<140f && !player.isInvulnerable()) {
                        player.takeDamage((damage*1.3f).toInt(), Vec2(facing*180f, -200f), this)
                    }
                }
                if (stateTime>1.3f) {
                    state = EnemyState.CHASE
                    slamCooldown=5f
                }
            }
            EnemyState.HURT -> {
                if (hurtTimer<=0f) state=EnemyState.CHASE
            }
            else -> {}
        }
    }
}

class EliteEnemy(val base: Enemy) : Enemy() {
    override val archetype = com.lumenfall.echoes.game.EnemyArchetype.ELITE
    override fun getWidth() = base.getWidth()*1.25f
    override fun getHeight() = base.getHeight()*1.25f
    private var auraTimer = 0f

    init {
        maxHp = (base.maxHp * 2.2f).toInt()
        hp = maxHp
        damage = (base.damage*1.5f).toInt()
        detectionRange = base.detectionRange*1.2f
        coinValue = base.coinValue*3
        isElite = true
        width = getWidth()
        height = getHeight()
        // copy pos
        pos.set(base.pos)
    }

    override fun updateAI(dt: Float) {
        // delegate to base but with boosted stats
        // we reuse base logic by copying its AI - simplified
        auraTimer += dt
        // elite aura damages nearby
        base.updateAI(dt)
        // copy vel etc
        vel.set(base.vel)
        facing = base.facing
        state = base.state
        stateTime = base.stateTime
    }

    override fun update(dt: Float) {
        super.update(dt)
        base.pos.set(pos)
        base.vel.set(vel)
        base.playerRef.let { 
            when(base){
                is ShamblerHusk -> base.playerRef = (this as? EliteEnemy)?.let { (it as? ShamblerHusk)?.playerRef } // placeholder
                else -> {}
            }
        }
    }
}

// Extension to allow setting player ref generically
var Enemy.playerRef: Player?
    get() = when(this){
        is ShamblerHusk -> this.playerRef
        is Thornling -> this.playerRef
        is GloomMoth -> this.playerRef
        is AegisRemnant -> this.playerRef
        is CinderCharger -> this.playerRef
        is RiftStalker -> this.playerRef
        is SorrowSower -> this.playerRef
        is SpireWarden -> this.playerRef
        else -> null
    }
    set(value){
        when(this){
            is ShamblerHusk -> this.playerRef = value
            is Thornling -> this.playerRef = value
            is GloomMoth -> this.playerRef = value
            is AegisRemnant -> this.playerRef = value
            is CinderCharger -> this.playerRef = value
            is RiftStalker -> this.playerRef = value
            is SorrowSower -> this.playerRef = value
            is SpireWarden -> this.playerRef = value
        }
    }
