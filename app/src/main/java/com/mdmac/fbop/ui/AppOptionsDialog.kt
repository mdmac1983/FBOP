package com.mdmac.fbop.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.mdmac.fbop.data.PrefsManager
import com.mdmac.fbop.model.AppInfo

class AppOptionsDialog(
    private val context: Context,
    private val prefsManager: PrefsManager,
    private val onChanged: () -> Unit,
    private val onPickCustomIcon: (AppInfo) -> Unit
) {

    fun show(app: AppInfo) {
        if (!prefsManager.longPressShowAppOptions) return

        val isPinned = prefsManager.pinnedApps.contains(app.componentKey)
        val isHidden = prefsManager.hiddenApps.contains(app.componentKey)

        val options = mutableListOf<String>().apply {
            add(if (isPinned) "Unpin from bottom row" else "Pin to bottom row")
            add(if (isHidden) "Unhide app" else "Hide app")
            add("Edit label")
            add("Edit icon")
            add("App info")
            add("Uninstall")
        }

        AlertDialog.Builder(context)
            .setTitle(app.displayLabel)
            .setItems(options.toTypedArray()) { _, which ->
                when (options[which]) {
                    "Pin to bottom row" -> pin(app)
                    "Unpin from bottom row" -> unpin(app)
                    "Hide app" -> hide(app)
                    "Unhide app" -> unhide(app)
                    "Edit label" -> promptEditLabel(app)
                    "Edit icon" -> onPickCustomIcon(app)
                    "App info" -> openAppInfo(app)
                    "Uninstall" -> uninstall(app)
                }
            }
            .show()
    }

    private fun pin(app: AppInfo) {
        val current = prefsManager.pinnedApps.toMutableList()
        if (!current.contains(app.componentKey)) {
            if (current.size >= prefsManager.pinnedCount) {
                current.removeAt(0)
            }
            current.add(app.componentKey)
            prefsManager.pinnedApps = current
            onChanged()
        }
    }

    private fun unpin(app: AppInfo) {
        val current = prefsManager.pinnedApps.toMutableList()
        current.remove(app.componentKey)
        prefsManager.pinnedApps = current
        onChanged()
    }

    private fun hide(app: AppInfo) {
        prefsManager.addHiddenApp(app.componentKey)
        onChanged()
    }

    private fun unhide(app: AppInfo) {
        prefsManager.removeHiddenApp(app.componentKey)
        onChanged()
    }

    private fun promptEditLabel(app: AppInfo) {
        val input = EditText(context).apply { setText(app.displayLabel) }
        AlertDialog.Builder(context)
            .setTitle("Edit label")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newLabel = input.text.toString().trim()
                prefsManager.setCustomLabel(app.componentKey, if (newLabel.isEmpty()) null else newLabel)
                onChanged()
            }
            .setNeutralButton("Reset to default") { _, _ ->
                prefsManager.setCustomLabel(app.componentKey, null)
                onChanged()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun applyCustomIcon(app: AppInfo, uri: Uri) {
        prefsManager.setCustomIconUri(app.componentKey, uri.toString())
        onChanged()
    }

    private fun openAppInfo(app: AppInfo) {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${app.packageName}")
        }
        context.startActivity(intent)
    }

    private fun uninstall(app: AppInfo) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:${app.packageName}")
        }
        context.startActivity(intent)
    }
}
