package com.example.ttsapp

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var editText: EditText
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText = findViewById(R.id.editText)
        button = findViewById(R.id.button)

        tts = TextToSpeech(this, this)

        button.setOnClickListener {
            val text = editText.text.toString()
            if (text.isNotEmpty()) {
                speakAndSaveWav(text)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.JAPAN
        }
    }

    private fun speakAndSaveWav(text: String) {
        val outFile = File(filesDir, "tts.wav")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts1")

            tts.synthesizeToFile(text, params, outFile, "tts1")
        } else {
            @Suppress("DEPRECATION")
            tts.synthesizeToFile(text, null, outFile.absolutePath)
        }
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}
        
