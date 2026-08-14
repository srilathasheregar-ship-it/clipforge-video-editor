package com.clipforge.videoeditor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.VideoView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var videoView: VideoView

    companion object {
        private const val PICK_VIDEO = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        videoView = findViewById(R.id.videoView)

        val importButton: Button = findViewById(R.id.importVideoButton)
        val playButton: Button = findViewById(R.id.playButton)
        val pauseButton: Button = findViewById(R.id.pauseButton)
        val trimButton: Button = findViewById(R.id.trimButton)
        val splitButton: Button = findViewById(R.id.splitButton)
        val addTextButton: Button = findViewById(R.id.addTextButton)
        val exportButton: Button = findViewById(R.id.exportButton)

        importButton.setOnClickListener {
            openVideoPicker()
        }

        playButton.setOnClickListener {
            if (videoView.visibility == View.VISIBLE) {
                videoView.start()
            }
        }

        pauseButton.setOnClickListener {
            if (videoView.visibility == View.VISIBLE) {
                videoView.pause()
            }
        }

        trimButton.setOnClickListener {
            Toast.makeText(this, "Trim tool selected", Toast.LENGTH_SHORT).show()
        }

        splitButton.setOnClickListener {
            Toast.makeText(this, "Split tool selected", Toast.LENGTH_SHORT).show()
        }

        addTextButton.setOnClickListener {
            Toast.makeText(this, "Text tool selected", Toast.LENGTH_SHORT).show()
        }

        exportButton.setOnClickListener {
            Toast.makeText(
                this,
                "Export feature coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }
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

        if (
            requestCode == PICK_VIDEO &&
            resultCode == RESULT_OK &&
            data?.data != null
        ) {
            val videoUri: Uri = data.data!!

            videoView.visibility = View.VISIBLE
            videoView.setVideoURI(videoUri)

            videoView.setOnPreparedListener {
                videoView.start()
            }

            Toast.makeText(
                this,
                "Video loaded successfully",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
