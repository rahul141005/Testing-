package com.lumenfall.echoes.entities

import com.lumenfall.echoes.game.Constants
import com.lumenfall.echoes.utils.Vec2
import kotlin.math.sin

enum class BossType {
    WARDEN_OF_ASH,
    MATRIARCH_OF_ROOTS,
    FORGEHEART_COLOSSUS,
    LUMEN_PARAGON,
    THE_UNRAVELING
}

abstract class Boss : Enemy() {
    abstract val bossType: BossType
    var phase = 1
    var maxPhases = 2
    var isInvulnerable = false
    var invulnTimer = 0f
    var introTimer = 1.5f
    var hasStarted = false

    init {
        isElite = true
        knockbackResist = 1f
    }

    fun checkPhaseTransition() {
        val hpPct = hp.toFloat() / maxHp
        if (phase == 1 && hpPct < 0.55f) {
            phase = 2
            onPhaseTransition(2)
        } else if (maxPhases >=3 && phase == 2 && hpPct < 0.25f) {
            phase = 3
            onPhaseTransition(3)
        }
    }

    open fun onPhaseTransition(newPhase: Int) {
        isInvulnerable = true
        invulnTimer = 2f
        state = EnemyState.SPECIAL
        stateTime = 0f
    }

    override fun update(dt: Float) {
        if (introTimer > 0f) {
            introTimer -= dt
            if (introTimer <= 0f) hasStarted = true
            return
        }
        if (invulnTimer > 0f) {
            invulnTimer -= dt
            if (invulnTimer <= 0f) isInvulnerable = false
        }
        checkPhaseTransition()
        super.update(dt)
    }

    override fun takeDamage(amount: Int, knockback: Vec2, from: Entity?) {
        if (isInvulnerable) return
        if (state == EnemyState.DEAD) return
        super.takeDamage(amount, knockback, from)
    }
}

class WardenOfAsh : Boss() {
    override val bossType = BossType.WARDEN_OF_ASH
    override val archetype = com.lumenfall.echoes.game.EnemyArchetype.BOSS
    override fun getWidth() = 68f
    override fun getHeight() = 84f
    var playerRef: Player? = null
    private var attackPattern = 0
    private var patternTimer = 0f

    init {
        maxHp = 420
        hp = maxHp
        damage = 24
        detectionRange = 800f
        attackRange = 90f
        coinValue = 80
        maxPhases = 2
    }

    override fun updateAI(dt: Float) {
        val player = playerRef ?: return
        val dist = center.distance(player.center)
        patternTimer += dt

        when(state) {
            EnemyState.IDLE -> {
                state = EnemyState.CHASE
            }
            EnemyState.CHASE -> {
                facing = if (player.pos.x > pos.x) 1 else -1
                vel.x = facing * (if(phase==1) 70f else 95f)
                if (dist < attackRange) {
                    state = EnemyState.ATTACK
                    stateTime = 0f
                    attackPattern = (attackPattern+1)%3
                } else if (patternTimer > (if(phase==1) 3.5f else 2.2f)) {
                    state = EnemyState.SPECIAL
                    stateTime = 0f
                    patternTimer = 0f
                }
            }
            EnemyState.ATTACK -> {
                vel.x *= 0.85f
                when(attackPattern) {
                    0 -> { // overhead slam
                        if (stateTime>0.6f && stateTime<0.75f) {
                            if (dist < attackRange+20f && !player.isInvulnerable()) {
                                player.takeDamage(damage, Vec2(facing*260f, -120f), this)
                            }
                        }
                    }
                    1 -> { // sweep
                        if (stateTime>0.4f && stateTime<0.9f) {
                            if (dist < 120f && !player.isInvulnerable()) {
                                player.takeDamage((damage*0.8f).toInt(), Vec2(facing*200f, -40f), this)
                            }
                        }
                    }
                    2 -> { // thrust
                        if (stateTime>0.35f && stateTime<0.5f) {
                            if (dist < 140f && !player.isInvulnerable()) {
                                player.takeDamage((damage*1.2f).toInt(), Vec2(facing*320f, -80f), this)
                            }
                        }
                    }
                }
                if (stateTime > 1.1f) {
                    state = EnemyState.CHASE
                }
            }
            EnemyState.SPECIAL -> {
                // ash eruption - area attack
                vel.x *= 0.9f
                if (stateTime>0.9f && stateTime<1.1f) {
                    // damage in wide area
                    if (dist < 220f && !player.isInvulnerable()) {
                        player.takeDamage((damage*1.1f).toInt(), Vec2(facing*180f, -200f), this)
                    }
                }
                if (stateTime>1.6f) state = EnemyState.CHASE
            }
            EnemyState.HURT -> {
                if (hurtTimer<=0f) state = EnemyState.CHASE
            }
            else -> {}
        }
    }
}

class MatriarchOfRoots : Boss() {
    override val bossType = BossType.MATRIARCH_OF_ROOTS
    override val archetype = com.lumenfall.echoes.game.EnemyArchetype.BOSS
    override fun getWidth() = 88f
    override fun getHeight() = 72f
    var playerRef: Player? = null
    var rootTimer = 0f

    init {
        maxHp = 520
        hp = maxHp
        damage = 22
        detectionRange = 800f
        attackRange = 110f
        coinValue = 100
        maxPhases = 3
    }

