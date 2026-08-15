package com.clipforge.videoeditor

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlin.math.max
import kotlin.math.min


class MainActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private lateinit var player: ExoPlayer

    private lateinit var playPauseButton: TextView
    private lateinit var centerPlayButton: TextView

    private lateinit var seekBar: SeekBar
    private lateinit var currentTimeText: TextView
    private lateinit var durationText: TextView

    private lateinit var textOverlay: TextView

    private val handler = Handler(Looper.getMainLooper())

    private val updateProgress = object : Runnable {
        override fun run() {

            if (::player.isInitialized) {

                val duration = player.duration
                val position = player.currentPosition

                if (duration > 0) {

                    seekBar.max = duration.toInt()

                    seekBar.progress =
                        position.toInt().coerceIn(0, duration.toInt())

                    currentTimeText.text =
                        formatTime(position)

                    durationText.text =
                        formatTime(duration)
                }
            }

            handler.postDelayed(this, 300)
        }
    }


    private val videoPicker =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->

            if (uri != null) {

                try {

                    contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                } catch (_: Exception) {
                }

                loadVideo(uri)
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        initializeViews()

        initializePlayer()

        setupButtons()

        setupSeekBar()

        setupTextDragging()

        handler.post(updateProgress)
    }


    private fun initializeViews() {

        playerView =
            findViewById(R.id.playerView)

        playPauseButton =
            findViewById(R.id.playPauseButton)

        centerPlayButton =
            findViewById(R.id.centerPlayButton)

        seekBar =
            findViewById(R.id.videoSeekBar)

        currentTimeText =
            findViewById(R.id.currentTimeText)

        durationText =
            findViewById(R.id.durationText)

        textOverlay =
            findViewById(R.id.textOverlay)
    }


    private fun initializePlayer() {

        player =
            ExoPlayer.Builder(this)
                .build()

        playerView.player = player

        player.addListener(
            object :
                androidx.media3.common.Player.Listener {

                override fun onIsPlayingChanged(
                    isPlaying: Boolean
                ) {

                    updatePlayButton(isPlaying)
                }
            }
        )
    }


    private fun setupButtons() {

        /*
         * MEDIA
         */

        findViewById<View>(
            R.id.mediaButton
        ).setOnClickListener {

            openVideoPicker()
        }


        /*
         * IMPORT VIDEO
         */

        findViewById<View>(
            R.id.titleText
        ).setOnClickListener {

            openVideoPicker()
        }


        /*
         * PLAY / PAUSE
         */

        playPauseButton.setOnClickListener {

            togglePlayback()
        }


        centerPlayButton.setOnClickListener {

            togglePlayback()
        }


        /*
         * PREVIOUS
         */

        findViewById<View>(
            R.id.previousButton
        ).setOnClickListener {

            if (player.isCommandAvailable(
                    androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS
                )
            ) {

                player.seekToPrevious()
            } else {

                player.seekTo(0)
            }
        }


        /*
         * NEXT
         */

        findViewById<View>(
            R.id.nextButton
        ).setOnClickListener {

            if (player.isCommandAvailable(
                    androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT
                )
            ) {

                player.seekToNext()
            } else {

                val duration = player.duration

                if (duration > 0) {
                    player.seekTo(duration)
                }
            }
        }


        /*
         * TEXT
         */

        findViewById<View>(
            R.id.textButton
        ).setOnClickListener {

            showTextDialog()
        }


        /*
         * LAYER
         */

        findViewById<View>(
            R.id.layerButton
        ).setOnClickListener {

            showMessage(
                "Layer",
                "Text, sticker and media layers can be added here."
            )
        }


        /*
         * STICKER
         */

        findViewById<View>(
            R.id.stickerButton
        ).setOnClickListener {

            addSticker()
        }


        /*
         * AUDIO
         */

        findViewById<View>(
            R.id.audioButton
        ).setOnClickListener {

            showMessage(
                "Audio",
                "Audio track system will be added next."
            )
        }


        /*
         * EFFECT
         */

        findViewById<View>(
            R.id.effectButton
        ).setOnClickListener {

            showMessage(
                "Effects",
                "Video effects system will be added next."
            )
        }


        /*
         * VOICE
         */

        findViewById<View>(
            R.id.voiceButton
        ).setOnClickListener {

            showMessage(
                "Voice",
                "Voice recording system will be added next."
            )
        }


        /*
         * SPEED
         */

        findViewById<View>(
            R.id.speedButton
        ).setOnClickListener {

            changeSpeed()
        }


        /*
         * CROP
         */

        findViewById<View>(
            R.id.cropButton
        ).setOnClickListener {

            showMessage(
                "Crop",
                "Crop editor will be added next."
            )
        }


        /*
         * ROTATE
         */

        findViewById<View>(
            R.id.rotateButton
        ).setOnClickListener {

            rotatePreview()
        }


        /*
         * VOLUME
         */

        findViewById<View>(
            R.id.volumeButton
        ).setOnClickListener {

            changeVolume()
        }


        /*
         * FULL SCREEN
         */

        findViewById<View>(
            R.id.fullscreenButton
        ).setOnClickListener {

            toggleFullscreen()
        }


        /*
         * EXPORT
         */

        findViewById<View>(
            R.id.exportButton
        ).setOnClickListener {

            showMessage(
                "Export",
                "MP4 rendering will be connected next."
            )
        }


        /*
         * UNDO
         */

        findViewById<View>(
            R.id.undoButton
        ).setOnClickListener {

            showMessage(
                "Undo",
                "Undo system ready for the editing engine."
            )
        }


        /*
         * REDO
         */

        findViewById<View>(
            R.id.redoButton
        ).setOnClickListener {

            showMessage(
                "Redo",
                "Redo system ready for the editing engine."
            )
        }


        /*
         * TIMELINE VIDEO CLIP
         */

        findViewById<View>(
            R.id.videoClip
        ).setOnClickListener {

            showMessage(
                "Video Clip",
                "Clip selected."
            )
        }


        /*
         * TEXT CLIP
         */

        findViewById<View>(
            R.id.textClip
        ).setOnClickListener {

            showTextDialog()
        }


        /*
         * STICKER CLIP
         */

        findViewById<View>(
            R.id.stickerClip
        ).setOnClickListener {

            addSticker()
        }
    }


    private fun setupSeekBar() {

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
                ) {
                }


                override fun onStopTrackingTouch(
                    seekBar: SeekBar?
                ) {
                }
            }
        )
    }


    private fun setupTextDragging() {

        textOverlay.setOnTouchListener(
            object : View.OnTouchListener {

                private var dX = 0f
                private var dY = 0f

                override fun onTouch(
                    view: View,
                    event: MotionEvent
                ): Boolean {

                    when (event.actionMasked) {

                        MotionEvent.ACTION_DOWN -> {

                            dX =
                                view.x - event.rawX

                            dY =
                                view.y - event.rawY

                            return true
                        }


                        MotionEvent.ACTION_MOVE -> {

                            val parent =
                                view.parent as View

                            val newX =
                                event.rawX + dX

                            val newY =
                                event.rawY + dY

                            view.x =
                                min(
                                    max(
                                        0f,
                                        newX
                                    ),
                                    (
                                        parent.width -
                                            view.width
                                    ).toFloat()
                                )

                            view.y =
                                min(
                                    max(
                                        0f,
                                        newY
                                    ),
                                    (
                                        parent.height -
                                            view.height
                                    ).toFloat()
                                )

                            return true
                        }
                    }

                    return true
                }
            }
        )
    }


    private fun openVideoPicker() {

        videoPicker.launch(
            arrayOf("video/*")
        )
    }


    private fun loadVideo(
        uri: Uri
    ) {

        val mediaItem =
            MediaItem.fromUri(uri)

        player.setMediaItem(
            mediaItem
        )

        player.prepare()

        player.play()

        showMessage(
            "ClipForge",
            "Video loaded successfully."
        )
    }


    private fun togglePlayback() {

        if (player.isPlaying) {

            player.pause()

        } else {

            player.play()
        }
    }


    private fun updatePlayButton(
        playing: Boolean
    ) {

        if (playing) {

            playPauseButton.text = "Ⅱ"

            centerPlayButton.visibility =
                View.GONE

        } else {

            playPauseButton.text = "▶"

            centerPlayButton.visibility =
                View.VISIBLE
        }
    }


    private fun showTextDialog() {

        val input =
            EditText(this)

        input.hint =
            "Enter text"

        input.setSingleLine(false)

        input.gravity =
            Gravity.CENTER

        val dialog =
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

                    val text =
                        input.text.toString()

                    if (text.isNotBlank()) {

                        textOverlay.text =
                            text

                        textOverlay.visibility =
                            View.VISIBLE

                        findViewById<TextView>(
                            R.id.textClip
                        ).text =
                            text
                    }
                }
                .create()

        dialog.show()
    }


    private fun addSticker() {

        textOverlay.text =
            "⭐"

        textOverlay.textSize =
            48f

        textOverlay.setBackgroundColor(
            android.graphics.Color.TRANSPARENT
        )

        textOverlay.visibility =
            View.VISIBLE

        findViewById<TextView>(
            R.id.stickerClip
        ).text =
            "⭐ Sticker"

        showMessage(
            "Sticker",
            "Sticker added to preview."
        )
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
            .setTitle("Playback Speed")
            .setItems(speeds) { _, which ->

                val value =
                    speeds[which]
                        .replace("×", "")
                        .toFloat()

                player.setPlaybackSpeed(
                    value
                )

                showMessage(
                    "Speed",
                    speeds[which]
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

                val volume =
                    when (which) {

                        0 -> 0f
                        1 -> 0.25f
                        2 -> 0.50f
                        3 -> 0.75f
                        else -> 1f
                    }

                player.volume =
                    volume

                showMessage(
                    "Volume",
                    values[which]
                )
            }
            .show()
    }


    private fun rotatePreview() {

        val current =
            playerView.rotation

        playerView.rotation =
            if (current >= 270f) {
                0f
            } else {
                current + 90f
            }

        showMessage(
            "Rotate",
            "Preview rotated."
        )
    }


    private fun toggleFullscreen() {

        val decorView =
            window.decorView

        val flags =
            decorView.systemUiVisibility

        if (
            flags and
            View.SYSTEM_UI_FLAG_FULLSCREEN
            != 0
        ) {

            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_VISIBLE

        } else {

            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
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

        val hours =
            totalSeconds / 3600

        return if (hours > 0) {

            String.format(
                "%02d:%02d:%02d",
                hours,
                minutes,
                seconds
            )

        } else {

            String.format(
                "%02d:%02d",
                minutes,
                seconds
            )
        }
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


    override fun onStart() {

        super.onStart()

        if (::player.isInitialized) {
            player.prepare()
        }
    }


    override fun onStop() {

        super.onStop()

        if (::player.isInitialized) {
            player.pause()
        }
    }


    override fun onDestroy() {

        handler.removeCallbacks(
            updateProgress
        )

        if (::player.isInitialized) {
            player.release()
        }

        super.onDestroy()
    }
}
