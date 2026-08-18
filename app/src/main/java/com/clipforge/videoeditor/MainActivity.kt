package com.clipforge.videoeditor

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
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
    private lateinit var timeText: TextView

    private val handler = Handler(Looper.getMainLooper())

    // VIDEO PICKER
    private val videoPicker =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            if (uri != null) {
                loadVideo(uri)
            } else {
                Toast.makeText(
                    this,
                    "No video selected",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val updateTime = object : Runnable {

        override fun run() {

            if (::player.isInitialized) {

                val position = player.currentPosition
                val duration = player.duration

                if (duration > 0) {

                    timeText.text =
                        "${formatTime(position)} / ${formatTime(duration)}"
                }
            }

            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK

        createEditor()

        initializePlayer()

        handler.post(updateTime)
    }

    private fun createEditor() {

        val root = LinearLayout(this)

        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.BLACK)

        // =========================
        // TOP BAR
        // =========================

        val topBar = LinearLayout(this)

        topBar.orientation = LinearLayout.HORIZONTAL
        topBar.gravity = Gravity.CENTER_VERTICAL
        topBar.setPadding(8, 8, 8, 8)
        topBar.setBackgroundColor(
            Color.rgb(30, 30, 30)
        )

        val title = TextView(this)

        title.text = "ClipForge"
        title.textSize = 22f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER_VERTICAL

        topBar.addView(
            title,
            LinearLayout.LayoutParams(
                0,
                60,
                1f
            )
        )

        val undoButton =
            createTopButton("↶")

        topBar.addView(
            undoButton,
            LinearLayout.LayoutParams(
                55,
                60
            )
        )

        val redoButton =
            createTopButton("↷")

        topBar.addView(
            redoButton,
            LinearLayout.LayoutParams(
                55,
                60
            )
        )

        val exportButton =
            createTopButton("EXPORT")

        topBar.addView(
            exportButton,
            LinearLayout.LayoutParams(
                100,
                60
            )
        )

        root.addView(
            topBar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                70
            )
        )

        // =========================
        // IMPORT VIDEO BUTTON
        // =========================

        val importButton = Button(this)

        importButton.text = "＋  IMPORT VIDEO"
        importButton.textSize = 16f
        importButton.setTextColor(Color.WHITE)

        importButton.setOnClickListener {
            openVideoPicker()
        }

        root.addView(
            importButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                60
            )
        )

        // =========================
        // VIDEO PREVIEW
        // =========================

        playerView = PlayerView(this)

        playerView.setBackgroundColor(Color.BLACK)

        playerView.useController = true

        root.addView(
            playerView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        // =========================
        // TIME
        // =========================

        timeText = TextView(this)

        timeText.text = "00:00 / 00:00"
        timeText.textSize = 18f
        timeText.setTextColor(Color.WHITE)
        timeText.gravity = Gravity.CENTER
        timeText.setBackgroundColor(
            Color.rgb(20, 20, 20)
        )

        root.addView(
            timeText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                50
            )
        )

        // =========================
        // TIMELINE TITLE
        // =========================

        val timelineTitle = TextView(this)

        timelineTitle.text = "TIMELINE"
        timelineTitle.textSize = 16f
        timelineTitle.setTextColor(Color.WHITE)
        timelineTitle.setPadding(
            10,
            4,
            10,
            4
        )

        root.addView(
            timelineTitle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                40
            )
        )

        // =========================
        // TRACKS
        // =========================

        addTimelineTrack(
            root,
            "VIDEO",
            Color.rgb(65, 85, 95)
        )

        addTimelineTrack(
            root,
            "TEXT",
            Color.rgb(255, 190, 0)
        )

        addTimelineTrack(
            root,
            "STICKER",
            Color.rgb(140, 70, 170)
        )

        addTimelineTrack(
            root,
            "AUDIO",
            Color.rgb(90, 45, 110)
        )

        // =========================
        // BOTTOM TOOLBAR
        // =========================

        val toolbar = LinearLayout(this)

        toolbar.orientation =
            LinearLayout.HORIZONTAL

        toolbar.gravity =
            Gravity.CENTER

        toolbar.setBackgroundColor(
            Color.rgb(20, 20, 20)
        )

        addTool(
            toolbar,
            "MEDIA"
        ) {
            openVideoPicker()
        }

        addTool(
            toolbar,
            "LAYER"
        ) {
            showMessage(
                "Layer",
                "Coming next"
            )
        }

        addTool(
            toolbar,
            "AUDIO"
        ) {
            showMessage(
                "Audio",
                "Coming next"
            )
        }

        addTool(
            toolbar,
            "TEXT"
        ) {
            showMessage(
                "Text",
                "Coming next"
            )
        }

        addTool(
            toolbar,
            "STICKER"
        ) {
            showMessage(
                "Sticker",
                "Coming next"
            )
        }

        root.addView(
            toolbar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                70
            )
        )

        setContentView(root)
    }

    // =========================
    // PLAYER
    // =========================

    private fun initializePlayer() {

        player =
            ExoPlayer.Builder(this)
                .build()

        playerView.player = player
    }

    // =========================
    // OPEN VIDEO PICKER
    // =========================

    private fun openVideoPicker() {

        try {

            videoPicker.launch("video/*")

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Unable to open video picker",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // =========================
    // LOAD VIDEO
    // =========================

    private fun loadVideo(uri: Uri) {

        try {

            val mediaItem =
                MediaItem.fromUri(uri)

            player.setMediaItem(mediaItem)

            player.prepare()

            player.play()

            Toast.makeText(
                this,
                "Video loaded successfully",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Video could not be loaded",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // =========================
    // TIMELINE
    // =========================

    private fun addTimelineTrack(
        root: LinearLayout,
        name: String,
        backgroundColor: Int
    ) {

        val track = TextView(this)

        track.text =
            "  $name       ─────────────────────"

        track.textSize = 15f

        track.setTextColor(Color.WHITE)

        track.gravity =
            Gravity.CENTER_VERTICAL

        track.setBackgroundColor(
            backgroundColor
        )

        root.addView(
            track,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                35
            )
        )
    }

    // =========================
    // TOOLBAR BUTTON
    // =========================

    private fun addTool(
        toolbar: LinearLayout,
        text: String,
        action: () -> Unit
    ) {

        val button = Button(this)

        button.text = text

        button.textSize = 11f

        button.setTextColor(Color.WHITE)

        button.setBackgroundColor(
            Color.TRANSPARENT
        )

        button.setOnClickListener {
            action()
        }

        toolbar.addView(
            button,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
        )
    }

    private fun createTopButton(
        text: String
    ): Button {

        val button = Button(this)

        button.text = text

        button.textSize = 12f

        button.setTextColor(Color.WHITE)

        button.setBackgroundColor(
            Color.TRANSPARENT
        )

        return button
    }

    // =========================
    // TIME FORMAT
    // =========================

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

    override fun onStop() {

        super.onStop()

        if (::player.isInitialized) {
            player.pause()
        }
    }

    override fun onDestroy() {

        handler.removeCallbacks(updateTime)

        if (::player.isInitialized) {
            player.release()
        }

        super.onDestroy()
    }
}
