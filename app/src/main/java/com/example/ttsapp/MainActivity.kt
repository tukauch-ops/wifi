package com.example.ttsapp2

import android.content.ContentValues
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStream
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var ready = false
    private lateinit var audioFile: File

    private lateinit var editText: EditText
    private lateinit var speakButton: Button
    private lateinit var saveButton: Button
    private lateinit var playButton: Button
    private lateinit var exportButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText = findViewById(R.id.editText)
        speakButton = findViewById(R.id.buttonSpeak)
        saveButton = findViewById(R.id.buttonSave)
        playButton = findViewById(R.id.buttonPlay)
        exportButton = findViewById(R.id.buttonExport)

        // filesDir 内のファイル（アプリ専用・権限不要）
        audioFile = File(filesDir, "tts.wav")

        // TTS 初期化（onInit で ready=true になる）
        tts = TextToSpeech(this, this)

        // UI 初期状態
        playButton.isEnabled = audioFile.exists() && audioFile.length() > 0
        exportButton.isEnabled = playButton.isEnabled

        // 貼り付け安定化
        editText.isFocusableInTouchMode = true
        editText.requestFocus()

        // 読み上げ（その場で発話）
        speakButton.setOnClickListener {
            if (!ready) {
                Toast.makeText(this, "TTS 準備中です", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val text = editText.text.toString()
            if (text.isBlank()) return@setOnClickListener
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utter_speak")
        }

        // 保存（filesDir 内に非同期で合成）
        saveButton.setOnClickListener {
            if (!ready) {
                Toast.makeText(this, "TTS 準備中です", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val text = editText.text.toString()
            if (text.isBlank()) {
                Toast.makeText(this, "テキストが空です", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                // 非同期で合成 → 完了は onDone("utter_save") で検出
                tts.synthesizeToFile(text, null, audioFile, "utter_save")
                Toast.makeText(this, "保存を開始しました…", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "保存処理に失敗しました", Toast.LENGTH_SHORT).show()
            }
        }

        // 再生（アプリ内部ファイルを直接再生）
        playButton.setOnClickListener {
            if (!audioFile.exists() || audioFile.length() == 0L) {
                Toast.makeText(this, "保存音声がありません", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            playFile(audioFile)
        }

        // エクスポート（Download にコピーして外部アプリから見えるようにする）
        exportButton.setOnClickListener {
            if (!audioFile.exists() || audioFile.length() == 0L) {
                Toast.makeText(this, "保存音声がありません", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            copyToDownloadsSafe(audioFile)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // 言語をセット（成功判定は必要なら追加）
            tts.language = Locale.JAPAN
            ready = true

            // 完了通知を受け取る（重要）
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) { /* 必要ならUI更新 */ }

                override fun onDone(utteranceId: String?) {
                    // 保存完了の合図は utteranceId == "utter_save"
                    if (utteranceId == "utter_save") {
                        val ok = audioFile.exists() && audioFile.length() > 0
                        runOnUiThread {
                            if (ok) {
                                Toast.makeText(this@MainActivity, "保存完了（内部）", Toast.LENGTH_SHORT).show()
                                playButton.isEnabled = true
                                exportButton.isEnabled = true
                            } else {
                                Toast.makeText(this@MainActivity, "保存に失敗しました（ファイル空）", Toast.LENGTH_LONG).show()
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

    private fun playFile(file: File) {
        try {
            val player = MediaPlayer()
            player.setDataSource(file.absolutePath)
            player.prepare()
            player.start()
            player.setOnCompletionListener { it.release() }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "再生に失敗しました", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * API29+ : MediaStore (Downloads) を使って安全にコピー（権限不要）
     * API28以下: このコードは案内のみ（古い端末で外部に書く場合は権限フローが必要）
     */
    private fun copyToDownloadsSafe(src: File) {
        if (!src.exists() || src.length() == 0L) {
            Toast.makeText(this, "コピー元が存在しません", Toast.LENGTH_SHORT).show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = contentResolver
            val fileName = "tts_${System.currentTimeMillis()}.wav"
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "audio/wav")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            try {
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri == null) {
                    Toast.makeText(this, "コピー用URIの生成に失敗しました", Toast.LENGTH_SHORT).show()
                    return
                }
                resolver.openOutputStream(uri).use { out: OutputStream? ->
                    FileInputStream(src).use { input ->
                        if (out == null) throw IOException("OutputStream is null")
                        input.copyTo(out)
                        out.flush()
                    }
                }
                Toast.makeText(this, "Download にコピーしました: $fileName", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "コピーに失敗しました: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // 古い端末では権限フローが必要になるため、ここでは案内だけにする
            runOnUiThread {
                Toast.makeText(
                    this,
                    "この端末では自動コピーはサポートしていません（Android 10 未満）",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroy() {
        tts.shutdown()
        super.onDestroy()
    }
}
