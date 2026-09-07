package com.lumenfall.echoes.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log

class AudioManager(private val context: Context) {
    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<String, Int>()
    private var musicPlayer: MediaPlayer? = null
    var musicVolume = 0.8f
    var sfxVolume = 0.9f
    var isMuted = false

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(16)
            .setAudioAttributes(attrs)
            .build()
        // Load placeholder sounds - we generate tones procedurally? For now empty
        // In real game, load from res/raw
    }

    fun playSfx(name: String, pitch: Float = 1f) {
        if (isMuted) return
        val id = soundMap[name] ?: return
        soundPool?.play(id, sfxVolume, sfxVolume, 1, 0, pitch)
    }

    fun playMusic(track: String, loop: Boolean = true) {
        if (isMuted) return
        try {
            musicPlayer?.stop()
            musicPlayer?.release()
            // In real implementation, load from assets
            // For this version, we use no actual music files to keep APK small
            // Placeholder: log
            Log.d("AudioManager", "Play music: $track")
        } catch (e: Exception) {
            Log.e("AudioManager", "Music error: $e")
        }
    }

    fun setVolumes(music: Float, sfx: Float) {
        musicVolume = music
        sfxVolume = sfx
        musicPlayer?.setVolume(music, music)
    }

    fun pause() {
        musicPlayer?.pause()
    }

    fun resume() {
        musicPlayer?.start()
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        musicPlayer?.release()
        musicPlayer = null
    }

    // Procedural sound generation helpers - generate simple tones for feedback
    fun playHitSound(isCrit: Boolean, heavy: Boolean) {
        // In a real game, play actual samples. Here we log for debugging
        // Could implement ToneGenerator
    }

    fun playJump() = playSfx("jump")
    fun playDash() = playSfx("dash")
    fun playAttack(id: String) = playSfx("attack_$id")
    fun playEnemyHit() = playSfx("enemy_hit")
    fun playPickup() = playSfx("pickup")
}
