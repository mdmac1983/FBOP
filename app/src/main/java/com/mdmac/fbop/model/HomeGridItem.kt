package com.mdmac.fbop.model

sealed class HomeGridItem {
    data class AppItem(val app: AppInfo) : HomeGridItem()
    data class FolderItem(val folder: FolderInfo) : HomeGridItem()
}
