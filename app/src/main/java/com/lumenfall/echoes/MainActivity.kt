package com.lumenfall.echoes

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.lumenfall.echoes.audio.AudioManager
import com.lumenfall.echoes.game.GameEngine
import com.lumenfall.echoes.progression.SaveManager

class MainActivity : AppCompatActivity() {

    private var gameEngine: GameEngine? = null
    private lateinit var saveManager: SaveManager
    private lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        saveManager = SaveManager(this)
        audioManager = AudioManager(this)

        val settings = saveManager.loadPlayerSave().settings
        audioManager.setVolumes(settings.musicVolume, settings.sfxVolume)

        gameEngine = GameEngine(this, saveManager, audioManager)
        setContentView(gameEngine)

        // Apply left-handed if needed - handled inside engine
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gameEngine?.handleTouch(event) ?: super.onTouchEvent(event)
    }

    override fun onPause() {
        super.onPause()
        gameEngine?.stopGameLoop()
        audioManager.pause()
    }

    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        )
        gameEngine?.startGameLoop()
        audioManager.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        gameEngine?.stopGameLoop()
        audioManager.release()
    }

    override fun onBackPressed() {
        // Handle back as pause
        if (gameEngine?.gameState == com.lumenfall.echoes.game.GameState.PLAYING) {
            gameEngine?.gameState = com.lumenfall.echoes.game.GameState.PAUSED
        } else if (gameEngine?.gameState == com.lumenfall.echoes.game.GameState.PAUSED) {
            gameEngine?.gameState = com.lumenfall.echoes.game.GameState.PLAYING
        } else {
            super.onBackPressed()
        }
    }
}
