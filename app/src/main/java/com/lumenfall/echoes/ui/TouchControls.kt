package com.lumenfall.echoes.ui

import android.graphics.RectF
import android.view.MotionEvent
import kotlin.math.*

data class TouchButton(
    val id: String,
    var rect: RectF,
    var isPressed: Boolean = false,
    var pointerId: Int = -1,
    var alpha: Float = 0.7f,
    var scale: Float = 1f
)

class VirtualJoystick(
    var centerX: Float,
    var centerY: Float,
    var radius: Float = 120f,
    var thumbRadius: Float = 50f
) {
    var thumbX = centerX
    var thumbY = centerY
    var inputX = 0f
    var inputY = 0f
    var isActive = false
    var pointerId = -1
    var alpha = 0.6f

    fun onTouchDown(x: Float, y: Float, pid: Int): Boolean {
        val dx = x - centerX
        val dy = y - centerY
        if (sqrt(dx*dx + dy*dy) <= radius*1.5f) {
            isActive = true
            pointerId = pid
            updateThumb(x,y)
            return true
        }
        return false
    }

    fun onTouchMove(x: Float, y: Float, pid: Int) {
        if (isActive && pointerId == pid) {
            updateThumb(x,y)
        }
    }

    fun onTouchUp(pid: Int) {
        if (pointerId == pid) {
            isActive = false
            pointerId = -1
            thumbX = centerX
            thumbY = centerY
            inputX = 0f
            inputY = 0f
        }
    }

    private fun updateThumb(x: Float, y: Float) {
        val dx = x - centerX
        val dy = y - centerY
        val dist = sqrt(dx*dx + dy*dy)
        if (dist <= radius) {
            thumbX = x
            thumbY = y
            inputX = dx / radius
            inputY = dy / radius
        } else {
            val angle = atan2(dy, dx)
            thumbX = centerX + cos(angle)*radius
            thumbY = centerY + sin(angle)*radius
            inputX = cos(angle)
            inputY = sin(angle)
        }
    }

    fun getMoveX(): Float = inputX
}

