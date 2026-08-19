package com.clipforge.videoeditor

import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import android.graphics.Color
import android.graphics.Typeface
import android.view.Window
import android.view.WindowManager

class MainActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var playButton: Button
    private lateinit var importButton: Button
    private lateinit var timeText: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var statusText: TextView

    private val videoPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->

            if (uri != null) {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                    // Some providers do not support persistable permissions.
                }

                loadVideo(uri)
            } else {
                Toast.makeText(
                    this,
                    "No video selected",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setStatusBarColor(Color.BLACK)
        window.setNavigationBarColor(Color.BLACK)

        createEditorScreen()
        createPlayer()
    }

    private fun createEditorScreen() {

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.BLACK)

        // ---------------------------------------------------------
        // TOP BAR
        // ---------------------------------------------------------

        val topBar = LinearLayout(this)
        topBar.orientation = LinearLayout.HORIZONTAL
        topBar.gravity = Gravity.CENTER_VERTICAL
        topBar.setPadding(16, 8, 16, 8)
        topBar.setBackgroundColor(Color.rgb(30, 30, 30))

        val logo = TextView(this)
        logo.text = "ClipForge"
        logo.textSize = 24f
        logo.setTextColor(Color.WHITE)
        logo.setTypeface(null, Typeface.BOLD)

        topBar.addView(
            logo,
            LinearLayout.LayoutParams(
                0,
                64,
                1f
            )
        )

        importButton = Button(this)
        importButton.text = "IMPORT VIDEO"
        importButton.textSize = 12f
        importButton.setTextColor(Color.WHITE)

        importButton.setOnClickListener {
            openVideoPicker()
        }

        topBar.addView(
            importButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                64
            )
        )

        root.addView(
            topBar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                72
            )
        )

        // ---------------------------------------------------------
        // VIDEO PREVIEW
        // ---------------------------------------------------------

        val previewContainer = FrameLayout(this)
        previewContainer.setBackgroundColor(Color.BLACK)

        playerView = PlayerView(this)

        playerView.useController = false
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
        playerView.setBackgroundColor(Color.BLACK)

        previewContainer.addView(
            playerView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // Center play button

        playButton = Button(this)
        playButton.text = "▶"
        playButton.textSize = 24f
        playButton.setTextColor(Color.WHITE)
        playButton.setBackgroundColor(Color.DKGRAY)

        playButton.setOnClickListener {
            togglePlayback()
        }

        val playParams = FrameLayout.LayoutParams(
            110,
            80
        )

        playParams.gravity = Gravity.CENTER

        previewContainer.addView(
            playButton,
            playParams
        )

        root.addView(
            previewContainer,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        // ---------------------------------------------------------
        // VIDEO TIME
        // ---------------------------------------------------------

        val timeContainer = LinearLayout(this)

        timeContainer.orientation = LinearLayout.HORIZONTAL
        timeContainer.gravity = Gravity.CENTER_VERTICAL
        timeContainer.setPadding(12, 4, 12, 4)
        timeContainer.setBackgroundColor(Color.rgb(20, 20, 20))

        timeText = TextView(this)
        timeText.text = "00:00 / 00:00"
        timeText.textSize = 16f
        timeText.setTextColor(Color.WHITE)

        timeContainer.addView(
            timeText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                50
            )
        )

        root.addView(
            timeContainer,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                55
            )
        )

        // ---------------------------------------------------------
        // SEEK BAR
        // ---------------------------------------------------------

        seekBar = SeekBar(this)

        seekBar.max = 1000
        seekBar.progress = 0

        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    if (fromUser) {
                        val duration = player?.duration ?: 0L

                        if (duration > 0) {
                            val position =
                                duration * progress / 1000L

                            player?.seekTo(position)
                        }
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

        root.addView(
            seekBar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                45
            )
        )

        // ---------------------------------------------------------
        // TIMELINE TITLE
        // ---------------------------------------------------------

        val timelineTitle = TextView(this)

        timelineTitle.text = "TIMELINE"
        timelineTitle.textSize = 18f
        timelineTitle.setTextColor(Color.WHITE)
        timelineTitle.setTypeface(null, Typeface.BOLD)
        timelineTitle.setPadding(10, 8, 10, 8)
        timelineTitle.setBackgroundColor(Color.rgb(15, 15, 15))

        root.addView(
            timelineTitle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                50
            )
        )

        // ---------------------------------------------------------
        // TIMELINE TRACKS
        // ---------------------------------------------------------

        val timeline = LinearLayout(this)
        timeline.orientation = LinearLayout.VERTICAL
        timeline.setBackgroundColor(Color.rgb(10, 10, 10))

        timeline.addView(
            createTrack("VIDEO", Color.rgb(70, 95, 105))
        )

        timeline.addView(
            createTrack("TEXT", Color.rgb(255, 190, 0))
        )

        timeline.addView(
            createTrack("STICKER", Color.rgb(145, 65, 180))
        )

        timeline.addView(
            createTrack("AUDIO", Color.rgb(70, 20, 80))
        )

        root.addView(
            timeline,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                200
            )
        )

        // ---------------------------------------------------------
        // BOTTOM TOOL BAR
        // ---------------------------------------------------------

        val bottomBar = LinearLayout(this)

        bottomBar.orientation = LinearLayout.HORIZONTAL
        bottomBar.gravity = Gravity.CENTER
        bottomBar.setBackgroundColor(Color.rgb(20, 20, 20))

        addToolButton(bottomBar, "MEDIA")
        addToolButton(bottomBar, "LAYER")
        addToolButton(bottomBar, "AUDIO")
        addToolButton(bottomBar, "TEXT")
        addToolButton(bottomBar, "STICKER")

        root.addView(
            bottomBar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                70
            )
        )

        // ---------------------------------------------------------
        // STATUS MESSAGE
        // ---------------------------------------------------------

        statusText = TextView(this)
        statusText.text = "Ready — import a video to begin"
        statusText.textSize = 13f
        statusText.setTextColor(Color.LTGRAY)
        statusText.gravity = Gravity.CENTER
        statusText.setPadding(5, 5, 5, 5)
        statusText.setBackgroundColor(Color.BLACK)

        root.addView(
            statusText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                45
            )
        )

        setContentView(root)
    }

    private fun createTrack(
        name: String,
        color: Int
    ): TextView {

        val track = TextView(this)

        track.text = name
        track.textSize = 15f
        track.setTextColor(Color.WHITE)
        track.gravity = Gravity.CENTER_VERTICAL
        track.setTypeface(null, Typeface.BOLD)

        track.setPadding(10, 0, 0, 0)
        track.setBackgroundColor(color)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            45
        )

        params.setMargins(0, 2, 0, 2)

        track.layoutParams = params

        return track
    }

    private fun addToolButton(
        parent: LinearLayout,
        title: String
    ) {

        val button = Button(this)

        button.text = title
        button.textSize = 11f
        button.setTextColor(Color.WHITE)

        button.setOnClickListener {

            when (title) {

                "MEDIA" -> {
                    openVideoPicker()
                }

                "LAYER" -> {
                    Toast.makeText(
                        this,
                        "Layer tools coming next",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                "AUDIO" -> {
                    Toast.makeText(
                        this,
                        "Audio tools coming next",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                "TEXT" -> {
                    Toast.makeText(
                        this,
                        "Text tools coming next",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                "STICKER" -> {
                    Toast.makeText(
                        this,
                        "Sticker tools coming next",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        parent.addView(
            button,
            LinearLayout.LayoutParams(
                0,
                70,
                1f
            )
        )
    }

    // -------------------------------------------------------------
    // VIDEO PICKER
    // -------------------------------------------------------------

    private fun openVideoPicker() {

        videoPicker.launch(
            arrayOf(
                "video/*"
            )
        )
    }

    // -------------------------------------------------------------
    // CREATE EXOPLAYER
    // -------------------------------------------------------------

    private fun createPlayer() {

        player = ExoPlayer.Builder(this)
            .build()

        playerView.player = player

        player?.addListener(
            object : Player.Listener {

                override fun onIsPlayingChanged(
                    isPlaying: Boolean
                ) {

                    playButton.text =
                        if (isPlaying) "❚❚"
                        else "▶"
                }

                override fun onPlaybackStateChanged(
                    playbackState: Int
                ) {

                    if (
                        playbackState ==
                        Player.STATE_READY
                    ) {

                        updateDuration()
                    }

                    if (
                        playbackState ==
                        Player.STATE_ENDED
                    ) {

                        playButton.text = "▶"
                    }
                }
            }
        )
    }

    // -------------------------------------------------------------
    // LOAD SELECTED VIDEO
    // -------------------------------------------------------------

    private fun loadVideo(uri: Uri) {

        statusText.text = "Loading video..."

        val mediaItem =
            MediaItem.fromUri(uri)

        player?.setMediaItem(mediaItem)

        player?.prepare()

        player?.playWhenReady = false

        statusText.text =
            "Video loaded — press ▶ to play"

        Toast.makeText(
            this,
            "Video imported successfully",
            Toast.LENGTH_SHORT
        ).show()

        updateDuration()
    }

    // -------------------------------------------------------------
    // PLAY / PAUSE
    // -------------------------------------------------------------

    private fun togglePlayback() {

        val currentPlayer = player

        if (currentPlayer == null) {
            return
        }

        if (currentPlayer.currentMediaItem == null) {

            Toast.makeText(
                this,
                "Please import a video first",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (currentPlayer.isPlaying) {

            currentPlayer.pause()

        } else {

            currentPlayer.play()
        }
    }

    // -------------------------------------------------------------
    // UPDATE VIDEO TIME
    // -------------------------------------------------------------

    private fun updateDuration() {

        val duration =
            player?.duration ?: 0L

        if (duration <= 0) {
            timeText.text = "00:00 / 00:00"
            return
        }

        timeText.text =
            "00:00 / ${formatTime(duration)}"

        window.decorView.postDelayed(
            object : Runnable {

                override fun run() {

                    val currentPlayer = player
                        ?: return

                    val position =
                        currentPlayer.currentPosition

                    val total =
                        currentPlayer.duration

                    if (total > 0) {

                        val progress =
                            ((position.toDouble() /
                                    total.toDouble()) * 1000)
                                .toInt()

                        seekBar.progress =
                            progress

                        timeText.text =
                            "${formatTime(position)} / " +
                                    "${formatTime(total)}"
                    }

                    if (currentPlayer.isPlaying) {

                        window.decorView.postDelayed(
                            this,
                            250
                        )
                    }
                }
            },
            250
        )
    }

    private fun formatTime(
        milliseconds: Long
    ): String {

        if (milliseconds < 0) {
            return "00:00"
        }

        val totalSeconds =
            milliseconds / 1000

        val minutes =
            totalSeconds / 60

        val seconds =
            totalSeconds % 60

        return String.format(
            "%02d:%02d",
            minutes,
            seconds
        )
    }

    // -------------------------------------------------------------
    // CLEANUP
    // -------------------------------------------------------------

    override fun onStop() {

        super.onStop()

        player?.pause()
    }

    override fun onDestroy() {

        player?.release()
        player = null

        super.onDestroy()
    }
}
