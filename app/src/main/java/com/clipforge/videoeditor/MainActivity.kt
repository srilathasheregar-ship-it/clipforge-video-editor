package com.clipforge.videoeditor

import android.app.AlertDialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var seekBar: SeekBar
    private lateinit var currentTime: TextView
    private lateinit var durationTime: TextView
    private lateinit var playButton: Button
    private lateinit var textOverlay: TextView

    private val handler = Handler(Looper.getMainLooper())

    private val videoPicker =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            if (uri != null) {
                loadVideo(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createEditor()
        createPlayer()
        startProgressUpdater()
    }

    private fun createEditor() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(15, 15, 15))
        }

        // TOP BAR
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(12, 8, 12, 8)
            setBackgroundColor(Color.rgb(25, 25, 25))
        }

        val title = TextView(this).apply {
            text = "ClipForge"
            textSize = 20f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        topBar.addView(
            title,
            LinearLayout.LayoutParams(
                0,
                60
            ).apply {
                weight = 1f
            }
        )

        val undo = makeButton("↶")
        val redo = makeButton("↷")
        val export = makeButton("EXPORT")

        topBar.addView(undo)
        topBar.addView(redo)
        topBar.addView(export)

        root.addView(topBar)

        // VIDEO PREVIEW
        val preview = android.widget.FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
        }

        playerView = PlayerView(this).apply {
            useController = false
            setKeepContentOnPlayerReset(true)
        }

        preview.addView(
            playerView,
            android.widget.FrameLayout.LayoutParams(
                -1,
                -1
            )
        )

        textOverlay = TextView(this).apply {
            text = ""
            textSize = 28f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.argb(120, 0, 0, 0))
            setPadding(12, 8, 12, 8)
            visibility = View.GONE
            gravity = Gravity.CENTER
        }

        val overlayParams =
            android.widget.FrameLayout.LayoutParams(
                -2,
                -2
            ).apply {
                gravity = Gravity.CENTER
            }

        preview.addView(textOverlay, overlayParams)

        val playCenter = makeButton("▶")
        playCenter.setOnClickListener {
            togglePlayback()
        }

        val playParams =
            android.widget.FrameLayout.LayoutParams(
                150,
                100
            ).apply {
                gravity = Gravity.CENTER
            }

        preview.addView(playCenter, playParams)

        root.addView(
            preview,
            LinearLayout.LayoutParams(
                -1,
                0
            ).apply {
                weight = 1f
            }
        )

        // TIME BAR
        val timeLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8, 4, 8, 4)
            setBackgroundColor(Color.rgb(10, 10, 10))
        }

        currentTime = makeLabel("00:00")
        durationTime = makeLabel("00:00")

        seekBar = SeekBar(this)

        timeLayout.addView(
            currentTime,
            LinearLayout.LayoutParams(60, -2)
        )

        timeLayout.addView(
            seekBar,
            LinearLayout.LayoutParams(
                0,
                -2
            ).apply {
                weight = 1f
            }
        )

        timeLayout.addView(
            durationTime,
            LinearLayout.LayoutParams(60, -2)
        )

        root.addView(timeLayout)

        // PLAYBACK
        val playback = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.rgb(25, 25, 25))
        }

        val previous = makeButton("|◀")
        playButton = makeButton("▶")
        val next = makeButton("▶|")

        previous.setOnClickListener {
            player.seekTo(0)
        }

        playButton.setOnClickListener {
            togglePlayback()
        }

        next.setOnClickListener {
            if (player.duration > 0) {
                player.seekTo(player.duration)
            }
        }

        playback.addView(previous)
        playback.addView(playButton)
        playback.addView(next)

        root.addView(
            playback,
            LinearLayout.LayoutParams(
                -1,
                60
            )
        )

        // TIMELINE
        val timelineTitle = makeLabel("TIMELINE")
        timelineTitle.textSize = 14f
        timelineTitle.setPadding(12, 8, 0, 8)

        root.addView(timelineTitle)

        val timeline = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8, 4, 8, 4)
            setBackgroundColor(Color.rgb(13, 13, 13))
        }

        timeline.addView(makeTrack("VIDEO", Color.rgb(55, 71, 79)))
        timeline.addView(makeTrack("TEXT", Color.rgb(255, 193, 7)))
        timeline.addView(makeTrack("STICKER", Color.rgb(142, 68, 173)))
        timeline.addView(makeTrack("AUDIO", Color.rgb(106, 27, 154)))

        root.addView(
            timeline,
            LinearLayout.LayoutParams(
                -1,
                170
            )
        )

        // TOOLBAR
        val scroll =
            android.widget.HorizontalScrollView(this)

        val tools = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.rgb(25, 25, 25))
        }

        tools.addView(
            makeTool("MEDIA") {
                openVideoPicker()
            }
        )

        tools.addView(
            makeTool("LAYER") {
                showMessage("Layer", "Layer tool selected")
            }
        )

        tools.addView(
            makeTool("AUDIO") {
                showMessage("Audio", "Audio tool selected")
            }
        )

        tools.addView(
            makeTool("TEXT") {
                showTextDialog()
            }
        )

        tools.addView(
            makeTool("STICKER") {
                addSticker()
            }
        )

        tools.addView(
            makeTool("FX") {
                showMessage("Effects", "Effects tool selected")
            }
        )

        tools.addView(
            makeTool("VOICE") {
                showMessage("Voice", "Voice recording selected")
            }
        )

        tools.addView(
            makeTool("SPEED") {
                changeSpeed()
            }
        )

        tools.addView(
            makeTool("CROP") {
                showMessage("Crop", "Crop tool selected")
            }
        )

        tools.addView(
            makeTool("ROTATE") {
                playerView.rotation =
                    (playerView.rotation + 90f) % 360f
            }
        )

        tools.addView(
            makeTool("VOLUME") {
                changeVolume()
            }
        )

        scroll.addView(tools)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1,
                85
            )
        )

        export.setOnClickListener {
            showMessage(
                "Export",
                "Export engine will be added next."
            )
        }

        setContentView(root)
    }

    private fun createPlayer() {

        player = ExoPlayer.Builder(this).build()

        playerView.player = player

        player.addListener(
            object : androidx.media3.common.Player.Listener {

                override fun onIsPlayingChanged(
                    isPlaying: Boolean
                ) {
                    playButton.text =
                        if (isPlaying) "Ⅱ" else "▶"
                }
            }
        )
    }

    private fun openVideoPicker() {

        videoPicker.launch(
            arrayOf("video/*")
        )
    }

    private fun loadVideo(uri: Uri) {

        val mediaItem =
            MediaItem.fromUri(uri)

        player.setMediaItem(mediaItem)

        player.prepare()

        player.play()

        Toast.makeText(
            this,
            "Video loaded",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun togglePlayback() {

        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    private fun startProgressUpdater() {

        handler.post(
            object : Runnable {

                override fun run() {

                    if (::player.isInitialized) {

                        val duration = player.duration
                        val position = player.currentPosition

                        if (duration > 0) {

                            seekBar.max =
                                duration.toInt()

                            seekBar.progress =
                                position.toInt()

                            currentTime.text =
                                formatTime(position)

                            durationTime.text =
                                formatTime(duration)
                        }
                    }

                    handler.postDelayed(
                        this,
                        300
                    )
                }
            }
        )

        seekBar.setOnSeekBarChangeListener(
            object :
                SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        player.seekTo(
                            progress.toLong()
                        )
                    }
                }

                override fun onStartTrackingTouch(
                    seekBar: SeekBar?
                ) {}

                override fun onStopTrackingTouch(
                    seekBar: SeekBar?
                ) {}
            }
        )
    }

    private fun showTextDialog() {

        val input = EditText(this)

        input.hint = "Enter text"

        AlertDialog.Builder(this)
            .setTitle("Add Text")
            .setView(input)
            .setNegativeButton(
                "CANCEL",
                null
            )
            .setPositiveButton(
                "ADD"
            ) { _, _ ->

                val value =
                    input.text.toString()

                if (value.isNotBlank()) {

                    textOverlay.text =
                        value

                    textOverlay.textSize =
                        28f

                    textOverlay.visibility =
                        View.VISIBLE
                }
            }
            .show()
    }

    private fun addSticker() {

        textOverlay.text = "⭐"

        textOverlay.textSize = 50f

        textOverlay.setBackgroundColor(
            Color.TRANSPARENT
        )

        textOverlay.visibility =
            View.VISIBLE
    }

    private fun changeSpeed() {

        val speeds =
            arrayOf(
                "0.5×",
                "0.75×",
                "1.0×",
                "1.25×",
                "1.5×",
                "2.0×"
            )

        AlertDialog.Builder(this)
            .setTitle("Speed")
            .setItems(speeds) { _, which ->

                player.setPlaybackSpeed(
                    speeds[which]
                        .replace("×", "")
                        .toFloat()
                )
            }
            .show()
    }

    private fun changeVolume() {

        val values =
            arrayOf(
                "Mute",
                "25%",
                "50%",
                "75%",
                "100%"
            )

        AlertDialog.Builder(this)
            .setTitle("Volume")
            .setItems(values) { _, which ->

                player.volume =
                    when (which) {
                        0 -> 0f
                        1 -> 0.25f
                        2 -> 0.5f
                        3 -> 0.75f
                        else -> 1f
                    }
            }
            .show()
    }

    private fun makeButton(
        text: String
    ): Button {

        return Button(this).apply {
            this.text = text
            setTextColor(Color.WHITE)
            setBackgroundColor(
                Color.rgb(45, 45, 45)
            )
        }
    }

    private fun makeTool(
        text: String,
        action: () -> Unit
    ): Button {

        return Button(this).apply {
            this.text = text
            setTextColor(Color.WHITE)
            setBackgroundColor(
                Color.rgb(32, 32, 32)
            )

            setOnClickListener {
                action()
            }
        }
    }

    private fun makeLabel(
        text: String
    ): TextView {

        return TextView(this).apply {
            this.text = text
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_VERTICAL
        }
    }

    private fun makeTrack(
        name: String,
        background: Int
    ): TextView {

        return TextView(this).apply {

            text = "$name     ━━━━━━━━━━━━━━━━━━━━━"

            textSize = 12f

            setTextColor(Color.WHITE)

            setBackgroundColor(background)

            gravity = Gravity.CENTER_VERTICAL

            setPadding(12, 0, 0, 0)

            layoutParams =
                LinearLayout.LayoutParams(
                    -1,
                    36
                ).apply {
                    setMargins(
                        0,
                        2,
                        0,
                        2
                    )
                }
        }
    }

    private fun formatTime(
        milliseconds: Long
    ): String {

        if (milliseconds < 0) {
            return "00:00"
        }

        val totalSeconds =
            milliseconds / 1000

        val seconds =
            totalSeconds % 60

        val minutes =
            (totalSeconds / 60) % 60

        return String.format(
            "%02d:%02d",
            minutes,
            seconds
        )
    }

    private fun showMessage(
        title: String,
        message: String
    ) {

        Toast.makeText(
            this,
            "$title: $message",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroy() {

        handler.removeCallbacksAndMessages(
            null
        )

        if (::player.isInitialized) {
            player.release()
        }

        super.onDestroy()
    }
}
