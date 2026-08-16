package com.mdmac.fbop.ui

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mdmac.fbop.R
import com.mdmac.fbop.model.AppInfo
import com.mdmac.fbop.model.FolderInfo
import com.mdmac.fbop.model.HomeGridItem

class HomeGridAdapter(
    private val context: Context,
    private var items: MutableList<HomeGridItem>,
    private val appLookup: (String) -> AppInfo?,
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppDragStart: (RecyclerView.ViewHolder) -> Unit,
    private val onFolderClick: (FolderInfo) -> Unit,
    private val onFolderDragStart: (RecyclerView.ViewHolder) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_APP = 0
        private const val TYPE_FOLDER = 1
    }

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val label: TextView = view.findViewById(R.id.appLabel)
    }

    class FolderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val customIcon: ImageView = view.findViewById(R.id.folderCustomIcon)
        val previewGrid: GridLayout = view.findViewById(R.id.folderPreviewGrid)
        val label: TextView = view.findViewById(R.id.folderLabel)
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is HomeGridItem.AppItem -> TYPE_APP
        is HomeGridItem.FolderItem -> TYPE_FOLDER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        return if (viewType == TYPE_APP) {
            AppViewHolder(inflater.inflate(R.layout.item_app_icon, parent, false))
        } else {
            FolderViewHolder(inflater.inflate(R.layout.item_folder_icon, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is HomeGridItem.AppItem -> bindApp(holder as AppViewHolder, item.app)
            is HomeGridItem.FolderItem -> bindFolder(holder as FolderViewHolder, item.folder)
        }
    }

    private fun bindApp(holder: AppViewHolder, app: AppInfo) {
        holder.label.text = app.displayLabel
        setIcon(holder.icon, app)

        holder.itemView.setOnClickListener { onAppClick(app) }
        holder.itemView.setOnLongClickListener {
            onAppDragStart(holder)
            true
        }
    }

    private fun bindFolder(holder: FolderViewHolder, folder: FolderInfo) {
        holder.label.text = folder.name

        if (folder.iconUri != null) {
            holder.customIcon.visibility = View.VISIBLE
            holder.previewGrid.visibility = View.GONE
            try {
                holder.customIcon.setImageURI(Uri.parse(folder.iconUri))
            } catch (e: Exception) {
                holder.customIcon.visibility = View.GONE
                holder.previewGrid.visibility = View.VISIBLE
            }
        } else {
            holder.customIcon.visibility = View.GONE
            holder.previewGrid.visibility = View.VISIBLE
            holder.previewGrid.removeAllViews()

            folder.appComponentKeys.take(4).mapNotNull { appLookup(it) }.forEach { app ->
                val mini = ImageView(context).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 18
                        height = 18
                    }
                }
                setIcon(mini, app)
                holder.previewGrid.addView(mini)
            }
        }

        holder.itemView.setOnClickListener { onFolderClick(folder) }
        holder.itemView.setOnLongClickListener {
            onFolderDragStart(holder)
            true
        }
    }

    private fun setIcon(imageView: ImageView, app: AppInfo) {
        if (app.customIconUri != null) {
            try {
                imageView.setImageURI(Uri.parse(app.customIconUri))
                return
            } catch (e: Exception) {
                // fall through to system icon
            }
        }
        imageView.setImageDrawable(app.systemIcon)
    }

    override fun getItemCount() = items.size

    fun getItems(): List<HomeGridItem> = items

    fun getItemAt(position: Int): HomeGridItem? = items.getOrNull(position)

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val item = items.removeAt(fromPosition)
        items.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
    }
}
