package com.lumenfall.echoes.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.lumenfall.echoes.entities.Player
import com.lumenfall.echoes.progression.RunManager
import com.lumenfall.echoes.world.BiomeRegistry
import com.lumenfall.echoes.world.Room

/**
 * HUD renderer - separate from GameEngine for clean architecture.
 * Handles health, coins, weapon, abilities, minimap hints.
 */
class HUD {
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 28f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
    }

    fun render(canvas: Canvas, player: Player, room: Room?, runManager: RunManager?) {
        val w = canvas.width.toFloat()
        // top bar
        bgPaint.color = Color.argb(160, 18,18,30)
        canvas.drawRect(0f,0f,w,110f, bgPaint)

        // HP
        val hpPct = player.hp.toFloat()/player.maxHp
        bgPaint.color = Color.argb(200, 40,40,40)
        canvas.drawRoundRect(20f,20f,320f,48f,12f,12f, bgPaint)
        bgPaint.color = Color.parseColor("#F87171")
        canvas.drawRoundRect(20f,20f,20f+300f*hpPct,48f,12f,12f, bgPaint)
        textPaint.textSize = 22f
        canvas.drawText("HP ${player.hp}/${player.maxHp}", 30f, 40f, textPaint)

        // Coins
        bgPaint.color = Color.parseColor("#FFD700")
        canvas.drawCircle(400f, 34f, 12f, bgPaint)
        canvas.drawText("${player.coins} (${runManager?.coins ?: 0})", 424f, 42f, textPaint)

        // Weapon
        textPaint.textSize = 20f
        canvas.drawText("W: ${player.primaryWeapon.name}", 20f, 80f, textPaint)
        val a1Cd = if (player.ability1.cooldownTimer>0) "${player.ability1.cooldownTimer.toInt()}s" else "READY"
        canvas.drawText("A1: ${player.ability1.name} [$a1Cd]  A2: ${player.ability2.name}", 20f, 102f, textPaint)

        room?.let {
            val biome = BiomeRegistry.get(it.biome)
            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.color = Color.parseColor("#9AA0B0")
            canvas.drawText("${biome.name} - Room ${runManager?.currentRoomIndex ?: 0}", w-20f, 40f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.color = Color.WHITE
        }

        if (room?.isCleared == true) {
            textPaint.color = Color.parseColor("#4ADE80")
            textPaint.textSize = 24f
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("Room Cleared! Find EXIT →", w/2, 140f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.color = Color.WHITE
        }
    }
}
