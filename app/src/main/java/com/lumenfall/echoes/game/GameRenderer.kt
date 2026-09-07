package com.lumenfall.echoes.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

/**
 * Dedicated renderer - separated from GameEngine for clean architecture.
 * In this implementation, GameEngine owns rendering for performance (single Canvas),
 * but this class provides reusable drawing utilities and is used for UI/HUD rendering.
 */
class GameRenderer {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 32f
    }

    fun clear(canvas: Canvas, color: Int) {
        canvas.drawColor(color)
    }

    fun drawRect(canvas: Canvas, x: Float, y: Float, w: Float, h: Float, color: Int) {
        paint.color = color
        canvas.drawRect(x, y, x+w, y+h, paint)
    }

    fun drawCircle(canvas: Canvas, x: Float, y: Float, r: Float, color: Int) {
        paint.color = color
        canvas.drawCircle(x, y, r, paint)
    }

    fun drawText(canvas: Canvas, text: String, x: Float, y: Float, color: Int = Color.WHITE, size: Float = 32f) {
        textPaint.color = color
        textPaint.textSize = size
        canvas.drawText(text, x, y, textPaint)
    }
}
