package com.lumenfall.echoes.game

import com.lumenfall.echoes.entities.Entity
import com.lumenfall.echoes.utils.Rect
import com.lumenfall.echoes.world.Room

/**
 * Physics system - handles gravity, collision resolution, and world bounds.
 * Separated for clean architecture and testability.
 */
object PhysicsSystem {

    fun applyGravity(entity: Entity, dt: Float, gravity: Float = Constants.PLAYER_GRAVITY) {
        if (!entity.grounded) {
            entity.vel.y += gravity * dt
            if (entity.vel.y > Constants.PLAYER_MAX_FALL) entity.vel.y = Constants.PLAYER_MAX_FALL
        }
    }

    fun resolveTileCollisionX(entity: Entity, room: Room): Boolean {
        entity.pos.x += entity.vel.x * Constants.FIXED_DT
        for (solid in room.tileMap.solids) {
            if (entity.bounds.intersects(solid)) {
                if (entity.vel.x > 0) entity.pos.x = solid.left - entity.width
                else if (entity.vel.x < 0) entity.pos.x = solid.right
                entity.vel.x = 0f
                return true
            }
        }
        return false
    }

    fun resolveTileCollisionY(entity: Entity, room: Room): Boolean {
        entity.pos.y += entity.vel.y * Constants.FIXED_DT
        entity.grounded = false
        for (solid in room.tileMap.solids) {
            if (entity.bounds.intersects(solid)) {
                if (entity.vel.y > 0) {
                    entity.pos.y = solid.top - entity.height
                    entity.vel.y = 0f
                    entity.grounded = true
                } else if (entity.vel.y < 0) {
                    entity.pos.y = solid.bottom
                    entity.vel.y = 0f
                }
                return true
            }
        }
        return false
    }

    fun checkWorldBounds(entity: Entity, room: Room): Boolean {
        if (entity.pos.y > room.heightTiles * Constants.TILE_SIZE + 200f) return true // fell out
        if (entity.pos.x < 0f) entity.pos.x = 0f
        if (entity.pos.x > room.widthTiles * Constants.TILE_SIZE - entity.width) {
            entity.pos.x = room.widthTiles * Constants.TILE_SIZE - entity.width
        }
        return false
    }

    fun sweptCollision(moving: Rect, vel: com.lumenfall.echoes.utils.Vec2, static: Rect, dt: Float): Float? {
        return com.lumenfall.echoes.utils.Collision.sweptAABB(moving, vel, static, dt)
    }
}
