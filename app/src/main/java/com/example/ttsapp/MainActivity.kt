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

    private var tts: TextToSpeech? = null
    private lateinit var editText: EditText
    private lateinit var btnSpeak: Button
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText = findViewById(R.id.editText)
        btnSpeak = findViewById(R.id.btnSpeak)
        btnSave = findViewById(R.id.btnSave)

        // 初期化が終わるまで無効
        btnSpeak.isEnabled = false
        btnSave.isEnabled = false

        // applicationContext を使うのが重要
        tts = TextToSpeech(applicationContext, this)

        btnSpeak.setOnClickListener { speakText() }
        btnSave.setOnClickListener { saveWav() }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.JAPANESE
            tts?.setSpeechRate(1.0f)

            // 初期化成功後に有効化
            btnSpeak.isEnabled = true
            btnSave.isEnabled = true
        }
    }

    private fun speakText() {
        val text = editText.text.toString()
        if (text.isBlank()) return

        tts?.speak(
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
        val outFile = File(dir, "tts_${UUID.randomUUID()}.wav")

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {}
            override fun onError(utteranceId: String?) {}
        })

        tts?.synthesizeToFile(text, null, outFile, "SAVE_WAV")
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