    override fun updateAI(dt: Float) {
        val player = playerRef ?: return
        val dist = center.distance(player.center)
        rootTimer += dt

        when(state) {
            EnemyState.IDLE -> state = EnemyState.CHASE
            EnemyState.CHASE -> {
                facing = if (player.pos.x > pos.x) 1 else -1
                vel.x = facing * 45f
                // floats slightly
                vel.y = sin(stateTime*1.2f)*20f

                if (rootTimer > (if(phase==1) 4f else 2.5f)) {
                    state = EnemyState.SPECIAL
                    stateTime = 0f
                    rootTimer = 0f
                } else if (dist < attackRange) {
                    state = EnemyState.ATTACK
                    stateTime = 0f
                }
            }
            EnemyState.ATTACK -> {
                if (stateTime>0.5f && stateTime<0.7f) {
                    if (dist < attackRange+30f && !player.isInvulnerable()) {
                        player.takeDamage(damage, Vec2(facing*200f, -60f), this)
                    }
                }
                if (stateTime>1f) state = EnemyState.CHASE
            }
            EnemyState.SPECIAL -> {
                // summon roots / thornlings
                if (stateTime>0.8f && stateTime<1f) {
                    // handled by engine - spawn minions
                }
                // vine whip across arena
                if (stateTime>1.2f && stateTime<1.4f) {
                    if (dist < 280f && !player.isInvulnerable()) {
                        player.takeDamage((damage*0.9f).toInt(), Vec2(facing*160f, -100f), this)
                    }
                }
                if (stateTime>1.8f) state = EnemyState.CHASE
            }
            EnemyState.HURT -> {
                if (hurtTimer<=0f) state = EnemyState.CHASE
            }
            else -> {}
        }
    }

    override fun update(dt: Float) {
        // flying boss, no gravity
        if (hp <= 0) { state = EnemyState.DEAD; active=false; return }
        stateTime += dt
        if (attackCooldown>0f) attackCooldown-=dt
        if (hurtTimer>0f) hurtTimer-=dt
        if (introTimer>0f) { introTimer-=dt; return }
        if (invulnTimer>0f) { invulnTimer-=dt; if(invulnTimer<=0f) isInvulnerable=false }
        checkPhaseTransition()
        updateAI(dt)
    }
}

class ForgeheartColossus : Boss() {
    override val bossType = BossType.FORGEHEART_COLOSSUS
    override val archetype = com.lumenfall.echoes.game.EnemyArchetype.BOSS
    override fun getWidth() = 96f
    override fun getHeight() = 96f
    var playerRef: Player? = null
    var slamCount = 0

    init {
        maxHp = 680
        hp = maxHp
        damage = 28
        detectionRange = 900f
        attackRange = 100f
        coinValue = 130
        maxPhases = 2
    }

    override fun updateAI(dt: Float) {
        val player = playerRef ?: return
        val dist = center.distance(player.center)

        when(state) {
            EnemyState.IDLE -> state = EnemyState.CHASE
            EnemyState.CHASE -> {
                facing = if (player.pos.x > pos.x) 1 else -1
                vel.x = facing * (phase==1 && true || true).let { if(phase==1) 55f else 75f }
                if (dist < attackRange) {
                    state = EnemyState.ATTACK
                    stateTime = 0f
                    slamCount = 0
                }
            }
            EnemyState.ATTACK -> {
                vel.x *= 0.8f
                // 3-hit combo
                val hitWindow = when(slamCount){
                    0 -> stateTime>0.5f && stateTime<0.7f
                    1 -> stateTime>1.2f && stateTime<1.4f
                    2 -> stateTime>1.9f && stateTime<2.2f
                    else -> false
                }
                if (hitWindow && dist < 130f && !player.isInvulnerable()) {
                    player.takeDamage((damage * (0.9f+slamCount*0.2f)).toInt(), Vec2(facing*(200f+slamCount*40f), -80f), this)
                }
                if (stateTime>0.9f && slamCount==0) slamCount=1
                if (stateTime>1.6f && slamCount==1) slamCount=2
                if (stateTime>2.5f) state = EnemyState.SPECIAL
            }
            EnemyState.SPECIAL -> {
                // forge slam - lava eruption
                if (stateTime>0.8f && stateTime<1.1f) {
                    if (dist < 300f && !player.isInvulnerable()) {
                        player.takeDamage((damage*1.4f).toInt(), Vec2(facing*120f, -260f), this)
                    }
                }
                if (stateTime>1.6f) state = EnemyState.CHASE
            }
            EnemyState.HURT -> {
                if (hurtTimer<=0f) state = EnemyState.CHASE
            }
            else -> {}
        }
    }
}

// Helper to set player ref for bosses
var Boss.playerRefGeneric: Player?
    get() = when(this){
        is WardenOfAsh -> this.playerRef
        is MatriarchOfRoots -> this.playerRef
        is ForgeheartColossus -> this.playerRef
        else -> null
    }
    set(v){
        when(this){
            is WardenOfAsh -> this.playerRef = v
            is MatriarchOfRoots -> this.playerRef = v
            is ForgeheartColossus -> this.playerRef = v
        }
    }
