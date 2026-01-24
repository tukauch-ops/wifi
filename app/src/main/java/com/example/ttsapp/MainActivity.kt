package com.example.ttsapp

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import java.io.File
import java.util.Locale
import java.util.UUID

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var editText: EditText
    private var ttsReady = false   // ★重要

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText = findViewById(R.id.editText)
        val btnSpeak = findViewById<Button>(R.id.btnSpeak)
        val btnSave = findViewById<Button>(R.id.btnSave)

        tts = TextToSpeech(this, this)

        // ★ Listener は1回だけ
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {}
            override fun onError(utteranceId: String?) {}
        })

        btnSpeak.setOnClickListener {
            if (ttsReady) speakText()
        }

        btnSave.setOnClickListener {
            if (ttsReady) saveWav()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.JAPANESE
            tts.setSpeechRate(1.0f)
            ttsReady = true   // ★ここで初めて使える
        }
    }

    private fun speakText() {
        val text = editText.text.toString()
        if (text.isBlank()) return

        tts.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "SPEAK_ID"
        )
    }

    private fun saveWav() {
        val text = editText.text.toString()
        if (text.isBlank()) return

        val dir = getExternalFilesDir(null) ?: return
        val fileName = "tts_${UUID.randomUUID()}.wav"
        val outFile = File(dir, fileName)

        tts.synthesizeToFile(
            text,
            null,
            outFile,
            "SAVE_WAV"
        )
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}
