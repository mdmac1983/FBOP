package com.mdmac.fbop.data

import android.content.Context
import android.content.SharedPreferences
import com.mdmac.fbop.model.FolderInfo
import org.json.JSONArray
import org.json.JSONObject

class PrefsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("fbop_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HOME_COLUMNS = "home_columns"
        private const val KEY_HOME_ROWS = "home_rows"
        private const val KEY_DRAWER_COLUMNS = "drawer_columns"
        private const val KEY_DRAWER_ROWS = "drawer_rows"

        private const val KEY_PINNED_APPS = "pinned_apps"
        private const val KEY_PINNED_COUNT = "pinned_count"

        private const val KEY_HIDDEN_APPS = "hidden_apps"
        private const val KEY_HIDDEN_FOLDER_NAME = "hidden_folder_name"
        private const val KEY_HIDDEN_FOLDER_ICON = "hidden_folder_icon_uri"

        private const val KEY_FOLDERS = "folders"

        private const val KEY_CUSTOM_LABELS = "custom_labels"
        private const val KEY_CUSTOM_ICONS = "custom_icons"

        private const val KEY_THEME_MODE = "theme_mode"

        private const val KEY_SEARCH_BAR_ALPHA = "search_bar_alpha"

        private const val KEY_LAYOUT_LOCKED = "layout_locked"
        private const val KEY_STATUS_BAR_VISIBLE = "status_bar_visible"
        private const val KEY_HOME_PAGE_COUNT = "home_page_count"

        private const val KEY_SETTINGS_VIA_DRAWER = "settings_via_drawer"
        private const val KEY_SETTINGS_VIA_LONGPRESS = "settings_via_longpress"

        private const val KEY_LONGPRESS_SHOW_HOME_OPTIONS = "longpress_show_home_options"
        private const val KEY_LONGPRESS_SHOW_APP_OPTIONS = "longpress_show_app_options"

        private const val KEY_TOUCHBLOCK_GESTURE_ENABLED = "touchblock_gesture_enabled"
        private const val KEY_TOUCHBLOCK_TRIPLE_POWER_ENABLED = "touchblock_triple_power_enabled"
    }

    // ---------- Grid dimensions ----------

    var homeColumns: Int
        get() = prefs.getInt(KEY_HOME_COLUMNS, 4)
        set(value) = prefs.edit().putInt(KEY_HOME_COLUMNS, value).apply()

    var homeRows: Int
        get() = prefs.getInt(KEY_HOME_ROWS, 5)
        set(value) = prefs.edit().putInt(KEY_HOME_ROWS, value).apply()

    var drawerColumns: Int
        get() = prefs.getInt(KEY_DRAWER_COLUMNS, 4)
        set(value) = prefs.edit().putInt(KEY_DRAWER_COLUMNS, value).apply()

    var drawerRows: Int
        get() = prefs.getInt(KEY_DRAWER_ROWS, 6)
        set(value) = prefs.edit().putInt(KEY_DRAWER_ROWS, value).apply()

    // ---------- Pinned row ----------

    var pinnedCount: Int
        get() = prefs.getInt(KEY_PINNED_COUNT, 5)
        set(value) = prefs.edit().putInt(KEY_PINNED_COUNT, value).apply()

    var pinnedApps: List<String>
        get() = jsonArrayToList(prefs.getString(KEY_PINNED_APPS, null))
        set(value) = prefs.edit().putString(KEY_PINNED_APPS, listToJsonArray(value)).apply()

    // ---------- Hidden apps ----------

    var hiddenApps: List<String>
        get() = jsonArrayToList(prefs.getString(KEY_HIDDEN_APPS, null))
        set(value) = prefs.edit().putString(KEY_HIDDEN_APPS, listToJsonArray(value)).apply()

    var hiddenFolderName: String
        get() = prefs.getString(KEY_HIDDEN_FOLDER_NAME, "Hidden") ?: "Hidden"
        set(value) = prefs.edit().putString(KEY_HIDDEN_FOLDER_NAME, value).apply()

    var hiddenFolderIconUri: String?
        get() = prefs.getString(KEY_HIDDEN_FOLDER_ICON, null)
        set(value) = prefs.edit().putString(KEY_HIDDEN_FOLDER_ICON, value).apply()

    fun addHiddenApp(componentKey: String) {
        val current = hiddenApps.toMutableList()
        if (!current.contains(componentKey)) {
            current.add(componentKey)
            hiddenApps = current
        }
    }

    fun removeHiddenApp(componentKey: String) {
        val current = hiddenApps.toMutableList()
        current.remove(componentKey)
        hiddenApps = current
    }

    // ---------- Folders ----------

    fun getFolders(): List<FolderInfo> {
        val raw = prefs.getString(KEY_FOLDERS, null) ?: return emptyList()
        val array = JSONArray(raw)
        val result = mutableListOf<FolderInfo>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val apps = mutableListOf<String>()
            val appsArray = obj.getJSONArray("apps")
            for (j in 0 until appsArray.length()) apps.add(appsArray.getString(j))

            result.add(
                FolderInfo(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    iconUri = if (obj.has("iconUri") && !obj.isNull("iconUri")) obj.getString("iconUri") else null,
                    appComponentKeys = apps,
                    pageIndex = obj.optInt("pageIndex", 0),
                    position = obj.optInt("position", 0),
                    isHiddenAppsFolder = obj.optBoolean("isHiddenAppsFolder", false)
                )
            )
        }
        return result
    }

    fun saveFolders(folders: List<FolderInfo>) {
        val array = JSONArray()
        folders.forEach { folder ->
            val obj = JSONObject()
            obj.put("id", folder.id)
            obj.put("name", folder.name)
            obj.put("iconUri", folder.iconUri)
            obj.put("apps", JSONArray(folder.appComponentKeys))
            obj.put("pageIndex", folder.pageIndex)
            obj.put("position", folder.position)
            obj.put("isHiddenAppsFolder", folder.isHiddenAppsFolder)
            array.put(obj)
        }
        prefs.edit().putString(KEY_FOLDERS, array.toString()).apply()
    }

    // ---------- Custom labels / icons ----------

    fun getCustomLabel(componentKey: String): String? {
        val obj = JSONObject(prefs.getString(KEY_CUSTOM_LABELS, "{}") ?: "{}")
        return if (obj.has(componentKey)) obj.getString(componentKey) else null
    }

    fun setCustomLabel(componentKey: String, label: String?) {
        val obj = JSONObject(prefs.getString(KEY_CUSTOM_LABELS, "{}") ?: "{}")
        if (label == null) obj.remove(componentKey) else obj.put(componentKey, label)
        prefs.edit().putString(KEY_CUSTOM_LABELS, obj.toString()).apply()
    }

    fun getCustomIconUri(componentKey: String): String? {
        val obj = JSONObject(prefs.getString(KEY_CUSTOM_ICONS, "{}") ?: "{}")
        return if (obj.has(componentKey)) obj.getString(componentKey) else null
    }

    fun setCustomIconUri(componentKey: String, uri: String?) {
        val obj = JSONObject(prefs.getString(KEY_CUSTOM_ICONS, "{}") ?: "{}")
        if (uri == null) obj.remove(componentKey) else obj.put(componentKey, uri)
        prefs.edit().putString(KEY_CUSTOM_ICONS, obj.toString()).apply()
    }

    // ---------- Theme ----------

    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, "light") ?: "light"
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    // ---------- Search bar ----------

    var searchBarAlpha: Int
        get() = prefs.getInt(KEY_SEARCH_BAR_ALPHA, 180)
        set(value) = prefs.edit().putInt(KEY_SEARCH_BAR_ALPHA, value).apply()

    // ---------- Home screen behavior ----------

    var layoutLocked: Boolean
        get() = prefs.getBoolean(KEY_LAYOUT_LOCKED, false)
        set(value) = prefs.edit().putBoolean(KEY_LAYOUT_LOCKED, value).apply()

    var statusBarVisible: Boolean
        get() = prefs.getBoolean(KEY_STATUS_BAR_VISIBLE, true)
        set(value) = prefs.edit().putBoolean(KEY_STATUS_BAR_VISIBLE, value).apply()

    var homePageCount: Int
        get() = prefs.getInt(KEY_HOME_PAGE_COUNT, 1)
        set(value) = prefs.edit().putInt(KEY_HOME_PAGE_COUNT, value).apply()

    // ---------- Settings access ----------

    var settingsAccessibleViaDrawer: Boolean
        get() = prefs.getBoolean(KEY_SETTINGS_VIA_DRAWER, true)
        set(value) = prefs.edit().putBoolean(KEY_SETTINGS_VIA_DRAWER, value).apply()

    var settingsAccessibleViaLongPress: Boolean
        get() = prefs.getBoolean(KEY_SETTINGS_VIA_LONGPRESS, true)
        set(value) = prefs.edit().putBoolean(KEY_SETTINGS_VIA_LONGPRESS, value).apply()

    // ---------- Long-press menu ----------

    var longPressShowHomeOptions: Boolean
        get() = prefs.getBoolean(KEY_LONGPRESS_SHOW_HOME_OPTIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_LONGPRESS_SHOW_HOME_OPTIONS, value).apply()

    var longPressShowAppOptions: Boolean
        get() = prefs.getBoolean(KEY_LONGPRESS_SHOW_APP_OPTIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_LONGPRESS_SHOW_APP_OPTIONS, value).apply()

    // ---------- Accessibility touch-block ----------

    var touchBlockGestureEnabled: Boolean
        get() = prefs.getBoolean(KEY_TOUCHBLOCK_GESTURE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_TOUCHBLOCK_GESTURE_ENABLED, value).apply()

    var touchBlockTriplePowerEnabled: Boolean
        get() = prefs.getBoolean(KEY_TOUCHBLOCK_TRIPLE_POWER_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_TOUCHBLOCK_TRIPLE_POWER_ENABLED, value).apply()

    // ---------- JSON helpers ----------

    private fun listToJsonArray(list: List<String>): String {
        val array = JSONArray()
        list.forEach { array.put(it) }
        return array.toString()
    }

    private fun jsonArrayToList(raw: String?): List<String> {
        if (raw == null) return emptyList()
        val array = JSONArray(raw)
        val result = mutableListOf<String>()
        for (i in 0 until array.length()) result.add(array.getString(i))
        return result
    }
}
