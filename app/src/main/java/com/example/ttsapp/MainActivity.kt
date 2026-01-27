package com.example.ttsapp2

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Toast.makeText(this, "HELLO 起動成功", Toast.LENGTH_LONG).show()

        setContentView(android.R.layout.simple_list_item_1)
    }
}
