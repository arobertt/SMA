package com.example.tabatatimer

import android.os.CountDownTimer
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Date

enum class TimerState { IDLE, RUNNING, PAUSED }
enum class WorkoutPhase { WORK, REST, WARMUP }

data class WorkoutConfig(
    val workTimeSecs: Long = 20,
    val restTimeSecs: Long = 10,
    val rounds: Int = 8,
    val intervalHornSecs: Long = 0 // 0 means disabled
)

class TabataViewModel : ViewModel() {
    // UI State
    var timeLeftMs by mutableLongStateOf(0L)
    var currentRound by mutableIntStateOf(1)
    var phase by mutableStateOf(WorkoutPhase.WORK)
    var timerState by mutableStateOf(TimerState.IDLE)
    var config by mutableStateOf(WorkoutConfig())

    // History List
    var historyList = mutableStateListOf<Map<String, Any>>()

    // Internal Logic
    private var timer: CountDownTimer? = null
    private var soundManager: SoundManager? = null

    fun setSoundManager(manager: SoundManager) {
        this.soundManager = manager
    }

    fun updateConfig(newConfig: WorkoutConfig) {
        config = newConfig
        resetWorkout()
    }

    fun togglePausePlay() {
        if (timerState == TimerState.RUNNING) {
            pauseTimer()
        } else {
            startTimer(timeLeftMs)
        }
    }

    fun resetWorkout() {
        timer?.cancel()
        timerState = TimerState.IDLE
        phase = WorkoutPhase.WORK
        currentRound = 1
        timeLeftMs = config.workTimeSecs * 1000
    }

    private fun startTimer(durationMs: Long) {
        timer?.cancel()
        timerState = TimerState.RUNNING

        timer = object : CountDownTimer(durationMs, 100) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftMs = millisUntilFinished

                // Logic: Interval Horn inside a round
                if (phase == WorkoutPhase.WORK && config.intervalHornSecs > 0) {
                    val secondsElapsed = (config.workTimeSecs * 1000 - millisUntilFinished) / 1000
                    if (secondsElapsed > 0 && secondsElapsed % config.intervalHornSecs == 0L) {
                        // Simple debounce check could be added here
                        soundManager?.playHorn()
                    }
                }
            }

            override fun onFinish() {
                handlePhaseFinish()
            }
        }.start()
    }

    private fun pauseTimer() {
        timer?.cancel()
        timerState = TimerState.PAUSED
    }

    private fun handlePhaseFinish() {
        soundManager?.playHorn() // Play sound on switch

        if (phase == WorkoutPhase.WORK) {
            if (currentRound >= config.rounds) {
                // Workout Complete
                finishWorkout()
            } else {
                phase = WorkoutPhase.REST
                startTimer(config.restTimeSecs * 1000)
            }
        } else {
            // Rest Finished, start next round
            phase = WorkoutPhase.WORK
            currentRound++
            startTimer(config.workTimeSecs * 1000)
        }
    }

    private fun finishWorkout() {
        timerState = TimerState.IDLE
        phase = WorkoutPhase.WORK
        saveToFirebase()
        resetWorkout()
    }

    // --- Firebase Logic ---
    fun fetchHistory() {
        val user = Firebase.auth.currentUser ?: return
        Firebase.firestore.collection("users").document(user.uid)
            .collection("workouts").orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                historyList.clear()
                for (document in result) {
                    historyList.add(document.data)
                }
            }
    }

    private fun saveToFirebase() {
        val user = Firebase.auth.currentUser

        if (user == null) {
            println("ERROR: User is not logged in. Cannot save.")
            return
        }

        println("Attempting to save workout for user: ${user.uid}")

        val workoutData = hashMapOf(
            "date" to Date(),
            "rounds" to config.rounds,
            "total_time_sec" to (config.rounds * (config.workTimeSecs + config.restTimeSecs))
        )

        Firebase.firestore.collection("users").document(user.uid)
            .collection("workouts")
            .add(workoutData)
            .addOnSuccessListener {
                println("SUCCESS: Workout saved with ID: ${it.id}")
            }
            .addOnFailureListener { e ->
                println("FAILURE: Error adding document: $e")
            }
    }
}