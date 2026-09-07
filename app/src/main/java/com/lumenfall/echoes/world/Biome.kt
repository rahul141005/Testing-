package com.lumenfall.echoes.world

import com.lumenfall.echoes.game.BiomeType

data class BiomeDef(
    val type: BiomeType,
    val name: String,
    val description: String,
    val colorPrimary: Int,
    val colorSecondary: Int,
    val colorBackground: Int,
    val enemyPool: List<com.lumenfall.echoes.game.EnemyArchetype>,
    val bossType: com.lumenfall.echoes.entities.BossType?,
    val musicTrack: String,
    val difficulty: Int
)

object BiomeRegistry {
    val biomes = listOf(
        BiomeDef(
            type = BiomeType.ASHEN_RAMPARTS,
            name = "Ashen Ramparts",
            description = "The outer walls where the Citadel first cracked. Wind carries ash of forgotten banners.",
            colorPrimary = 0xFF8A8A8A.toInt(),
            colorSecondary = 0xFFD4A373.toInt(),
            colorBackground = 0xFF2B2B2B.toInt(),
            enemyPool = listOf(
                com.lumenfall.echoes.game.EnemyArchetype.SHAMBLER_HUSK,
                com.lumenfall.echoes.game.EnemyArchetype.AEGIS_REMNANT,
                com.lumenfall.echoes.game.EnemyArchetype.GLOOM_MOTH
            ),
            bossType = com.lumenfall.echoes.entities.BossType.WARDEN_OF_ASH,
            musicTrack = "ashen_winds",
            difficulty = 1
        ),
        BiomeDef(
            type = BiomeType.GLOOMROOT_WARREN,
            name = "Gloomroot Warren",
            description = "Living roots have torn through stone, forming a labyrinth that breathes.",
            colorPrimary = 0xFF2D4A22.toInt(),
            colorSecondary = 0xFF7FB069.toInt(),
            colorBackground = 0xFF1A2A1A.toInt(),
            enemyPool = listOf(
                com.lumenfall.echoes.game.EnemyArchetype.THORNLING,
                com.lumenfall.echoes.game.EnemyArchetype.GLOOM_MOTH,
                com.lumenfall.echoes.game.EnemyArchetype.RIFT_STALKER
            ),
            bossType = com.lumenfall.echoes.entities.BossType.MATRIARCH_OF_ROOTS,
            musicTrack = "gloomroot_pulse",
            difficulty = 2
        ),
        BiomeDef(
            type = BiomeType.EMBERWORKS_FOUNDRY,
            name = "Emberworks Foundry",
            description = "Ancient forges still burn, hammering weapons for an army that never came.",
            colorPrimary = 0xFF8B2500.toInt(),
            colorSecondary = 0xFFFF6B35.toInt(),
            colorBackground = 0xFF2A1A0F.toInt(),
            enemyPool = listOf(
                com.lumenfall.echoes.game.EnemyArchetype.CINDER_CHARGER,
                com.lumenfall.echoes.game.EnemyArchetype.SPIRE_WARDEN,
                com.lumenfall.echoes.game.EnemyArchetype.AEGIS_REMNANT
            ),
            bossType = com.lumenfall.echoes.entities.BossType.FORGEHEART_COLOSSUS,
            musicTrack = "emberworks_hammer",
            difficulty = 3
        ),
        BiomeDef(
            type = BiomeType.LUMEN_ARCHIVE,
            name = "Lumen Archive",
            description = "Floating shelves hold light itself as books. Knowledge is guarded jealously.",
            colorPrimary = 0xFF3A6EA5.toInt(),
            colorSecondary = 0xFF7DE2FF.toInt(),
            colorBackground = 0xFF121E2A.toInt(),
            enemyPool = listOf(
                com.lumenfall.echoes.game.EnemyArchetype.RIFT_STALKER,
                com.lumenfall.echoes.game.EnemyArchetype.SORROW_SOWER,
                com.lumenfall.echoes.game.EnemyArchetype.AEGIS_REMNANT
            ),
            bossType = com.lumenfall.echoes.entities.BossType.LUMEN_PARAGON,
            musicTrack = "lumen_archive",
            difficulty = 4
        ),
        BiomeDef(
            type = BiomeType.VOIDSPIRE_APEX,
            name = "Voidspire Apex",
            description = "At the Citadel's peak, reality frays. The Unraveling waits.",
            colorPrimary = 0xFF4A2A6A.toInt(),
            colorSecondary = 0xFFB07FFF.toInt(),
            colorBackground = 0xFF1A0F2A.toInt(),
            enemyPool = listOf(
                com.lumenfall.echoes.game.EnemyArchetype.SPIRE_WARDEN,
                com.lumenfall.echoes.game.EnemyArchetype.SORROW_SOWER,
                com.lumenfall.echoes.game.EnemyArchetype.RIFT_STALKER,
                com.lumenfall.echoes.game.EnemyArchetype.CINDER_CHARGER
            ),
            bossType = com.lumenfall.echoes.entities.BossType.THE_UNRAVELING,
            musicTrack = "voidspire",
            difficulty = 5
        )
    )

    fun get(type: BiomeType) = biomes.find { it.type == type } ?: biomes[0]
    fun getOrdered(): List<BiomeDef> = biomes.sortedBy { it.difficulty }
}
