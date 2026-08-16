package com.mdmac.fbop.model

data class FolderInfo(
    val id: String,
    var name: String,
    var iconUri: String? = null,
    val appComponentKeys: MutableList<String> = mutableListOf(),
    var pageIndex: Int = 0,
    var position: Int = 0,
    val isHiddenAppsFolder: Boolean = false
)
