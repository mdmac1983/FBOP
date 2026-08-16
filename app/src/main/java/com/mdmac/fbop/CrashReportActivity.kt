package com.mdmac.fbop

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CrashReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash_report)

        val details = intent.getStringExtra("crash_details") ?: "No crash details available."
        findViewById<TextView>(R.id.crashDetailsText).text = details
    }
}
