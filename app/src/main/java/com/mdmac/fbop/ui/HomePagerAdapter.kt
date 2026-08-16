package com.mdmac.fbop.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mdmac.fbop.R
import com.mdmac.fbop.model.AppInfo
import com.mdmac.fbop.model.FolderInfo
import com.mdmac.fbop.model.HomeGridItem

class HomePagerAdapter(
    private val pages: List<MutableList<HomeGridItem>>,
    private val columns: Int,
    private val appLookup: (String) -> AppInfo?,
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppOptionsRequested: (AppInfo) -> Unit,
    private val onFolderClick: (FolderInfo) -> Unit,
    private val onFolderOptionsRequested: (FolderInfo) -> Unit,
    private val onMergeRequested: (pageIndex: Int, fromItem: HomeGridItem, toItem: HomeGridItem) -> Unit,
    private val onReordered: (pageIndex: Int, newOrder: List<HomeGridItem>) -> Unit
) : RecyclerView.Adapter<HomePagerAdapter.PageViewHolder>() {

    class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val grid: RecyclerView = view.findViewById(R.id.homePageGrid)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.page_home, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val context = holder.itemView.context
        val pageItems = pages[position]

        lateinit var touchHelper: ItemTouchHelper

        val adapter = HomeGridAdapter(
            context = context,
            items = pageItems,
            appLookup = appLookup,
            onAppClick = onAppClick,
            onAppDragStart = { viewHolder -> touchHelper.startDrag(viewHolder) },
            onFolderClick = onFolderClick,
            onFolderDragStart = { viewHolder -> touchHelper.startDrag(viewHolder) }
        )

        val callback = HomeGridDragCallback(
            adapter = adapter,
            onReordered = { onReordered(position, adapter.getItems()) },
            onMergeRequested = { fromPos, toPos ->
                val fromItem = adapter.getItemAt(fromPos)
                val toItem = adapter.getItemAt(toPos)
                if (fromItem != null && toItem != null) {
                    onMergeRequested(position, fromItem, toItem)
                }
            },
            onLongPressReleasedWithoutMove = { pos ->
                when (val item = adapter.getItemAt(pos)) {
                    is HomeGridItem.AppItem -> onAppOptionsRequested(item.app)
                    is HomeGridItem.FolderItem -> onFolderOptionsRequested(item.folder)
                    null -> {}
                }
            }
        )
        touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(holder.grid)

        holder.grid.layoutManager = GridLayoutManager(context, columns)
        holder.grid.adapter = adapter
    }

    override fun getItemCount() = pages.size
}
