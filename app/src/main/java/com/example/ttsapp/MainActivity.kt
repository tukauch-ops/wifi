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
        val button = findViewById<Button>(R.id.button)

        button.setOnClickListener {
            if (!ready) return@setOnClickListener

            val text = editText.text.toString()
            if (text.isBlank()) return@setOnClickListener

            speakAndSave(text)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.JAPAN
            ready = true
        }
    }

    private fun speakAndSave(text: String) {
        val file = File(filesDir, "tts.wav")

        // 再生
        tts.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "speak1"
        )

        // 保存（アプリ専用フォルダ）
        tts.synthesizeToFile(
            text,
            null,
            file,
            "save1"
        )
    }

    override fun onDestroy() {
        tts.shutdown()
        super.onDestroy()
    }
}
