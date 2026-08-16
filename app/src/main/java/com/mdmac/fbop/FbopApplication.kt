package com.mdmac.fbop

import android.app.Application
import android.content.Intent

class FbopApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val stackTrace = android.util.Log.getStackTraceString(throwable)

                val intent = Intent(applicationContext, CrashReportActivity::class.java).apply {
                    putExtra("crash_details", stackTrace)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            } catch (e: Exception) {
                // If even the crash reporter fails, fall back to default behavior
            }

            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(1)
        }
    }
}
