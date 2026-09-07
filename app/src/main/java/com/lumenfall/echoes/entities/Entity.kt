package com.lumenfall.echoes.entities

import com.lumenfall.echoes.utils.Rect
import com.lumenfall.echoes.utils.Vec2

abstract class Entity {
    val pos = Vec2()
    val vel = Vec2()
    var width = 32f
    var height = 32f
    var active = true
    var grounded = false
    var facing = 1 // 1 right, -1 left

    val bounds: Rect get() = Rect(pos.x, pos.y, width, height)
    val center: Vec2 get() = Vec2(pos.x + width*0.5f, pos.y + height*0.5f)

    open fun update(dt: Float) {}
    open fun onCollision(other: Entity) {}
    open fun takeDamage(amount: Int, knockback: Vec2, from: Entity?) {}
    open fun reset() {
        pos.set(0f,0f)
        vel.set(0f,0f)
        active = true
        grounded = false
    }
}

interface Damageable {
    var hp: Int
    var maxHp: Int
    fun isAlive(): Boolean
    fun damage(amount: Int): Boolean
}
