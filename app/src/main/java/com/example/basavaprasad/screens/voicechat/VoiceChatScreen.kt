package com.basavaprasadgola.basavaprasad.screens.voicechat

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

private const val TAG = "VoiceChat"
private const val WS_URL = "wss://basavaprasad-digital-twin-882178443942.us-central1.run.app/voice"

@Composable
fun VoiceChatScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var voiceState by remember { mutableStateOf("idle") }
    var session by remember { mutableStateOf<VoiceSession?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val s = VoiceSession { voiceState = it }
            session = s
            s.start()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            session?.stop()
            session = null
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (voiceState) {
                "idle" -> "Tap to connect"
                "connecting" -> "Connecting..."
                "listening" -> "Listening..."
                "speaking" -> "Speaking..."
                else -> voiceState
            },
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(48.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
                .clickable {
                    Log.d(TAG, "TAPPED! State: $voiceState")
                    if (voiceState == "idle") {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!hasPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            val s = VoiceSession { voiceState = it }
                            session = s
                            s.start()
                        }
                    } else {
                        session?.stop()
                        session = null
                        voiceState = "idle"
                    }
                }
        ) {
            if (voiceState == "listening" || voiceState == "speaking") {
                PulseRing(size = 200, color = Color.White)
                PulseRing(size = 160, color = Color.White, delayMillis = 300)
            }

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        if (voiceState == "idle") Color.White.copy(alpha = 0.1f)
                        else Color.White.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            if (voiceState == "listening" || voiceState == "speaking")
                                Color.White else Color.White.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (voiceState == "idle") Icons.Default.Call
                        else Icons.Default.Close,
                        contentDescription = "Voice",
                        tint = if (voiceState == "listening" || voiceState == "speaking")
                            Color.Black else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        if (voiceState != "idle") {
            Text(
                text = "Tap to end",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Voice session with auto-reconnect.
 *
 * After each turn_complete, the WebSocket reconnects automatically.
 * Mic thread and playback thread stay alive the entire session.
 * The mic thread reads the current WebSocket reference atomically.
 */
private class VoiceSession(
    private val onStateChange: (String) -> Unit
) {
    private val isRunning = AtomicBoolean(false)
    private val isSendingAudio = AtomicBoolean(false)
    private val wsRef = AtomicReference<WebSocket?>(null)

    private val audioQueue = LinkedBlockingQueue<ByteArray>()
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var audioTrack: AudioTrack? = null
    private var audioRecord: AudioRecord? = null
    private var micThread: Thread? = null
    private var playbackThread: Thread? = null

    fun start() {
        if (isRunning.get()) return
        isRunning.set(true)
        onStateChange("connecting")

        // Create AudioTrack (24kHz from server)
        val sampleRate = 24000
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val track = AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
            minBuf * 4,
            AudioTrack.MODE_STREAM,
            0
        )
        audioTrack = track
        track.play()
        Log.d(TAG, "AudioTrack started")

        // Start playback thread
        playbackThread = Thread {
            try {
                while (isRunning.get() && !Thread.currentThread().isInterrupted) {
                    val data = audioQueue.poll(100, TimeUnit.MILLISECONDS)
                    if (data != null) {
                        track.write(data, 0, data.size)
                    }
                }
            } catch (_: InterruptedException) {}
            Log.d(TAG, "Playback thread ended")
        }.also { it.start() }

        // Start mic thread
        startMicThread()

        // Connect first WebSocket
        connectWebSocket()
    }

    private fun connectWebSocket() {
        if (!isRunning.get()) return

        Log.d(TAG, "Connecting WebSocket...")
        isSendingAudio.set(false)

        val request = Request.Builder().url(WS_URL).build()

        val ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket OPEN")
                wsRef.set(ws)
                isSendingAudio.set(true)
                onStateChange("listening")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d(TAG, "WS text: $text")
                if (text.contains("turn_complete")) {
                    Log.d(TAG, "Turn complete — will reconnect for next turn")
                    isSendingAudio.set(false)
                    wsRef.set(null)

                    // Close this WebSocket cleanly
                    try { ws.close(1000, "turn done") } catch (_: Exception) {}

                    // Small delay then reconnect
                    Thread {
                        try {
                            Thread.sleep(300)
                            if (isRunning.get()) {
                                onStateChange("listening")
                                connectWebSocket()
                            }
                        } catch (_: InterruptedException) {}
                    }.start()
                }
            }

            override fun onMessage(ws: WebSocket, bytes: ByteString) {
                isSendingAudio.set(false) // pause mic during AI speech
                audioQueue.offer(bytes.toByteArray())
                onStateChange("speaking")
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WS failed: ${t.message}")
                wsRef.set(null)
                // Try to reconnect if session still running
                if (isRunning.get()) {
                    Thread {
                        try {
                            Thread.sleep(1000)
                            if (isRunning.get()) {
                                onStateChange("connecting")
                                connectWebSocket()
                            }
                        } catch (_: InterruptedException) {}
                    }.start()
                }
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WS closed: $reason")
                wsRef.set(null)
            }
        })
    }

    private fun startMicThread() {
        micThread = Thread {
            try {
                val bufSize = AudioRecord.getMinBufferSize(
                    16000,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                @Suppress("MissingPermission")
                val recorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    16000,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufSize * 2
                )
                audioRecord = recorder
                recorder.startRecording()
                Log.d(TAG, "Mic STARTED")

                val buffer = ByteArray(3200)
                while (isRunning.get() && !Thread.currentThread().isInterrupted) {
                    val read = recorder.read(buffer, 0, buffer.size)
                    if (read > 0 && isSendingAudio.get()) {
                        // Send to current WebSocket (may be null during reconnect)
                        wsRef.get()?.send(buffer.copyOf(read).toByteString())
                    }
                }

                Log.d(TAG, "Mic STOPPED")
                recorder.stop()
                recorder.release()
            } catch (e: Exception) {
                Log.e(TAG, "Mic error: ${e.message}")
            }
        }.also { it.start() }
    }

    fun stop() {
        Log.d(TAG, "Session stopping")
        isRunning.set(false)
        isSendingAudio.set(false)

        micThread?.interrupt()
        micThread = null
        playbackThread?.interrupt()
        playbackThread = null
        audioQueue.clear()

        try { audioRecord?.stop() } catch (_: Exception) {}
        try { audioRecord?.release() } catch (_: Exception) {}
        audioRecord = null

        try { audioTrack?.stop() } catch (_: Exception) {}
        try { audioTrack?.release() } catch (_: Exception) {}
        audioTrack = null

        try {
            wsRef.get()?.send("{\"type\": \"close\"}")
            wsRef.get()?.close(1000, "Session ended")
        } catch (_: Exception) {}
        wsRef.set(null)

        onStateChange("idle")
    }
}

@Composable
private fun PulseRing(size: Int, color: Color, delayMillis: Int = 0) {
    val transition = rememberInfiniteTransition(label = "pulse_$size")
    val scale by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, delayMillis = delayMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_$size"
    )
    val alpha by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, delayMillis = delayMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_$size"
    )

    Box(
        modifier = Modifier
            .size(size.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}