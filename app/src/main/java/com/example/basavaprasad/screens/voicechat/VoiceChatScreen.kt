package com.example.basavaprasad.screens.voicechat

import android.Manifest
import android.content.pm.PackageManager
import android.media.*
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import okhttp3.*
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "VoiceChat"

@Composable
fun VoiceChatScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var voiceState by remember { mutableStateOf("idle") }
    var session by remember { mutableStateOf<AdkVoiceSession?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            session = AdkVoiceSession { voiceState = it }.apply { start() }
        }
    }

    DisposableEffect(Unit) {
        onDispose { session?.stop() }
    }

    Column(
        modifier = modifier.fillMaxSize().background(Color(0xFF121212)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = voiceState.uppercase(),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(Color.White.copy(0.05f))
                .clickable {
                    if (voiceState == "idle") {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!hasPermission) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        else session = AdkVoiceSession { voiceState = it }.apply { start() }
                    } else {
                        session?.stop()
                        session = null
                    }
                }
        ) {
            if (voiceState == "listening" || voiceState == "speaking") {
                PulseRing(200, Color.Cyan)
            }
            Icon(
                imageVector = if (voiceState == "idle") Icons.Default.Call else Icons.Default.Close,
                contentDescription = null,
                tint = if (voiceState == "idle") Color.White else Color.Black,
                modifier = Modifier
                    .size(60.dp)
                    .background(if (voiceState == "idle") Color.Transparent else Color.White, CircleShape)
                    .padding(16.dp)
            )
        }
    }
}

private class AdkVoiceSession(private val onStateChange: (String) -> Unit) {
    private val isRunning = AtomicBoolean(false)
    private var webSocket: WebSocket? = null
    private val audioQueue = LinkedBlockingQueue<ByteArray>()

    // Manual echo cancellation gate
    private val isAiSpeaking = AtomicBoolean(false)

    // Backend Specs: 16kHz in, 24kHz out
    private val IN_SAMPLE_RATE = 16000
    private val OUT_SAMPLE_RATE = 24000

    fun start() {
        if (isRunning.get()) return
        isRunning.set(true)
        onStateChange("connecting")

        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        val request = Request.Builder()
            .url("wss://basavaprasad-digital-twin-882178443942.us-central1.run.app/voice")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                onStateChange("listening")
                startThreads()
            }

            override fun onMessage(ws: WebSocket, text: String) {
                // Server sends "turn_complete" when finished speaking
                if (text.contains("turn_complete")) {
                    isAiSpeaking.set(false) // Resume mic sending
                    onStateChange("listening")
                }
            }

            override fun onMessage(ws: WebSocket, bytes: ByteString) {
                // Incoming audio means AI is speaking
                isAiSpeaking.set(true) // Stop mic sending to avoid echo
                audioQueue.offer(bytes.toByteArray())
                onStateChange("speaking")
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WS Error: ${t.message}")
                stop()
            }
        })
    }

    private fun startThreads() {
        // --- MIC THREAD (16kHz PCM) ---
        Thread {
            try {
                val minBuf = AudioRecord.getMinBufferSize(
                    IN_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
                )
                @Suppress("MissingPermission")
                val recorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    IN_SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBuf * 2
                )

                recorder.startRecording()
                val buffer = ByteArray(3200)

                while (isRunning.get()) {
                    val read = recorder.read(buffer, 0, buffer.size)
                    // Echo Protection: Only send if AI is not speaking
                    if (read > 0 && !isAiSpeaking.get()) {
                        webSocket?.send(buffer.copyOf(read).toByteString())
                    }
                }
                recorder.release()
            } catch (e: Exception) { Log.e(TAG, "Mic Fail: ${e.message}") }
        }.start()

        // --- PLAYBACK THREAD (24kHz PCM) ---
        Thread {
            try {
                val minBuf = AudioTrack.getMinBufferSize(
                    OUT_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
                )
                val track = AudioTrack(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                    AudioFormat.Builder()
                        .setSampleRate(OUT_SAMPLE_RATE)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                    minBuf * 4,
                    AudioTrack.MODE_STREAM,
                    0
                )
                track.play()

                while (isRunning.get()) {
                    val data = audioQueue.poll(100, TimeUnit.MILLISECONDS)
                    if (data != null) {
                        track.write(data, 0, data.size)
                    }
                }
                track.release()
            } catch (e: Exception) { Log.e(TAG, "Track Fail: ${e.message}") }
        }.start()
    }

    fun stop() {
        isRunning.set(false)
        isAiSpeaking.set(false)
        webSocket?.close(1000, "User Stop")
        onStateChange("idle")
    }
}

@Composable
fun PulseRing(size: Int, color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Restart), label = ""
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Restart), label = ""
    )
    Box(Modifier.size(size.dp).scale(scale).clip(CircleShape).background(color.copy(alpha = alpha)))
}