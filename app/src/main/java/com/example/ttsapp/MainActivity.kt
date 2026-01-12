package com.example.ttsapp

import android.os.Build
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
    private lateinit var editText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText = findViewById(R.id.editText)
        saveButton = findViewById(R.id.saveButton)

        tts = TextToSpeech(this, this)

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "保存中…", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onDone(utteranceId: String?) {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "WAV保存完了（内部保存）",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onError(utteranceId: String?) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "保存失敗", Toast.LENGTH_LONG).show()
                }
            }
        })

        saveButton.setOnClickListener {
            val text = editText.text.toString()
            if (text.isNotEmpty()) {
                saveWav(text)
            } else {
                Toast.makeText(this, "文字を入力してください", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.JAPAN
        } else {
            Toast.makeText(this, "TTS初期化失敗", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveWav(text: String) {
        val outFile = File(filesDir, "tts.wav")

        Toast.makeText(this, "保存開始", Toast.LENGTH_SHORT).show()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val params = Bundle()
            tts.synthesizeToFile(text, params, outFile, "tts1")
        } else {
            @Suppress("DEPRECATION")
            tts.synthesizeToFile(text, null, outFile.absolutePath)
        }
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}
