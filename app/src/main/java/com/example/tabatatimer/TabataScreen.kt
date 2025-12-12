package com.example.tabatatimer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabataApp(viewModel: TabataViewModel = viewModel()) {
    var showSettings by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }

    // Colors
    val bgColor = Color(0xFF121212)
    val workColor = Color(0xFF00E676)
    val restColor = Color(0xFFFF3D00)
    val activeColor = if (viewModel.phase == WorkoutPhase.WORK) workColor else restColor

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = { Text("Tabata Timer", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor),
                actions = {
                    IconButton(onClick = {
                        viewModel.fetchHistory()
                        showHistory = true
                    }) {
                        Icon(Icons.Default.History, "History", tint = Color.White)
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, "Settings", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Timer Display
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = 1f, // Static background ring
                    modifier = Modifier.size(300.dp),
                    color = Color.DarkGray,
                    strokeWidth = 20.dp
                )

                // Text Time
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (viewModel.phase == WorkoutPhase.WORK) "WORK" else "REST",
                        fontSize = 32.sp,
                        color = activeColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = (viewModel.timeLeftMs / 1000 + 1).toString(),
                        fontSize = 90.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Round ${viewModel.currentRound} / ${viewModel.config.rounds}",
                        fontSize = 24.sp,
                        color = Color.LightGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(50.dp))

            // Controls
            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                // Reset
                Button(
                    onClick = { viewModel.resetWorkout() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Refresh, "Reset", tint = Color.White)
                }

                // Play/Pause
                Button(
                    onClick = { viewModel.togglePausePlay() },
                    colors = ButtonDefaults.buttonColors(containerColor = activeColor),
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        if (viewModel.timerState == TimerState.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                        "Toggle",
                        tint = Color.Black
                    )
                }
            }
        }
    }

    // Settings Bottom Sheet (Simplified as a basic Dialog for copy-paste ease)
    if (showSettings) {
        SettingsDialog(
            currentConfig = viewModel.config,
            onDismiss = { showSettings = false },
            onConfirm = { viewModel.updateConfig(it); showSettings = false }
        )
    }

    // History Sheet
    if (showHistory) {
        HistoryDialog(
            history = viewModel.historyList,
            onDismiss = { showHistory = false }
        )
    }
}

@Composable
fun SettingsDialog(currentConfig: WorkoutConfig, onDismiss: () -> Unit, onConfirm: (WorkoutConfig) -> Unit) {
    var work by remember { mutableStateOf(currentConfig.workTimeSecs.toString()) }
    var rest by remember { mutableStateOf(currentConfig.restTimeSecs.toString()) }
    var rounds by remember { mutableStateOf(currentConfig.rounds.toString()) }
    var interval by remember { mutableStateOf(currentConfig.intervalHornSecs.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Workout Settings") },
        text = {
            Column {
                OutlinedTextField(value = work, onValueChange = { work = it }, label = { Text("Work (sec)") })
                OutlinedTextField(value = rest, onValueChange = { rest = it }, label = { Text("Rest (sec)") })
                OutlinedTextField(value = rounds, onValueChange = { rounds = it }, label = { Text("Rounds") })
                OutlinedTextField(value = interval, onValueChange = { interval = it }, label = { Text("Horn Interval (0=off)") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(WorkoutConfig(
                    work.toLongOrNull() ?: 20,
                    rest.toLongOrNull() ?: 10,
                    rounds.toIntOrNull() ?: 8,
                    interval.toLongOrNull() ?: 0
                ))
            }) { Text("Save") }
        }
    )
}

@Composable
fun HistoryDialog(history: List<Map<String, Any>>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("History") },
        text = {
            LazyColumn {
                items(history) { item ->
                    val date = item["date"] as? com.google.firebase.Timestamp
                    val fmt = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                    val dateStr = date?.toDate()?.let { fmt.format(it) } ?: "Unknown Date"

                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Date: $dateStr", fontWeight = FontWeight.Bold)
                        Text("Rounds: ${item["rounds"]}")
                        Divider()
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Close") } }
    )
}