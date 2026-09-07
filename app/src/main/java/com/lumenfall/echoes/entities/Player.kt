package com.lumenfall.echoes.entities

import com.lumenfall.echoes.combat.*
import com.lumenfall.echoes.game.Constants
import com.lumenfall.echoes.game.DamageType
import com.lumenfall.echoes.utils.Rect
import com.lumenfall.echoes.utils.Vec2
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

enum class PlayerState {
    IDLE, RUN, JUMP, FALL, DASH, ATTACK, HURT, DEAD, WALL_SLIDE, CROUCH
}

class Player : Entity() {
    var maxHp = 100
    var hp = 100
    var maxMana = 100f
    var mana = 100f
    var coins = 0
    var lumenShards = 0 // permanent currency

    var state = PlayerState.IDLE
    var stateTime = 0f

    // Movement
    var inputX = 0f
    var wantJump = false
    var wantDash = false
    var wantAttack = false
    var wantAbility1 = false
    var wantAbility2 = false
    var wantRanged = false
    var jumpHeld = false

    var coyoteTimer = 0f
    var jumpBufferTimer = 0f
    var dashTimer = 0f
    var dashCooldown = 0f
    var iFrameTimer = 0f
    var attackLockTimer = 0f
    var hasDoubleJump = true
    var isWallSliding = false
    var wallDir = 0 // -1 left wall, 1 right wall

    // Combat
    var primaryWeapon: Weapon = com.lumenfall.echoes.combat.Dawnblade()
    var secondaryWeapon: Weapon? = null
    var rangedWeapon: Weapon = com.lumenfall.echoes.combat.Aetherbow()
    var ability1: Ability = EmberTrap()
    var ability2: Ability = PhaseDash()

    var currentWeapon: Weapon
        get() = primaryWeapon
        set(v) { primaryWeapon = v }

    var comboDamageMult = 1f
    var elementalSynergy = mutableMapOf<DamageType, Float>()

    var onGroundLastFrame = false
    var wasGrounded = false

    var activeHitboxes = mutableListOf<HitboxInstance>()
    var damageEvents = mutableListOf<DamageEvent>()

    data class HitboxInstance(
        val rect: Rect,
        val damage: Int,
        val knockback: Vec2,
        val type: DamageType,
        val isCrit: Boolean,
        var life: Float = 0.12f
    )

    init {
        width = Constants.PLAYER_WIDTH.toFloat()
        height = Constants.PLAYER_HEIGHT.toFloat()
    }

    override fun reset() {
        super.reset()
        hp = maxHp
        mana = maxMana
        state = PlayerState.IDLE
        stateTime = 0f
        vel.set(0f,0f)
        coyoteTimer = 0f
        jumpBufferTimer = 0f
        dashTimer = 0f
        dashCooldown = 0f
        iFrameTimer = 0f
        hasDoubleJump = true
        activeHitboxes.clear()
        primaryWeapon.reset()
        ability1.reset()
        ability2.reset()
    }

    fun setInput(moveX: Float, jump: Boolean, dash: Boolean, attack: Boolean, ability1In: Boolean, ability2In: Boolean, ranged: Boolean, jumpHeldIn: Boolean) {
        inputX = moveX.coerceIn(-1f,1f)
        if (jump) jumpBufferTimer = Constants.PLAYER_JUMP_BUFFER
        wantDash = dash
        wantAttack = attack
        wantAbility1 = ability1In
        wantAbility2 = ability2In
        wantRanged = ranged
        jumpHeld = jumpHeldIn
        if (moveX > 0.1f) facing = 1 else if (moveX < -0.1f) facing = -1
    }

