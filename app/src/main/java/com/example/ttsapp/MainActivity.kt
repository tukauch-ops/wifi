package com.example.ttsapp

import android.os.Bundle
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var ttsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ★ XMLを必ず使う
        setContentView(R.layout.activity_main)

        // ★ TTS初期化（まだ使わない）
        tts = TextToSpeech(this, this)

        val editText = findViewById<EditText>(R.id.editText)
        val button = findViewById<Button>(R.id.button)

        // ★ ボタンを押した時だけ保存処理
        button.setOnClickListener {
            if (!ttsReady) return@setOnClickListener

            val text = editText.text.toString()
            if (text.isBlank()) return@setOnClickListener

            val file = File(
                getExternalFilesDir(null),
                "tts.wav"
            )

            tts.synthesizeToFile(
                text,
                null,
                file,
                "ttsId"
            )
        }
    }

    // ★ ここが超重要：準備完了を待つ
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.JAPANESE
            ttsReady = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
    }
}
