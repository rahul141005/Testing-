package com.lumenfall.echoes.utils

class ObjectPool<T>(private val factory: () -> T, private val reset: (T) -> Unit, initialSize: Int = 32) {
    private val free = ArrayDeque<T>(initialSize)
    private val active = mutableListOf<T>()

    init {
        repeat(initialSize) { free.add(factory()) }
    }

    fun obtain(): T {
        val obj = if (free.isNotEmpty()) free.removeLast() else factory()
        active.add(obj)
        return obj
    }

    fun free(obj: T) {
        if (active.remove(obj)) {
            reset(obj)
            free.add(obj)
        }
    }

    fun freeAll() {
        for (obj in active.toList()) {
            reset(obj)
            free.add(obj)
        }
        active.clear()
    }

    fun forEachActive(action: (T) -> Unit) {
        for (i in active.indices) action(active[i])
    }

    fun activeCount() = active.size
    fun freeCount() = free.size
}
