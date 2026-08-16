package com.mdmac.fbop.accessibility

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import android.widget.TextView

class TouchBlockAccessibilityService : AccessibilityService() {

    private var overlayView: View? = null
    private var unlockTapCounter = 0
    private var lastTapTime = 0L

    companion object {
        private const val UNLOCK_TAP_COUNT = 5
        private const val UNLOCK_WINDOW_MS = 1500L

        var instance: TouchBlockAccessibilityService? = null
            private set

        val isServiceRunning: Boolean
            get() = instance != null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used — this service only exists to host the overlay window
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
        instance = null
    }

    fun enableTouchBlock() {
        if (overlayView != null) return

        val container = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#01000000"))
        }

        val hintText = TextView(this).apply {
            text = "Touch blocked — tap 5 times quickly to unlock"
            setTextColor(Color.WHITE)
            textSize = 14f
            setBackgroundColor(Color.parseColor("#88000000"))
            setPadding(24, 12, 24, 12)
        }
        val hintParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        ).apply { bottomMargin = 48 }
        container.addView(hintText, hintParams)

        container.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                handleUnlockTap()
            }
            true
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(container, params)
        overlayView = container
    }

    private fun handleUnlockTap() {
        val now = System.currentTimeMillis()
        if (now - lastTapTime > UNLOCK_WINDOW_MS) {
            unlockTapCounter = 0
        }
        lastTapTime = now
        unlockTapCounter++

        if (unlockTapCounter >= UNLOCK_TAP_COUNT) {
            disableTouchBlock()
        }
    }

    fun disableTouchBlock() {
        removeOverlay()
    }

    fun isBlocking(): Boolean = overlayView != null

    private fun removeOverlay() {
        val view = overlayView ?: return
        try {
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            windowManager.removeView(view)
        } catch (e: Exception) {
            // View already removed
        }
        overlayView = null
        unlockTapCounter = 0
    }
}
