package com.mdmac.fbop.data

import com.mdmac.fbop.model.FolderInfo
import java.util.UUID

class FolderManager(private val prefsManager: PrefsManager) {

    fun getAllFolders(): List<FolderInfo> = prefsManager.getFolders()

    fun getFolder(id: String): FolderInfo? =
        prefsManager.getFolders().find { it.id == id }

    /** Creates a new folder containing the two given apps (triggered by dragging one app onto another). */
    fun createFolder(firstComponentKey: String, secondComponentKey: String, pageIndex: Int, position: Int): FolderInfo {
        val folder = FolderInfo(
            id = UUID.randomUUID().toString(),
            name = "Folder",
            appComponentKeys = mutableListOf(firstComponentKey, secondComponentKey),
            pageIndex = pageIndex,
            position = position
        )
        val current = prefsManager.getFolders().toMutableList()
        current.add(folder)
        prefsManager.saveFolders(current)
        return folder
    }

    fun addAppToFolder(folderId: String, componentKey: String) {
        val folders = prefsManager.getFolders().toMutableList()
        val index = folders.indexOfFirst { it.id == folderId }
        if (index == -1) return
        val folder = folders[index]
        if (!folder.appComponentKeys.contains(componentKey)) {
            folder.appComponentKeys.add(componentKey)
        }
        folders[index] = folder
        prefsManager.saveFolders(folders)
    }

    fun removeAppFromFolder(folderId: String, componentKey: String) {
        val folders = prefsManager.getFolders().toMutableList()
        val index = folders.indexOfFirst { it.id == folderId }
        if (index == -1) return
        val folder = folders[index]
        folder.appComponentKeys.remove(componentKey)
        folders[index] = folder
        prefsManager.saveFolders(folders)

        if (folder.appComponentKeys.size <= 1 && !folder.isHiddenAppsFolder) {
            deleteFolder(folderId)
        }
    }

    fun renameFolder(folderId: String, newName: String) {
        val folders = prefsManager.getFolders().toMutableList()
        val index = folders.indexOfFirst { it.id == folderId }
        if (index == -1) return
        folders[index] = folders[index].copy(name = newName)
        prefsManager.saveFolders(folders)
    }

    fun setFolderIcon(folderId: String, iconUri: String?) {
        val folders = prefsManager.getFolders().toMutableList()
        val index = folders.indexOfFirst { it.id == folderId }
        if (index == -1) return
        folders[index] = folders[index].copy(iconUri = iconUri)
        prefsManager.saveFolders(folders)
    }

    fun deleteFolder(folderId: String) {
        val folders = prefsManager.getFolders().toMutableList()
        folders.removeAll { it.id == folderId }
        prefsManager.saveFolders(folders)
    }

    /** Gets or creates the special Hidden Apps folder. */
    fun getOrCreateHiddenFolder(): FolderInfo {
        val existing = prefsManager.getFolders().find { it.isHiddenAppsFolder }
        if (existing != null) return existing

        val folder = FolderInfo(
            id = "hidden_apps_folder",
            name = prefsManager.hiddenFolderName,
            iconUri = prefsManager.hiddenFolderIconUri,
            appComponentKeys = mutableListOf(),
            isHiddenAppsFolder = true
        )
        val current = prefsManager.getFolders().toMutableList()
        current.add(folder)
        prefsManager.saveFolders(current)
        return folder
    }
}
