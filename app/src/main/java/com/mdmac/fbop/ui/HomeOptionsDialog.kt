package com.mdmac.fbop.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import com.mdmac.fbop.SettingsActivity
import com.mdmac.fbop.data.PrefsManager

class HomeOptionsDialog(
    private val context: Context,
    private val prefsManager: PrefsManager
) {

    fun show() {
        val options = mutableListOf("Change wallpaper")
        if (prefsManager.settingsAccessibleViaLongPress) {
            options.add("Settings")
        }

        AlertDialog.Builder(context)
            .setTitle("Home screen")
            .setItems(options.toTypedArray()) { _, which ->
                when (options[which]) {
                    "Change wallpaper" -> openWallpaperPicker()
                    "Settings" -> openSettings()
                }
            }
            .show()
    }

    private fun openWallpaperPicker() {
        val intent = Intent(Intent.ACTION_SET_WALLPAPER)
        context.startActivity(Intent.createChooser(intent, "Set wallpaper"))
    }

    private fun openSettings() {
        context.startActivity(Intent(context, SettingsActivity::class.java))
    }
}
