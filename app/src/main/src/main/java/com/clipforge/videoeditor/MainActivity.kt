package com.clipforge.videoeditor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.VideoView

class MainActivity : Activity() {

    private lateinit var videoView: VideoView
    private lateinit var statusText: TextView

    companion object {
        private const val PICK_VIDEO = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createEditor()
    }

    private fun createEditor() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val title = TextView(this).apply {
            text = "ClipForge"
            textSize = 28f
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "Video Editor • Version 1"
            textSize = 16f
            gravity = Gravity.CENTER
        }

        videoView = VideoView(this).apply {
            setBackgroundColor(0xFF111111.toInt())
        }

        val videoParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0
        ).apply {
            weight = 1f
            topMargin = 20
            bottomMargin = 20
        }

        val importButton = Button(this).apply {
            text = "＋ Import Video"
            setOnClickListener {
                openVideoPicker()
            }
        }

        val playButton = Button(this).apply {
            text = "▶ Play / Pause"
            setOnClickListener {
                if (videoView.isPlaying) {
                    videoView.pause()
                } else {
                    videoView.start()
                }
            }
        }

        val trimButton = Button(this).apply {
            text = "✂ Trim"
            setOnClickListener {
                statusText.text = "Trim tool selected — coming next"
            }
        }

        val splitButton = Button(this).apply {
            text = "✂ Split"
            setOnClickListener {
                statusText.text = "Split tool selected — coming next"
            }
        }

        val textButton = Button(this).apply {
            text = "T Add Text"
            setOnClickListener {
                statusText.text = "Text tool selected — coming next"
            }
        }

        val exportButton = Button(this).apply {
            text = "Export Video"
            setOnClickListener {
                statusText.text = "Export tool selected — coming next"
            }
        }

        statusText = TextView(this).apply {
            text = "Import a video to start editing"
            textSize = 14f
            gravity = Gravity.CENTER
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(videoView, videoParams)
        root.addView(importButton)
        root.addView(playButton)
        root.addView(trimButton)
        root.addView(splitButton)
        root.addView(textButton)
        root.addView(exportButton)
        root.addView(statusText)

        setContentView(root)
    }

    private fun openVideoPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "video/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        startActivityForResult(intent, PICK_VIDEO)
    }

    @Deprecated("Deprecated Android API, kept for compatibility")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_VIDEO && resultCode == RESULT_OK) {

            val videoUri: Uri? = data?.data

            if (videoUri != null) {
                videoView.setVideoURI(videoUri)
                videoView.setOnPreparedListener {
                    statusText.text = "Video loaded successfully"
                    videoView.start()
                }
            }
        }
    }
}
