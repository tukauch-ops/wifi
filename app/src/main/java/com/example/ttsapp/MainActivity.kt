package com.example.ttsapp2

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var ready = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editText = findViewById<EditText>(R.id.editText)
        val speakButton = findViewById<Button>(R.id.buttonSpeak)

        tts = TextToSpeech(this, this)

        speakButton.setOnClickListener {
            if (!ready) {
                Toast.makeText(this, "TTS 準備中", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val text = editText.text.toString()
            if (text.isBlank()) return@setOnClickListener

            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utter1")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.JAPAN
            ready = true
            Toast.makeText(this, "TTS 準備完了", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "TTS 初期化失敗", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        tts.shutdown()
        super.onDestroy()
    }
}
