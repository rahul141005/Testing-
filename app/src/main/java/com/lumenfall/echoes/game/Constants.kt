package com.lumenfall.echoes.game

object Constants {
    const val TARGET_FPS = 60
    const val FRAME_TIME_MS = 1000L / TARGET_FPS
    const val FIXED_DT = 1f / TARGET_FPS

    // World
    const val TILE_SIZE = 32
    const val ROOM_WIDTH_TILES = 40
    const val ROOM_HEIGHT_TILES = 22
    const val ROOM_WIDTH_PX = ROOM_WIDTH_TILES * TILE_SIZE
    const val ROOM_HEIGHT_PX = ROOM_HEIGHT_TILES * TILE_SIZE

    // Player physics
    const val PLAYER_WIDTH = 28
    const val PLAYER_HEIGHT = 44
    const val PLAYER_SPEED = 280f
    const val PLAYER_RUN_ACCEL = 2200f
    const val PLAYER_AIR_ACCEL = 1200f
    const val PLAYER_FRICTION = 1800f
    const val PLAYER_GRAVITY = 1500f
    const val PLAYER_MAX_FALL = 680f
    const val PLAYER_JUMP_FORCE = 520f
    const val PLAYER_DOUBLE_JUMP_FORCE = 460f
    const val PLAYER_JUMP_CUT_MULT = 0.35f
    const val PLAYER_COYOTE_TIME = 0.15f
    const val PLAYER_JUMP_BUFFER = 0.18f
    const val PLAYER_DASH_SPEED = 620f
    const val PLAYER_DASH_DURATION = 0.22f
    const val PLAYER_DASH_COOLDOWN = 0.55f
    const val PLAYER_IFRAME_DURATION = 0.35f
    const val PLAYER_WALL_SLIDE_SPEED = 120f

    // Combat
    const val HIT_STOP_LIGHT = 0.04f
    const val HIT_STOP_HEAVY = 0.10f
    const val CAM_SHAKE_LIGHT = 3f
    const val CAM_SHAKE_HEAVY = 8f

    // Enemy
    const val ENEMY_DETECTION_RANGE = 380f
    const val ENEMY_ATTACK_RANGE_MELEE = 56f

    // World gen
    const val ROOMS_PER_BIOME_MIN = 8
    const val ROOMS_PER_BIOME_MAX = 14

    // Rendering
    const val MAX_PARTICLES = 400
    const val MAX_PROJECTILES = 64
    const val MAX_ENEMIES_PER_ROOM = 8
}

enum class GameState {
    MAIN_MENU,
    PLAYING,
    PAUSED,
    DEATH,
    VICTORY,
    BIOME_TRANSITION,
    SHOP,
    INVENTORY
}

enum class BiomeType {
    ASHEN_RAMPARTS,
    GLOOMROOT_WARREN,
    EMBERWORKS_FOUNDRY,
    LUMEN_ARCHIVE,
    VOIDSPIRE_APEX
}

enum class DamageType {
    PHYSICAL, FIRE, FROST, VOLT, VOID, TRUE_DAMAGE
}

enum class WeaponCategory {
    SWORD, HEAVY, SPEAR, DUAL, AXE, UNCONVENTIONAL,
    BOW, CROSSBOW, EMBERHAND, THROWABLE
}

enum class Rarity {
    COMMON, UNCOMMON, RARE, EPIC, LEGENDARY, MYTHIC
}

enum class EnemyArchetype {
    SHAMBLER_HUSK,
    THORNLING,
    GLOOM_MOTH,
    AEGIS_REMNANT,
    CINDER_CHARGER,
    RIFT_STALKER,
    SORROW_SOWER,
    SPIRE_WARDEN,
    ELITE,
    BOSS
}
