package com.example.tabatatimer

import android.content.Context
import android.media.SoundPool
import com.example.tabatatimer.R

class SoundManager(context: Context) {
    private val soundPool = SoundPool.Builder().setMaxStreams(2).build()
    private var hornSoundId: Int = 0

    init {
        // Ensure you have a file named 'horn.mp3' in res/raw
        hornSoundId = soundPool.load(context, R.raw.horn, 1)
    }

    fun playHorn() {
        soundPool.play(hornSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}