package com.mdmac.fbop.accessibility

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils

class TouchBlockController(private val context: Context) {

    fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponent = ComponentName(context, TouchBlockAccessibilityService::class.java)
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        if (TextUtils.isEmpty(enabledServices)) return false

        return enabledServices.split(":").any { serviceString ->
            ComponentName.unflattenFromString(serviceString) == expectedComponent
        }
    }

    fun openAccessibilitySettings() {
        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    fun startTouchBlock() {
        if (!isAccessibilityServiceEnabled()) return
        TouchBlockAccessibilityService.instance?.enableTouchBlock()
    }

    fun stopTouchBlock() {
        TouchBlockAccessibilityService.instance?.disableTouchBlock()
    }

    fun isCurrentlyBlocking(): Boolean {
        return TouchBlockAccessibilityService.instance?.isBlocking() ?: false
    }
}
