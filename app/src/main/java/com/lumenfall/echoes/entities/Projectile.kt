package com.lumenfall.echoes.entities

import com.lumenfall.echoes.game.DamageType
import com.lumenfall.echoes.utils.Vec2

enum class ProjectileType {
    ARROW, BOLT, EMBER, SHARD, THORN_SPIT, SORROW_ORB, VINE_LASH
}

class Projectile : Entity() {
    var type = ProjectileType.ARROW
    var damage = 10
    var damageType = DamageType.PHYSICAL
    var fromPlayer = true
    var life = 3f
    var maxLife = 3f
    var pierce = 0
    var hasHit = false
    var gravity = 0f
    var homing = false
    var homingTarget: Entity? = null
    var trailTimer = 0f

    init {
        width = 12f
        height = 6f
    }

    fun launch(x: Float, y: Float, vx: Float, vy: Float, dmg: Int, dmgType: DamageType, player: Boolean, projType: ProjectileType) {
        pos.set(x,y)
        vel.set(vx,vy)
        damage = dmg
        damageType = dmgType
        fromPlayer = player
        type = projType
        life = maxLife
        active = true
        hasHit = false
        facing = if (vx>0) 1 else -1
        width = when(projType){
            ProjectileType.ARROW -> 18f
            ProjectileType.BOLT -> 22f
            ProjectileType.EMBER -> 10f
            ProjectileType.SHARD -> 12f
            ProjectileType.THORN_SPIT -> 14f
            ProjectileType.SORROW_ORB -> 16f
            ProjectileType.VINE_LASH -> 20f
        }
        height = width*0.6f
        gravity = when(projType){
            ProjectileType.ARROW -> 420f
            ProjectileType.BOLT -> 120f
            ProjectileType.EMBER -> 80f
            else -> 0f
        }
        pierce = when(projType){
            ProjectileType.BOLT -> 2
            ProjectileType.SHARD -> 1
            else -> 0
        }
        homing = projType == ProjectileType.SHARD
    }

    override fun update(dt: Float) {
        if (!active) return
        life -= dt
        if (life <= 0f) { active=false; return }

        if (gravity != 0f) {
            vel.y += gravity * dt
        }

        if (homing && homingTarget != null && homingTarget!!.active) {
            val target = homingTarget!!
            val dx = target.center.x - center.x
            val dy = target.center.y - center.y
            val dist = kotlin.math.sqrt(dx*dx + dy*dy)
            if (dist < 300f && dist > 10f) {
                val steer = 320f * dt
                vel.x += (dx/dist)*steer
                vel.y += (dy/dist)*steer
                // clamp speed
                val speed = vel.length()
                val maxSpeed = 360f
                if (speed > maxSpeed) {
                    vel.mul(maxSpeed/speed)
                }
            }
        }

        pos.x += vel.x * dt
        pos.y += vel.y * dt
        trailTimer += dt
    }

    fun onHit() {
        if (pierce > 0) {
            pierce--
        } else {
            active = false
            hasHit = true
        }
    }

    override fun reset() {
        super.reset()
        active = false
        hasHit = false
        life = maxLife
        homingTarget = null
    }
}
