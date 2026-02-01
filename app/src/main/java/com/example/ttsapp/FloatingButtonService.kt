package com.example.ttsapp

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
import android.view.*
import android.widget.ImageView

class FloatingButtonService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        floatingView = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_wifi)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 100
        params.y = 300

        floatingView.setOnTouchListener(object : View.OnTouchListener {
            private var lastX = 0
            private var lastY = 0

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastX = event.rawX.toInt()
                        lastY = event.rawY.toInt()
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX.toInt() - lastX
                        val dy = event.rawY.toInt() - lastY
                        params.x += dx
                        params.y += dy
                        windowManager.updateViewLayout(floatingView, params)
                        lastX = event.rawX.toInt()
                        lastY = event.rawY.toInt()
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(floatingView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(floatingView)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
