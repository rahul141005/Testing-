package com.lumenfall.echoes.world

import com.lumenfall.echoes.game.BiomeType
import com.lumenfall.echoes.utils.Rect

enum class RoomType {
    COMBAT, TRAVERSAL, TREASURE, SECRET, SHOP, NPC, ELITE, CHALLENGE, TRANSITION, BOSS, START
}

data class EnemySpawn(
    val archetype: com.lumenfall.echoes.game.EnemyArchetype,
    val x: Float,
    val y: Float,
    val isElite: Boolean = false
)

data class LootSpawn(
    val x: Float,
    val y: Float,
    val type: String, // coin, weapon, ability, heal
    val rarity: com.lumenfall.echoes.game.Rarity = com.lumenfall.echoes.game.Rarity.COMMON
)

class Room(
    val id: String,
    val type: RoomType,
    val biome: BiomeType,
    val templateId: String,
    val widthTiles: Int,
    val heightTiles: Int
) {
    val tileMap = TileMap(widthTiles, heightTiles)
    val enemySpawns = mutableListOf<EnemySpawn>()
    val lootSpawns = mutableListOf<LootSpawn>()
    val platforms = mutableListOf<Rect>()
    var nextRoomId: String? = null
    var prevRoomId: String? = null
    var isCleared = false
    var isDiscovered = false
    var exitX = 0f
    var exitY = 0f
    var entryX = 0f
    var entryY = 0f

    fun worldToTileX(worldX: Float) = (worldX / com.lumenfall.echoes.game.Constants.TILE_SIZE).toInt()
    fun worldToTileY(worldY: Float) = (worldY / com.lumenfall.echoes.game.Constants.TILE_SIZE).toInt()
}

class RoomTemplate(
    val id: String,
    val type: RoomType,
    val width: Int,
    val height: Int,
    val pattern: List<String>, // ascii pattern
    val enemyMarkers: Map<Char, com.lumenfall.echoes.game.EnemyArchetype>,
    val difficulty: Int
) {
    fun instantiate(biome: BiomeType, rng: com.lumenfall.echoes.utils.SeededRandom): Room {
        val room = Room(
            id = "${id}_${rng.nextInt(100000)}",
            type = type,
            biome = biome,
            templateId = id,
            widthTiles = width,
            heightTiles = height
        )
        // parse pattern
        for (y in pattern.indices) {
            val row = pattern[y]
            for (x in row.indices) {
                val c = row[x]
                when(c) {
                    '#' -> room.tileMap.set(x,y, TileType.SOLID)
                    '=' -> room.tileMap.set(x,y, TileType.PLATFORM)
                    '^' -> room.tileMap.set(x,y, TileType.SPIKE)
                    'E' -> {
                        // enemy spawn - random archetype based on biome
                        val arch = pickEnemyForBiome(biome, rng)
                        room.enemySpawns.add(EnemySpawn(arch, x*32f, y*32f))
                    }
                    'S' -> room.entryX = x*32f
                    'X' -> {
                        room.exitX = x*32f
                        room.exitY = y*32f
                    }
                    'C' -> room.lootSpawns.add(LootSpawn(x*32f, y*32f, "coin"))
                    'W' -> room.lootSpawns.add(LootSpawn(x*32f, y*32f, "weapon"))
                    'A' -> room.lootSpawns.add(LootSpawn(x*32f, y*32f, "ability"))
                    'H' -> room.lootSpawns.add(LootSpawn(x*32f, y*32f, "heal"))
                    else -> {
                        if (enemyMarkers.containsKey(c)) {
                            room.enemySpawns.add(EnemySpawn(enemyMarkers[c]!!, x*32f, y*32f))
                        }
                    }
                }
            }
        }
        // ensure entry/exit
        if (room.entryX == 0f) room.entryX = 64f
        if (room.exitX == 0f) room.exitX = (width-3)*32f
        room.tileMap.rebuildCollision()
        return room
    }

    private fun pickEnemyForBiome(biome: BiomeType, rng: com.lumenfall.echoes.utils.SeededRandom): com.lumenfall.echoes.game.EnemyArchetype {
        val pool = when(biome){
            BiomeType.ASHEN_RAMPARTS -> listOf(
                com.lumenfall.echoes.game.EnemyArchetype.SHAMBLER_HUSK,
                com.lumenfall.echoes.game.EnemyArchetype.AEGIS_REMNANT,
                com.lumenfall.echoes.game.EnemyArchetype.GLOOM_MOTH
            )
            BiomeType.GLOOMROOT_WARREN -> listOf(
                com.lumenfall.echoes.game.EnemyArchetype.THORNLING,
                com.lumenfall.echoes.game.EnemyArchetype.GLOOM_MOTH,
                com.lumenfall.echoes.game.EnemyArchetype.SHAMBLER_HUSK,
                com.lumenfall.echoes.game.EnemyArchetype.RIFT_STALKER
            )
            BiomeType.EMBERWORKS_FOUNDRY -> listOf(
                com.lumenfall.echoes.game.EnemyArchetype.CINDER_CHARGER,
                com.lumenfall.echoes.game.EnemyArchetype.AEGIS_REMNANT,
                com.lumenfall.echoes.game.EnemyArchetype.SPIRE_WARDEN
            )
            BiomeType.LUMEN_ARCHIVE -> listOf(
                com.lumenfall.echoes.game.EnemyArchetype.RIFT_STALKER,
                com.lumenfall.echoes.game.EnemyArchetype.SORROW_SOWER,
                com.lumenfall.echoes.game.EnemyArchetype.AEGIS_REMNANT
            )
            BiomeType.VOIDSPIRE_APEX -> listOf(
                com.lumenfall.echoes.game.EnemyArchetype.RIFT_STALKER,
                com.lumenfall.echoes.game.EnemyArchetype.SORROW_SOWER,
                com.lumenfall.echoes.game.EnemyArchetype.SPIRE_WARDEN,
                com.lumenfall.echoes.game.EnemyArchetype.CINDER_CHARGER
            )
        }
        return pool[rng.nextInt(pool.size)]
    }
}

