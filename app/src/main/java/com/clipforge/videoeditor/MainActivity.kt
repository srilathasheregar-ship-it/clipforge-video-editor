package com.clipforge.videoeditor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
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

        createEditorUI()
    }

    private fun createEditorUI() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val title = TextView(this).apply {
            text = "ClipForge Video Editor"
            textSize = 24f
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 20)
        }

        statusText = TextView(this).apply {
            text = "No video selected"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 10)
        }

        videoView = VideoView(this).apply {
            visibility = View.GONE
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
                statusText.text = "Trim tool selected"
            }
        }

        val splitButton = Button(this).apply {
            text = "✂ Split"
            setOnClickListener {
                statusText.text = "Split tool selected"
            }
        }

        val textButton = Button(this).apply {
            text = "T Add Text"
            setOnClickListener {
                statusText.text = "Text tool selected"
            }
        }

        val exportButton = Button(this).apply {
            text = "Export Video"
            setOnClickListener {
                statusText.text = "Export feature coming soon"
            }
        }

        root.addView(title)
        root.addView(statusText)

        root.addView(
            videoView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                500
            )
        )

        root.addView(importButton)
        root.addView(playButton)
        root.addView(trimButton)
        root.addView(splitButton)
        root.addView(textButton)
        root.addView(exportButton)

        setContentView(root)
    }

    private fun openVideoPicker() {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "video/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        startActivityForResult(intent, PICK_VIDEO)
    }

    @Deprecated("Deprecated in Android API")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_VIDEO &&
            resultCode == RESULT_OK &&
            data?.data != null
        ) {

            val videoUri: Uri = data.data!!

            videoView.visibility = View.VISIBLE
            videoView.setVideoURI(videoUri)

            statusText.text = "Video loaded successfully"

            videoView.setOnPreparedListener {
                videoView.start()
            }
        }
    }
}
