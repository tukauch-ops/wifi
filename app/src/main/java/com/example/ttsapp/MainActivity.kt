package com.example.ttsapp

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var ready = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tts = TextToSpeech(this, this)

        val editText = findViewById<EditText>(R.id.editText)
        val speakButton = findViewById<Button>(R.id.buttonSpeak)
        val saveButton = findViewById<Button>(R.id.buttonSave)

        // 読み上げ
        speakButton.setOnClickListener {
            if (!ready) return@setOnClickListener
            val text = editText.text.toString()
            if (text.isBlank()) return@setOnClickListener

            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "speak")
        }

        // 保存のみ
        saveButton.setOnClickListener {
            if (!ready) return@setOnClickListener
            val text = editText.text.toString()
            if (text.isBlank()) return@setOnClickListener

            val file = File(filesDir, "tts.wav")
            tts.synthesizeToFile(text, null, file, "save")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.JAPAN
            ready = true
        }
    }

    override fun onDestroy() {
        tts.shutdown()
        super.onDestroy()
    }
}
