package com.clipforge.videoeditor

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Toast.makeText(
            this,
            "ClipForge started successfully",
            Toast.LENGTH_LONG
        ).show()
    }
}