    override fun update(dt: Float) {
        if (hp <= 0) {
            state = PlayerState.DEAD
            return
        }

        // Timers
        stateTime += dt
        if (coyoteTimer > 0f) coyoteTimer -= dt
        if (jumpBufferTimer > 0f) jumpBufferTimer -= dt
        if (dashCooldown > 0f) dashCooldown -= dt
        if (iFrameTimer > 0f) iFrameTimer -= dt
        if (attackLockTimer > 0f) attackLockTimer -= dt

        // Ground check for coyote
        if (grounded) {
            coyoteTimer = Constants.PLAYER_COYOTE_TIME
            hasDoubleJump = true
            if (!onGroundLastFrame) {
                // landed
            }
        }
        onGroundLastFrame = grounded

        // Weapon & abilities update
        primaryWeapon.update(dt)
        secondaryWeapon?.update(dt)
        rangedWeapon.update(dt)
        ability1.update(dt)
        ability2.update(dt)

        // Hitbox lifetime
        val it = activeHitboxes.iterator()
        while (it.hasNext()) {
            val hb = it.next()
            hb.life -= dt
            if (hb.life <= 0f) it.remove()
        }

        // State machine
        when(state) {
            PlayerState.DASH -> updateDash(dt)
            PlayerState.HURT -> updateHurt(dt)
            else -> {
                updateMovement(dt)
                updateCombat(dt)
            }
        }

        // Apply gravity
        if (state != PlayerState.DASH) {
            if (!grounded) {
                vel.y += Constants.PLAYER_GRAVITY * dt
                if (vel.y > Constants.PLAYER_MAX_FALL) vel.y = Constants.PLAYER_MAX_FALL
                // Wall slide
                if (isWallSliding && vel.y > Constants.PLAYER_WALL_SLIDE_SPEED) {
                    vel.y = Constants.PLAYER_WALL_SLIDE_SPEED
                }
            }
        }

        // Jump cut
        if (!jumpHeld && vel.y < 0f && state == PlayerState.JUMP) {
            vel.y *= Constants.PLAYER_JUMP_CUT_MULT
        }

        // Update facing from input if not attacking locked
        if (attackLockTimer <= 0f && abs(inputX) > 0.1f) {
            facing = if (inputX > 0) 1 else -1
        }

        // Update state based on physics
        if (state != PlayerState.DASH && state != PlayerState.ATTACK && state != PlayerState.HURT) {
            if (!grounded) {
                if (isWallSliding) state = PlayerState.WALL_SLIDE
                else if (vel.y < 0f) state = PlayerState.JUMP
                else state = PlayerState.FALL
            } else {
                if (abs(inputX) > 0.1f) state = PlayerState.RUN else state = PlayerState.IDLE
            }
        }
    }

    private fun updateMovement(dt: Float) {
        // Horizontal movement with accel
        val targetSpeed = inputX * Constants.PLAYER_SPEED
        val accel = if (grounded) Constants.PLAYER_RUN_ACCEL else Constants.PLAYER_AIR_ACCEL
        if (abs(inputX) > 0.1f) {
            // accelerate towards target
            if (vel.x < targetSpeed) {
                vel.x = min(targetSpeed, vel.x + accel*dt)
            } else if (vel.x > targetSpeed) {
                vel.x = max(targetSpeed, vel.x - accel*dt)
            }
        } else {
            // friction
            if (abs(vel.x) > 10f) {
                val friction = Constants.PLAYER_FRICTION * dt
                if (vel.x > 0f) vel.x = max(0f, vel.x - friction) else vel.x = min(0f, vel.x + friction)
            } else {
                vel.x = 0f
            }
        }

        // Jump
        if (jumpBufferTimer > 0f) {
            if (coyoteTimer > 0f || grounded) {
                // normal jump
                vel.y = -Constants.PLAYER_JUMP_FORCE
                grounded = false
                coyoteTimer = 0f
                jumpBufferTimer = 0f
                state = PlayerState.JUMP
                stateTime = 0f
            } else if (hasDoubleJump && !isWallSliding) {
                vel.y = -Constants.PLAYER_DOUBLE_JUMP_FORCE
                hasDoubleJump = false
                jumpBufferTimer = 0f
                state = PlayerState.JUMP
                stateTime = 0f
            } else if (isWallSliding) {
                // wall jump
                vel.y = -Constants.PLAYER_JUMP_FORCE * 0.9f
                vel.x = -wallDir * Constants.PLAYER_SPEED * 1.2f
                isWallSliding = false
                jumpBufferTimer = 0f
                facing = -wallDir
            }
        }

        // Dash
        if (wantDash && dashCooldown <= 0f && dashTimer <= 0f) {
            startDash()
        }
    }

