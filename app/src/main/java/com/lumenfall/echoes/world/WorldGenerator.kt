package com.lumenfall.echoes.world

import com.lumenfall.echoes.game.BiomeType
import com.lumenfall.echoes.game.Constants
import com.lumenfall.echoes.utils.SeededRandom

class WorldGenerator(private val rng: SeededRandom) {

    data class BiomeRun(
        val biome: BiomeType,
        val rooms: List<Room>,
        val bossRoom: Room
    )

    fun generateBiomeRun(biome: BiomeType, runSeed: Long, difficulty: Int = 1): BiomeRun {
        rng.setSeed(runSeed + biome.ordinal*1000L)

        val roomCount = rng.nextInt(Constants.ROOMS_PER_BIOME_MIN, Constants.ROOMS_PER_BIOME_MAX)
        val rooms = mutableListOf<Room>()

        // Start room
        val startTemplate = RoomTemplateLibrary.getRandomForType(RoomType.START, rng)
        val startRoom = startTemplate.instantiate(biome, rng)
        rooms.add(startRoom)

        // Middle rooms - mix
        for (i in 1 until roomCount-1) {
            val typeRoll = rng.nextFloat()
            val type = when {
                typeRoll < 0.55f -> RoomType.COMBAT
                typeRoll < 0.70f -> RoomType.TRAVERSAL
                typeRoll < 0.78f -> RoomType.TREASURE
                typeRoll < 0.88f -> RoomType.ELITE
                typeRoll < 0.94f -> RoomType.SHOP
                else -> RoomType.CHALLENGE
            }
            val template = RoomTemplateLibrary.getRandomForType(type, rng)
            val room = template.instantiate(biome, rng)

            // difficulty scaling: more enemies
            if (difficulty > 1) {
                val extra = rng.nextInt(0, difficulty)
                repeat(extra) {
                    if (room.enemySpawns.isNotEmpty()) {
                        val base = room.enemySpawns[rng.nextInt(room.enemySpawns.size)]
                        room.enemySpawns.add(base.copy(x = base.x + rng.nextFloat(-40f,40f)))
                    }
                }
            }

            // elite chance
            if (type == RoomType.ELITE) {
                room.enemySpawns.forEachIndexed { idx, spawn ->
                    if (idx==0) room.enemySpawns[idx] = spawn.copy(isElite = true)
                }
            }

            rooms.add(room)
        }

        // Pre-boss transition
        val transTemplate = RoomTemplateLibrary.getRandomForType(RoomType.TRANSITION, rng)
        rooms.add(transTemplate.instantiate(biome, rng))

        // Boss room
        val bossTemplate = RoomTemplateLibrary.getRandomForType(RoomType.BOSS, rng)
        val bossRoom = bossTemplate.instantiate(biome, rng)
        rooms.add(bossRoom)

        // Link rooms
        for (i in 0 until rooms.size-1) {
            rooms[i].nextRoomId = rooms[i+1].id
            rooms[i+1].prevRoomId = rooms[i].id
        }

        return BiomeRun(biome, rooms, bossRoom)
    }

    fun generateFullRun(seed: Long): List<BiomeRun> {
        val ordered = BiomeRegistry.getOrdered()
        val result = mutableListOf<BiomeRun>()
        var currentSeed = seed
        for ((idx, biomeDef) in ordered.withIndex()) {
            val run = generateBiomeRun(biomeDef.type, currentSeed, idx+1)
            result.add(run)
            currentSeed += 10000
        }
        return result
    }

    // Validate connectivity - ensure all rooms reachable
    fun validateRun(run: BiomeRun): Boolean {
        if (run.rooms.isEmpty()) return false
        // Check each room has at least floor
        for (room in run.rooms) {
            if (room.tileMap.solids.isEmpty()) return false
            // Check entry/exit not inside solid
            val entryTileX = (room.entryX / Constants.TILE_SIZE).toInt()
            val entryTileY = (room.entryY / Constants.TILE_SIZE).toInt()
            if (room.tileMap.isSolid(entryTileX, entryTileY)) {
                // fix: clear area
                room.tileMap.set(entryTileX, entryTileY, TileType.EMPTY)
                room.tileMap.set(entryTileX, entryTileY-1, TileType.EMPTY)
                room.tileMap.rebuildCollision()
            }
        }
        return true
    }
}