object RoomTemplateLibrary {
    // Handcrafted modular room pieces - original designs
    val templates = listOf(
        // START
        RoomTemplate("start_01", RoomType.START, 40, 22, listOf(
            "########################################",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#   S                                  #",
            "#  ===                                 #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                 X    #",
            "#                                ===   #",
            "#                                      #",
            "########################################"
        ), emptyMap(), 0),

        // COMBAT
        RoomTemplate("combat_01", RoomType.COMBAT, 40, 22, listOf(
            "########################################",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#      E          E                    #",
            "#     ===       ===                    #",
            "#                                      #",
            "#                                      #",
            "#  E              C          E         #",
            "# ===            ===        ===        #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#   S                              X   #",
            "#  ===            ===            ===   #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "########################################"
        ), emptyMap(), 1),

        RoomTemplate("combat_02", RoomType.COMBAT, 40, 22, listOf(
            "########################################",
            "#                                      #",
            "#                                      #",
            "#       E                              #",
            "#      ===                             #",
            "#                                      #",
            "#                                      #",
            "#  E                         E         #",
            "# ===                       ===        #",
            "#                                      #",
            "#                                      #",
            "#            E                         #",
            "#           ===                        #",
            "#                                      #",
            "#   S                              X   #",
            "#  ===      ===      ===          ===  #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "########################################"
        ), emptyMap(), 1),

        RoomTemplate("combat_vertical", RoomType.COMBAT, 30, 22, listOf(
            "##############################",
            "#                            #",
            "#   E                        #",
            "#  ===                       #",
            "#                            #",
            "#               E            #",
            "#              ===           #",
            "#                            #",
            "#  E                         #",
            "# ===                        #",
            "#                            #",
            "#                  E         #",
            "#                 ===        #",
            "#                            #",
            "#   S                    X   #",
            "#  ===                  ===  #",
            "#                            #",
            "#         ===                #",
            "#                            #",
            "#                            #",
            "#                            #",
            "##############################"
        ), emptyMap(), 2),

        // TREASURE
        RoomTemplate("treasure_01", RoomType.TREASURE, 40, 22, listOf(
            "########################################",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                 W                    #",
            "#                ===                   #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#   S                              X   #",
            "#  ===                            ===  #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "########################################"
        ), emptyMap(), 0),

        // ELITE
        RoomTemplate("elite_01", RoomType.ELITE, 40, 22, listOf(
            "########################################",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                 E                    #",
            "#                ===                   #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#   S                              X   #",
            "#  ===                            ===  #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "########################################"
        ), mapOf('E' to com.lumenfall.echoes.game.EnemyArchetype.ELITE), 3),

        // BOSS
        RoomTemplate("boss_01", RoomType.BOSS, 50, 22, listOf(
            "##################################################",
            "#                                                #",
            "#                                                #",
            "#                                                #",
            "#                                                #",
            "#                                                #",
            "#                                                #",
            "#                                                #",
            "#                                                #",
            "#                                                #",
            "#                                                #",
            "#                                                #",
            "#                                                #",
            "#                                                #",
            "#   S                                        X   #",
            "#  ===                                      ===  #",
            "#                                                #",
            "#                                                #",
            "#                                                #",
            "#                                                #",
            "#                                                #",
            "##################################################"
        ), emptyMap(), 5),

        // TRANSITION
        RoomTemplate("transition_01", RoomType.TRANSITION, 40, 22, listOf(
            "########################################",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#   S                              X   #",
            "#  ===                            ===  #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "########################################"
        ), emptyMap(), 0),

        // SHOP
        RoomTemplate("shop_01", RoomType.SHOP, 40, 22, listOf(
            "########################################",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#          W     A     H               #",
            "#         ===   ===   ===              #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#   S                              X   #",
            "#  ===                            ===  #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "########################################"
        ), emptyMap(), 0)
    )

    fun getForType(type: RoomType): List<RoomTemplate> = templates.filter { it.type == type }
    fun getRandomForType(type: RoomType, rng: com.lumenfall.echoes.utils.SeededRandom): RoomTemplate {
        val list = getForType(type)
        return if (list.isEmpty()) templates[0] else list[rng.nextInt(list.size)]
    }
}