class TouchControls(
    private var screenW: Int,
    private var screenH: Int,
    var leftHanded: Boolean = false,
    var scale: Float = 1f,
    var opacity: Float = 0.7f
) {
    val joystick: VirtualJoystick
    val buttons = mutableMapOf<String, TouchButton>()

    var jumpPressed = false
    var dashPressed = false
    var attackPressed = false
    var ability1Pressed = false
    var ability2Pressed = false
    var rangedPressed = false
    var interactPressed = false
    var pausePressed = false

    init {
        val joyX = if (leftHanded) screenW - 180f else 180f
        val joyY = screenH - 180f
        joystick = VirtualJoystick(joyX, joyY, 110f*scale, 48f*scale)
        joystick.alpha = opacity
        createButtons()
    }

    private fun createButtons() {
        buttons.clear()
        val baseSize = 110f * scale
        val margin = 20f
        val rightSide = if (leftHanded) false else true
        val buttonAreaX = if (rightSide) screenW - baseSize*2 - margin*2 else margin
        val buttonAreaY = screenH - baseSize*3 - margin*3

        // Attack - big rightmost
        buttons["attack"] = TouchButton("attack",
            RectF(
                (if (rightSide) screenW - baseSize - margin else margin + baseSize + margin),
                screenH - baseSize - margin,
                (if (rightSide) screenW - margin else margin + baseSize*2 + margin),
                screenH - margin
            ), alpha = opacity, scale = scale*1.15f)

        // Jump above attack
        buttons["jump"] = TouchButton("jump",
            RectF(
                (if (rightSide) screenW - baseSize*2 - margin*1.5f else margin),
                screenH - baseSize*2 - margin*1.5f,
                (if (rightSide) screenW - baseSize - margin*1.5f else margin + baseSize),
                screenH - baseSize - margin*1.5f + baseSize
            ), alpha = opacity, scale = scale)

        // Dash
        buttons["dash"] = TouchButton("dash",
            RectF(
                (if (rightSide) screenW - baseSize - margin else margin + baseSize + margin),
                screenH - baseSize*2 - margin*1.5f,
                (if (rightSide) screenW - margin else margin + baseSize*2 + margin),
                screenH - baseSize - margin*1.5f + baseSize
            ), alpha = opacity, scale = scale)

        // Ability 1
        buttons["ability1"] = TouchButton("ability1",
            RectF(
                (if (rightSide) screenW - baseSize*2 - margin*1.5f else margin),
                screenH - baseSize*3 - margin*2f,
                (if (rightSide) screenW - baseSize - margin*1.5f else margin + baseSize),
                screenH - baseSize*2 - margin*2f + baseSize
            ), alpha = opacity*0.9f, scale = scale*0.9f)

        // Ability 2
        buttons["ability2"] = TouchButton("ability2",
            RectF(
                (if (rightSide) screenW - baseSize - margin else margin + baseSize + margin),
                screenH - baseSize*3 - margin*2f,
                (if (rightSide) screenW - margin else margin + baseSize*2 + margin),
                screenH - baseSize*2 - margin*2f + baseSize
            ), alpha = opacity*0.9f, scale = scale*0.9f)

        // Interact - top right small
        buttons["interact"] = TouchButton("interact",
            RectF(screenW - 100f, 20f, screenW - 20f, 100f), alpha = opacity, scale = scale*0.8f)

        // Pause
        buttons["pause"] = TouchButton("pause",
            RectF(screenW/2f - 50f, 20f, screenW/2f + 50f, 90f), alpha = opacity, scale = scale*0.8f)
    }

    fun resize(w: Int, h: Int) {
        screenW = w
        screenH = h
        val joyX = if (leftHanded) w - 180f else 180f
        val joyY = h - 180f
        joystick.centerX = joyX
        joystick.centerY = joyY
        joystick.thumbX = joyX
        joystick.thumbY = joyY
        createButtons()
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val pointerIndex = event.actionIndex
        val pointerId = event.getPointerId(pointerIndex)
        val x = event.getX(pointerIndex)
        val y = event.getY(pointerIndex)

        when(action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                // Check joystick first
                if (!joystick.isActive) {
                    if (joystick.onTouchDown(x,y,pointerId)) return true
                }
                // Check buttons
                for (btn in buttons.values) {
                    if (btn.rect.contains(x,y) && btn.pointerId==-1) {
                        btn.isPressed = true
                        btn.pointerId = pointerId
                        handleButtonPress(btn.id, true)
                        return true
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val pid = event.getPointerId(i)
                    val mx = event.getX(i)
                    val my = event.getY(i)
                    if (joystick.pointerId == pid) {
                        joystick.onTouchMove(mx,my,pid)
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                if (joystick.pointerId == pointerId) {
                    joystick.onTouchUp(pointerId)
                }
                for (btn in buttons.values) {
                    if (btn.pointerId == pointerId) {
                        btn.isPressed = false
                        btn.pointerId = -1
                        handleButtonPress(btn.id, false)
                    }
                }
                if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    // reset all if last pointer
                    if (event.pointerCount <= 1) {
                        joystick.onTouchUp(joystick.pointerId)
                        for (btn in buttons.values) {
                            if (btn.isPressed) {
                                btn.isPressed = false
                                btn.pointerId = -1
                                handleButtonPress(btn.id, false)
                            }
                        }
                    }
                }
            }
        }
        return true
    }

    private fun handleButtonPress(id: String, pressed: Boolean) {
        if (!pressed) return // we only care about press events for actions, hold for movement already handled
        when(id) {
            "jump" -> jumpPressed = true
            "dash" -> dashPressed = true
            "attack" -> attackPressed = true
            "ability1" -> ability1Pressed = true
            "ability2" -> ability2Pressed = true
            "interact" -> interactPressed = true
            "pause" -> pausePressed = true
        }
    }

    fun consumeInputs(): InputSnapshot {
        val snap = InputSnapshot(
            moveX = joystick.getMoveX(),
            jump = jumpPressed,
            dash = dashPressed,
            attack = attackPressed,
            ability1 = ability1Pressed,
            ability2 = ability2Pressed,
            ranged = rangedPressed,
            interact = interactPressed,
            pause = pausePressed
        )
        // reset one-shot presses
        jumpPressed = false
        dashPressed = false
        attackPressed = false
        ability1Pressed = false
        ability2Pressed = false
        rangedPressed = false
        interactPressed = false
        pausePressed = false
        return snap
    }

    data class InputSnapshot(
        val moveX: Float,
        val jump: Boolean,
        val dash: Boolean,
        val attack: Boolean,
        val ability1: Boolean,
        val ability2: Boolean,
        val ranged: Boolean,
        val interact: Boolean,
        val pause: Boolean
    )
}
