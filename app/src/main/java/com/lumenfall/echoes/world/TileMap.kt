package com.lumenfall.echoes.world

import com.lumenfall.echoes.game.Constants
import com.lumenfall.echoes.utils.Rect

enum class TileType {
    EMPTY, SOLID, PLATFORM, SPIKE, LADDER, DECOR
}

class TileMap(val width: Int, val height: Int) {
    private val tiles = Array(height) { Array(width) { TileType.EMPTY } }
    val solids = mutableListOf<Rect>()

    fun set(x: Int, y: Int, type: TileType) {
        if (x in 0 until width && y in 0 until height) {
            tiles[y][x] = type
        }
    }

    fun get(x: Int, y: Int): TileType {
        if (x !in 0 until width || y !in 0 until height) return TileType.SOLID // out of bounds = solid
        return tiles[y][x]
    }

    fun isSolid(x: Int, y: Int): Boolean {
        val t = get(x,y)
        return t == TileType.SOLID
    }

    fun isPlatform(x: Int, y: Int): Boolean {
        return get(x,y) == TileType.PLATFORM
    }

    fun rebuildCollision() {
        solids.clear()
        // naive merging: for each solid tile, create rect
        // For performance, merge horizontal runs
        for (y in 0 until height) {
            var runStart = -1
            for (x in 0 until width) {
                if (isSolid(x,y)) {
                    if (runStart == -1) runStart = x
                } else {
                    if (runStart != -1) {
                        solids.add(Rect(
                            runStart * Constants.TILE_SIZE.toFloat(),
                            y * Constants.TILE_SIZE.toFloat(),
                            (x - runStart) * Constants.TILE_SIZE.toFloat(),
                            Constants.TILE_SIZE.toFloat()
                        ))
                        runStart = -1
                    }
                }
            }
            if (runStart != -1) {
                solids.add(Rect(
                    runStart * Constants.TILE_SIZE.toFloat(),
                    y * Constants.TILE_SIZE.toFloat(),
                    (width - runStart) * Constants.TILE_SIZE.toFloat(),
                    Constants.TILE_SIZE.toFloat()
                ))
            }
        }
        // platforms as separate thin rects
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (isPlatform(x,y)) {
                    solids.add(Rect(
                        x*Constants.TILE_SIZE.toFloat(),
                        y*Constants.TILE_SIZE.toFloat(),
                        Constants.TILE_SIZE.toFloat(),
                        8f
                    ))
                }
            }
        }
    }

    fun clear() {
        for (y in 0 until height) for (x in 0 until width) tiles[y][x] = TileType.EMPTY
        solids.clear()
    }

    fun fillRect(x0: Int, y0: Int, w: Int, h: Int, type: TileType) {
        for (y in y0 until y0+h) for (x in x0 until x0+w) set(x,y,type)
    }
}
