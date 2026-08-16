package com.mdmac.fbop.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.mdmac.fbop.model.AppInfo

class AppRepository(private val context: Context) {

    private val prefsManager = PrefsManager(context)

    /** All installed launchable apps, with custom label/icon overrides applied, hidden apps excluded. */
    fun getVisibleApps(): List<AppInfo> {
        val all = loadAllApps()
        val hidden = prefsManager.hiddenApps.toSet()
        return all.filter { !hidden.contains(it.componentKey) }
            .sortedBy { it.displayLabel.lowercase() }
    }

    /** All installed apps that are currently hidden (used to populate the Hidden Apps folder). */
    fun getHiddenApps(): List<AppInfo> {
        val all = loadAllApps()
        val hidden = prefsManager.hiddenApps.toSet()
        return all.filter { hidden.contains(it.componentKey) }
            .sortedBy { it.displayLabel.lowercase() }
    }

    fun getAppByComponentKey(componentKey: String): AppInfo? {
        return loadAllApps().find { it.componentKey == componentKey }
    }

    private fun loadAllApps(): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolvedApps = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

        return resolvedApps.map { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            val activityName = resolveInfo.activityInfo.name
            val componentKey = "$packageName/$activityName"

            AppInfo(
                packageName = packageName,
                activityName = activityName,
                systemLabel = resolveInfo.loadLabel(pm).toString(),
                systemIcon = resolveInfo.loadIcon(pm),
                customLabel = prefsManager.getCustomLabel(componentKey),
                customIconUri = prefsManager.getCustomIconUri(componentKey)
            )
        }
    }
}
