package com.example.ttsapp

import android.media.MediaPlayer
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
    private lateinit var audioFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 保存先ファイル
        audioFile = File(filesDir, "tts.wav")

        // TextToSpeech 初期化
        tts = TextToSpeech(this, this)

        val editText = findViewById<EditText>(R.id.editText)
        val speakButton = findViewById<Button>(R.id.buttonSpeak)
        val playButton = findViewById<Button>(R.id.btnPlay)

        // 読み上げ＋保存
        speakButton.setOnClickListener {
            if (!ready) return@setOnClickListener

            val text = editText.text.toString()
            if (text.isBlank()) return@setOnClickListener

            speakAndSave(text)
        }

        // 保存した音声を再生
        playButton.setOnClickListener {
            if (!audioFile.exists()) return@setOnClickListener

            val player = MediaPlayer()
            player.setDataSource(audioFile.absolutePath)
            player.prepare()
            player.start()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.JAPAN
            ready = true
        }
    }

    private fun speakAndSave(text: String) {
        // その場で読み上げ
        tts.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "speak"
        )

        // 音声を保存（アプリ専用フォルダ）
        tts.synthesizeToFile(
            text,
            null,
            audioFile,
            "save"
        )
    }

    override fun onDestroy() {
        tts.shutdown()
        super.onDestroy()
    }
}
