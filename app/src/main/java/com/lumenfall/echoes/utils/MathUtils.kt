package com.lumenfall.echoes.utils

import kotlin.math.*

data class Vec2(var x: Float = 0f, var y: Float = 0f) {
    fun set(nx: Float, ny: Float) { x = nx; y = ny }
    fun set(other: Vec2) { x = other.x; y = other.y }
    fun add(other: Vec2) { x += other.x; y += other.y }
    fun add(ax: Float, ay: Float) { x += ax; y += ay }
    fun mul(s: Float) { x *= s; y *= s }
    fun length() = sqrt(x*x + y*y)
    fun lengthSq() = x*x + y*y
    fun normalize(): Vec2 {
        val len = length()
        if (len > 0.0001f) { x /= len; y /= len }
        return this
    }
    fun normalized(): Vec2 {
        val len = length()
        return if (len > 0.0001f) Vec2(x/len, y/len) else Vec2(0f,0f)
    }
    fun distance(other: Vec2) = sqrt((x-other.x)*(x-other.x) + (y-other.y)*(y-other.y))
    fun dot(other: Vec2) = x*other.x + y*other.y
    fun clone() = Vec2(x,y)
    companion object {
        fun lerp(a: Vec2, b: Vec2, t: Float) = Vec2(
            a.x + (b.x - a.x)*t,
            a.y + (b.y - a.y)*t
        )
    }
}

data class Rect(var x: Float, var y: Float, var w: Float, var h: Float) {
    val left get() = x
    val right get() = x + w
    val top get() = y
    val bottom get() = y + h
    val centerX get() = x + w*0.5f
    val centerY get() = y + h*0.5f

    fun intersects(other: Rect): Boolean {
        return !(right < other.left || left > other.right || bottom < other.top || top > other.bottom)
    }
    fun contains(px: Float, py: Float) = px >= left && px <= right && py >= top && py <= bottom
    fun overlapsCircle(cx: Float, cy: Float, r: Float): Boolean {
        val closestX = cx.coerceIn(left, right)
        val closestY = cy.coerceIn(top, bottom)
        val dx = cx - closestX
        val dy = cy - closestY
        return dx*dx + dy*dy <= r*r
    }
}

object Easing {
    fun easeOutQuad(t: Float): Float = 1f - (1f - t)*(1f - t)
    fun easeInQuad(t: Float): Float = t*t
    fun easeInOutCubic(t: Float): Float {
        return if (t < 0.5f) 4f*t*t*t else 1f - (-2f*t + 2f).pow(3f)/2f
    }
    fun easeOutElastic(t: Float): Float {
        val c4 = (2f * PI.toFloat()) / 3f
        return if (t == 0f) 0f else if (t == 1f) 1f else (2f.pow(-10f*t) * sin((t*10f - 0.75f)*c4) + 1f)
    }
}

object Collision {
    fun aabbOverlap(ax: Float, ay: Float, aw: Float, ah: Float,
                    bx: Float, by: Float, bw: Float, bh: Float): Boolean {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by
    }
    fun sweptAABB(moving: Rect, vel: Vec2, static: Rect, dt: Float): Float? {
        // Simple swept check returning time of impact [0,1] or null
        val dx = vel.x * dt
        val dy = vel.y * dt
        if (dx == 0f && dy == 0f) return if (moving.intersects(static)) 0f else null

        var xEntry: Float
        var yEntry: Float
        var xExit: Float
        var yExit: Float

        if (dx > 0f) {
            xEntry = static.left - moving.right
            xExit = static.right - moving.left
        } else {
            xEntry = static.right - moving.left
            xExit = static.left - moving.right
        }
        if (dy > 0f) {
            yEntry = static.top - moving.bottom
            yExit = static.bottom - moving.top
        } else {
            yEntry = static.bottom - moving.top
            yExit = static.top - moving.bottom
        }

        val txEntry = if (dx == 0f) Float.NEGATIVE_INFINITY else xEntry / dx
        val txExit = if (dx == 0f) Float.POSITIVE_INFINITY else xExit / dx
        val tyEntry = if (dy == 0f) Float.NEGATIVE_INFINITY else yEntry / dy
        val tyExit = if (dy == 0f) Float.POSITIVE_INFINITY else yExit / dy

        val entryTime = max(txEntry, tyEntry)
        val exitTime = min(txExit, tyExit)

        if (entryTime > exitTime) return null
        if (txEntry < 0f && tyEntry < 0f) return null
        if (entryTime < 0f || entryTime > 1f) return null
        return entryTime
    }
}
