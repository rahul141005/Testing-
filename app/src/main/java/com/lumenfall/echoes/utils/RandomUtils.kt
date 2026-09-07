package com.lumenfall.echoes.utils

import kotlin.random.Random

class SeededRandom(seed: Long) {
    private var random = Random(seed)
    var currentSeed: Long = seed
        private set

    fun setSeed(seed: Long) {
        currentSeed = seed
        random = Random(seed)
    }

    fun nextFloat() = random.nextFloat()
    fun nextFloat(min: Float, max: Float) = min + random.nextFloat() * (max - min)
    fun nextInt(min: Int, max: Int) = random.nextInt(min, max+1) // inclusive
    fun nextInt(bound: Int) = random.nextInt(bound)
    fun nextBoolean() = random.nextBoolean()
    fun chance(p: Float) = random.nextFloat() < p

    fun <T> pick(list: List<T>): T = list[random.nextInt(list.size)]
    fun <T> pick(vararg items: T): T = items[random.nextInt(items.size)]
    fun <T> shuffle(list: MutableList<T>) { list.shuffle(random) }
}

object GlobalRandom {
    val rng = SeededRandom(System.currentTimeMillis())
}
