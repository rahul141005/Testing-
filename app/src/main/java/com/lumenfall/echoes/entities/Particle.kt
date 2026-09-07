package com.lumenfall.echoes.entities

import com.lumenfall.echoes.utils.Vec2
import kotlin.math.cos
import kotlin.math.sin

enum class ParticleType {
    SPARK, BLOOD, EMBER, FROST, VOLT, DUST, SLASH, IMPACT, HEAL, COIN
}

class Particle : Entity() {
    var type = ParticleType.SPARK
    var life = 1f
    var maxLife = 1f
    var size = 4f
    var startSize = 4f
    var endSize = 1f
    var r = 255
    var g = 255
    var b = 255
    var a = 255
    var gravity = 0f
    var drag = 0f
    var rotation = 0f
    var rotSpeed = 0f
    var flicker = false

    fun init(
        x: Float, y: Float,
        vx: Float, vy: Float,
        lifeSec: Float,
        pType: ParticleType,
        color: Int = 0xFFFFFF,
        sSize: Float = 4f,
        eSize: Float = 1f,
        grav: Float = 0f
    ) {
        pos.set(x,y)
        vel.set(vx,vy)
        life = lifeSec
        maxLife = lifeSec
        type = pType
        startSize = sSize
        endSize = eSize
        size = sSize
        gravity = grav
        r = (color shr 16) and 0xFF
        g = (color shr 8) and 0xFF
        b = color and 0xFF
        a = 255
        active = true
        drag = 0.5f
        rotation = 0f
        rotSpeed = (Math.random().toFloat()-0.5f)*360f
    }

    override fun update(dt: Float) {
        if (!active) return
        life -= dt
        if (life <= 0f) { active=false; return }
        vel.y += gravity * dt
        vel.x *= (1f - drag*dt).coerceAtLeast(0f)
        vel.y *= (1f - drag*dt*0.5f).coerceAtLeast(0f)
        pos.x += vel.x * dt
        pos.y += vel.y * dt
        rotation += rotSpeed * dt
        val t = 1f - (life / maxLife)
        size = startSize + (endSize - startSize)*t
        a = ((1f - t)*255).toInt().coerceIn(0,255)
        if (flicker && Math.random()<0.3) a = (a*0.5f).toInt()
    }

    override fun reset() {
        super.reset()
        active = false
        life = 0f
    }

    companion object {
        fun createSparks(x: Float, y: Float, count: Int, dir: Int, list: MutableList<Particle>, pool: com.lumenfall.echoes.utils.ObjectPool<Particle>) {
            repeat(count) {
                val p = pool.obtain()
                val angle = (Math.random()*Math.PI*2).toFloat()
                val speed = (80f + Math.random()*220f).toFloat()
                p.init(
                    x, y,
                    cos(angle)*speed*dir + (Math.random()*60f-30f).toFloat(),
                    sin(angle)*speed + (Math.random()*60f-30f).toFloat(),
                    (0.2f + Math.random()*0.4f).toFloat(),
                    ParticleType.SPARK,
                    0xFFD27D2D + (Math.random()*0x222222).toInt(),
                    3f, 0.5f, 300f
                )
                list.add(p)
            }
        }

        fun createBlood(x: Float, y: Float, count: Int, dir: Int, list: MutableList<Particle>, pool: com.lumenfall.echoes.utils.ObjectPool<Particle>) {
            repeat(count) {
                val p = pool.obtain()
                val angle = (Math.random()*Math.PI - Math.PI*0.5).toFloat()
                val speed = (60f + Math.random()*180f).toFloat()
                p.init(
                    x, y,
                    cos(angle)*speed*dir,
                    sin(angle)*speed - 50f,
                    (0.4f + Math.random()*0.5f).toFloat(),
                    ParticleType.BLOOD,
                    0xC73A3A,
                    4f, 1f, 600f
                )
                list.add(p)
            }
        }
    }
}
