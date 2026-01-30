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

        audioFile = File(filesDir, "tts.wav")

        tts = TextToSpeech(this, this)

        playButton.isEnabled = audioFile.exists() && audioFile.length() > 0
        exportButton.isEnabled = playButton.isEnabled

        speakButton.setOnClickListener {
            if (!ready) {
                toast("TTS 準備中です")
                return@setOnClickListener
            }

            val text = editText.text.toString()
            if (text.isBlank()) return@setOnClickListener

            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utter_speak")
        }

        saveButton.setOnClickListener {
            if (!ready) {
                toast("TTS 準備中です")
                return@setOnClickListener
            }

            val text = editText.text.toString()
            if (text.isBlank()) {
                toast("テキストが空です")
                return@setOnClickListener
            }

            tts.synthesizeToFile(text, null, audioFile, "utter_save")
            toast("保存開始…")
        }

        playButton.setOnClickListener {
            if (!audioFile.exists() || audioFile.length() == 0L) {
                toast("保存音声がありません")
                return@setOnClickListener
            }
            playFile(audioFile)
        }

        exportButton.setOnClickListener {
            if (!audioFile.exists() || audioFile.length() == 0L) {
                toast("保存音声がありません")
                return@setOnClickListener
            }
            copyToDownloadsSafe(audioFile)
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
                            if (audioFile.exists() && audioFile.length() > 0) {
                                toast("保存完了")
                                playButton.isEnabled = true
                                exportButton.isEnabled = true
                            } else {
                                toast("保存失敗（空ファイル）")
                            }
                        }
                    }
                }

                override fun onError(utteranceId: String?) {
                    runOnUiThread { toast("音声生成エラー") }
                }
            })
        } else {
            toast("TTS 初期化に失敗しました")
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
            toast("再生に失敗しました")
        }
    }

    private fun copyToDownloadsSafe(src: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = contentResolver
            val fileName = "tts_${System.currentTimeMillis()}.wav"

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "audio/wav")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)

            if (uri == null) {
                toast("Download 作成に失敗")
                return
            }

            resolver.openOutputStream(uri)?.use { out ->
                FileInputStream(src).use { input ->
                    input.copyTo(out)
                }
            }

            toast("Download に保存: $fileName")
        } else {
            toast("Android 10 未満は非対応")
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        tts.shutdown()
        super.onDestroy()
    }
}
