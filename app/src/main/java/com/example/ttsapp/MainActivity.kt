package com.example.ttsapp

import android.content.ContentValues
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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

        // 初期は再生ボタン無効（保存完了で有効化）
        playButton.isEnabled = audioFile.exists() && audioFile.length() > 0

        speakButton.setOnClickListener {
            if (!ready) return@setOnClickListener
            val text = editText.text.toString()
            if (text.isBlank()) return@setOnClickListener
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utter_speak")
        }

        saveButton.setOnClickListener {
            if (!ready) return@setOnClickListener
            val text = editText.text.toString()
            if (text.isBlank()) {
                Toast.makeText(this, "テキストが空です", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // 保存（非同期）を開始。完了は UtteranceProgressListener の onDone で受け取る
            tts.synthesizeToFile(text, null, audioFile, "utter_save")
            Toast.makeText(this, "保存を開始しました...", Toast.LENGTH_SHORT).show()
        }

        playButton.setOnClickListener {
            if (!audioFile.exists() || audioFile.length() == 0L) {
                Toast.makeText(this, "保存済み音声が見つかりません", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            MediaPlayer().apply {
                setDataSource(audioFile.absolutePath)
                prepare()
                start()
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.JAPAN
            ready = true

            // 完了通知リスナ（重要）
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    // 開始時（必要ならUI更新）
                }

                override fun onDone(utteranceId: String?) {
                    // 保存完了 or 発話完了の判別
                    if (utteranceId == "utter_save") {
                        // ファイルが実際にできているか確認（length>0）
                        val exists = audioFile.exists() && audioFile.length() > 0
                        runOnUiThread {
                            if (exists) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "保存完了: ${audioFile.absolutePath}",
                                    Toast.LENGTH_LONG
                                ).show()
                                // 再生ボタンを有効化
                                findViewById<Button>(R.id.btnPlay).isEnabled = true
                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    "保存に失敗しました（ファイル空）",
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

    /**
     * 任意: internal filesDir にある audioFile をユーザーのDownloadにコピーして
     * 外部から聞けるようにする（Android Q+/MediaStore対応）。
     * 呼び出しは保存完了後に行うと安全。
     */
    private fun saveToDownloads(displayName: String = "tts.wav"): Boolean {
        try {
            val resolver = contentResolver
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, "audio/wav")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Download")
                }
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: return false
            resolver.openOutputStream(uri).use { out: OutputStream? ->
                FileInputStream(audioFile).use { input ->
                    input.copyTo(out!!)
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override fun onDestroy() {
        tts.shutdown()
        super.onDestroy()
    }
}
