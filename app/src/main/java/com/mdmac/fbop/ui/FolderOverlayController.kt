package com.mdmac.fbop.ui

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mdmac.fbop.data.AppRepository
import com.mdmac.fbop.data.FolderManager
import com.mdmac.fbop.model.AppInfo
import com.mdmac.fbop.model.FolderInfo

class FolderOverlayController(
    private val context: Context,
    private val overlayRoot: View,
    private val folderTitle: TextView,
    private val folderContentsGrid: RecyclerView,
    private val folderManager: FolderManager,
    private val appRepository: AppRepository,
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppLongClick: (AppInfo, View) -> Unit,
    private val onFolderChanged: () -> Unit
) {

    private var currentFolder: FolderInfo? = null

    init {
        overlayRoot.setOnClickListener { close() }
        folderTitle.setOnClickListener {
            val folder = currentFolder
            if (folder != null && !folder.isHiddenAppsFolder) {
                promptRename()
            }
        }
    }

    fun open(folder: FolderInfo) {
        currentFolder = folder
        folderTitle.text = folder.name

        val allApps = appRepository.getVisibleApps() + appRepository.getHiddenApps()
        val folderApps = folder.appComponentKeys.mapNotNull { key ->
            allApps.find { it.componentKey == key }
        }

        folderContentsGrid.layoutManager = GridLayoutManager(context, 4)
        folderContentsGrid.adapter = AppAdapter(
            context = context,
            apps = folderApps,
            onAppClick = onAppClick,
            onAppLongClick = onAppLongClick
        )

        overlayRoot.visibility = View.VISIBLE
    }

    fun close() {
        overlayRoot.visibility = View.GONE
        currentFolder = null
    }

    fun isOpen(): Boolean = overlayRoot.visibility == View.VISIBLE

    private fun promptRename() {
        val folder = currentFolder ?: return
        val input = EditText(context).apply { setText(folder.name) }

        AlertDialog.Builder(context)
            .setTitle("Rename folder")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    folderManager.renameFolder(folder.id, newName)
                    folderTitle.text = newName
                    onFolderChanged()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
