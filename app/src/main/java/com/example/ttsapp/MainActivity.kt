package com.example.ttsapp

import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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

        val editText = findViewById<EditText>(R.id.editText)
        val speakButton = findViewById<Button>(R.id.buttonSpeak)
        val saveButton = findViewById<Button>(R.id.buttonSave)
        val playButton = findViewById<Button>(R.id.buttonPlay)

        // 貼り付け確実化
        editText.isFocusableInTouchMode = true
        editText.requestFocus()

        audioFile = File(filesDir, "tts.wav")
        tts = TextToSpeech(this, this)

        playButton.isEnabled = audioFile.exists() && audioFile.length() > 0

        // 読み上げ
        speakButton.setOnClickListener {
            if (!ready) return@setOnClickListener
            val text = editText.text.toString()
            if (text.isBlank()) return@setOnClickListener
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utter_speak")
        }

        // 保存
        saveButton.setOnClickListener {
            if (!ready) return@setOnClickListener
            val text = editText.text.toString()
            if (text.isBlank()) {
                Toast.makeText(this, "テキストが空です", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            tts.synthesizeToFile(text, null, audioFile, "utter_save")
            Toast.makeText(this, "保存中…", Toast.LENGTH_SHORT).show()
        }

        // 再生
        playButton.setOnClickListener {
            if (!audioFile.exists() || audioFile.length() == 0L) {
                Toast.makeText(this, "保存音声がありません", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                val player = MediaPlayer()
                player.setDataSource(audioFile.absolutePath)
                player.prepare()
                player.start()
                player.setOnCompletionListener { it.release() }
            } catch (e: Exception) {
                Toast.makeText(this, "再生失敗", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.JAPAN
            ready = true

            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                override fun onDone(utteranceId: String?) {
                    if (utteranceId == "utter_save") {
                        runOnUiThread {
                            val ok = audioFile.exists() && audioFile.length() > 0
                            if (ok) {
                                findViewById<Button>(R.id.buttonPlay).isEnabled = true
                                Toast.makeText(this@MainActivity, "保存完了", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@MainActivity, "保存失敗", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                override fun onError(utteranceId: String?) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "音声生成エラー", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        } else {
            Toast.makeText(this, "TTS 初期化失敗", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        tts.shutdown()
        super.onDestroy()
    }
}