    private fun startDash() {
        state = PlayerState.DASH
        stateTime = 0f
        dashTimer = Constants.PLAYER_DASH_DURATION
        dashCooldown = Constants.PLAYER_DASH_COOLDOWN
        iFrameTimer = Constants.PLAYER_IFRAME_DURATION
        vel.y = 0f
        vel.x = facing * Constants.PLAYER_DASH_SPEED
        wantDash = false
    }

    private fun updateDash(dt: Float) {
        dashTimer -= dt
        // keep dash velocity
        vel.x = facing * Constants.PLAYER_DASH_SPEED
        vel.y = 0f
        if (dashTimer <= 0f) {
            state = if (grounded) PlayerState.IDLE else PlayerState.FALL
            vel.x *= 0.5f
        }
    }

    private fun updateHurt(dt: Float) {
        // simple hurt stun
        if (stateTime > 0.22f) {
            state = if (grounded) PlayerState.IDLE else PlayerState.FALL
        }
    }

    private fun updateCombat(dt: Float) {
        if (wantAttack && attackLockTimer <= 0f) {
            if (primaryWeapon.canAttack()) {
                primaryWeapon.startAttack()
                state = PlayerState.ATTACK
                stateTime = 0f
                attackLockTimer = primaryWeapon.getAttackDuration(primaryWeapon.comboIndex) + 0.05f
                // generate hitboxes at appropriate time - we generate immediately but with delay logic in renderer
                generateHitboxes()
            }
            wantAttack = false
        }

        // Abilities
        if (wantAbility1 && ability1.canUse()) {
            if (ability1.activate(pos.x + width/2, pos.y + height/2, facing)) {
                // effects handled by game engine
            }
            wantAbility1 = false
        }
        if (wantAbility2 && ability2.canUse()) {
            if (ability2.activate(pos.x + width/2, pos.y + height/2, facing)) {
                if (ability2 is PhaseDash) {
                    // trigger dash-like movement
                    startDash()
                    vel.x = facing * Constants.PLAYER_DASH_SPEED * 1.4f
                }
            }
            wantAbility2 = false
        }
    }

    private fun generateHitboxes() {
        val weapon = primaryWeapon
        val combo = weapon.comboIndex
        val hitDefs = weapon.getHitboxes(combo, facing)
        val baseDamage = weapon.getCurrentDamage()
        val isCrit = Math.random() < weapon.stats.critChance
        val finalDamage = if (isCrit) (baseDamage * weapon.stats.critMultiplier).toInt() else baseDamage

        for (def in hitDefs) {
            val hx = pos.x + def.offsetX + (if (facing<0 && def.offsetX>0) -def.w else 0f)
            // Actually def already accounts for facing
            val rect = Rect(pos.x + def.offsetX, pos.y + def.offsetY, def.w, def.h)
            val kb = Vec2(facing * weapon.stats.knockback, -80f)
            activeHitboxes.add(HitboxInstance(rect, (finalDamage*def.damageMult).toInt(), kb, weapon.damageType, isCrit))
        }
    }

    override fun takeDamage(amount: Int, knockback: Vec2, from: Entity?) {
        if (iFrameTimer > 0f) return
        if (state == PlayerState.DASH) return // invulnerable during dash
        if (ability1 is AetherDome && ability1.isActive) return
        if (ability2 is AetherDome && ability2.isActive) return

        hp -= amount
        if (hp < 0) hp = 0
        vel.x += knockback.x * 0.15f
        vel.y = knockback.y * 0.15f - 80f
        iFrameTimer = Constants.PLAYER_IFRAME_DURATION
        state = PlayerState.HURT
        stateTime = 0f
        if (hp <= 0) {
            state = PlayerState.DEAD
        }
    }

    fun heal(amount: Int) {
        hp = min(maxHp, hp + amount)
    }

    fun isInvulnerable(): Boolean = iFrameTimer > 0f || state == PlayerState.DASH
}
