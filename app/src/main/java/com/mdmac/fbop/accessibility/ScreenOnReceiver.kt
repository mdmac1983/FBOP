package com.mdmac.fbop.accessibility

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mdmac.fbop.data.PrefsManager

class ScreenOnReceiver : BroadcastReceiver() {

    companion object {
        private const val TRIPLE_TAP_WINDOW_MS = 2000L
        private var screenOnTimestamps = mutableListOf<Long>()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_SCREEN_ON) return

        val prefsManager = PrefsManager(context)
        if (!prefsManager.touchBlockTriplePowerEnabled) return

        val now = System.currentTimeMillis()
        screenOnTimestamps.add(now)
        screenOnTimestamps.removeAll { now - it > TRIPLE_TAP_WINDOW_MS }

        if (screenOnTimestamps.size >= 3) {
            screenOnTimestamps.clear()
            TouchBlockController(context).startTouchBlock()
        }
    }
}
