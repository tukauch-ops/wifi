package com.example.ttsapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_TEXT_FILE = 1;
    private TextToSpeech tts;
    private String fileContent = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(this, status -> tts.setLanguage(Locale.JAPANESE));

        Button pickButton = findViewById(R.id.pickFileButton);
        pickButton.setOnClickListener(v -> openFilePicker());

        Button readButton = findViewById(R.id.readButton);
        readButton.setOnClickListener(v -> {
            if (!fileContent.isEmpty()) {
                tts.speak(fileContent, TextToSpeech.QUEUE_FLUSH, null, "tts1");
            }
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("text/*");
        startActivityForResult(intent, PICK_TEXT_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_TEXT_FILE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                readFile(uri);
            }
        }
    }

    private void readFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(" ");
            }
            reader.close();
            fileContent = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
                                                       }
