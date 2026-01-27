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
import java.io.FileInputStream
import java.io.OutputStream
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var ready = false
    private lateinit var audioFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        audioFile = File(filesDir, "tts.wav")

        tts = TextToSpeech(this, this)

        val editText = findViewById<EditText>(R.id.editText)
        val speakButton = findViewById<Button>(R.id.buttonSpeak)
        val saveButton = findViewById<Button>(R.id.buttonSave)
        val playButton = findViewById<Button>(R.id.btnPlay)

        // 初期：再生ボタン無効（保存が有効になったら enable）
        playButton.isEnabled = audioFile.exists() && audioFile.length() > 0

        // 読み上げ（その場で発話）
        speakButton.setOnClickListener {
            if (!ready) return@setOnClickListener
            val text = editText.text.toString()
            if (text.isBlank()) return@setOnClickListener
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utter_speak")
        }

        // 保存（ファイルに synthesizeToFile）
        saveButton.setOnClickListener {
            if (!ready) return@setOnClickListener
            val text = editText.text.toString()
            if (text.isBlank()) {
                Toast.makeText(this, "テキストが空です", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // ファイル上書きで保存開始（非同期）
            tts.synthesizeToFile(text, null, audioFile, "utter_save")
            Toast.makeText(this, "保存を開始しました...", Toast.LENGTH_SHORT).show()
        }

        // 再生
        playButton.setOnClickListener {
            if (!audioFile.exists() || audioFile.length() == 0L) {
                Toast.makeText(this, "保存済み音声が見つかりません", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                val player = MediaPlayer()
                player.setDataSource(audioFile.absolutePath)
                player.prepare()
                player.start()
                player.setOnCompletionListener { it.release() }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "再生に失敗しました", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.JAPAN
            ready = true

            // Utterance の完了を受け取るリスナを登録
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) { /* 必要ならUI更新 */ }

                override fun onDone(utteranceId: String?) {
                    if (utteranceId == "utter_save") {
                        // 保存完了判定：ファイルが存在してサイズが正ならOK
                        val ok = audioFile.exists() && audioFile.length() > 0
                        runOnUiThread {
                            if (ok) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "保存完了: ${audioFile.absolutePath}",
                                    Toast.LENGTH_LONG
                                ).show()
                                findViewById<Button>(R.id.btnPlay).isEnabled = true
                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    "保存に失敗しました（ファイルが空です）",
                                    Toast.LENGTH_LONG
                                ).show()
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
            Toast.makeText(this, "TTS 初期化に失敗しました", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        tts.shutdown()
        super.onDestroy()
    }
}
