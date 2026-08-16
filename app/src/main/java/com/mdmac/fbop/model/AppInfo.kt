package com.mdmac.fbop.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val activityName: String,
    val systemLabel: String,
    val systemIcon: Drawable,
    var customLabel: String? = null,
    var customIconUri: String? = null
) {
    val displayLabel: String
        get() = customLabel ?: systemLabel

    val componentKey: String
        get() = "$packageName/$activityName"
}
