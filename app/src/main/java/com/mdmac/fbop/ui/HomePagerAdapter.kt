package com.mdmac.fbop.ui

import android.content.Context
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mdmac.fbop.R
import com.mdmac.fbop.model.AppInfo
import com.mdmac.fbop.model.FolderInfo
import com.mdmac.fbop.model.HomeGridItem
import kotlin.math.abs

class HomePagerAdapter(
    private val pages: List<MutableList<HomeGridItem>>,
    private val columns: Int,
    private val appLookup: (String) -> AppInfo?,
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppOptionsRequested: (AppInfo) -> Unit,
    private val onFolderClick: (FolderInfo) -> Unit,
    private val onFolderOptionsRequested: (FolderInfo) -> Unit,
    private val onMergeRequested: (pageIndex: Int, fromItem: HomeGridItem, toItem: HomeGridItem) -> Unit,
    private val onReordered: (pageIndex: Int, newOrder: List<HomeGridItem>) -> Unit,
    private val onSwipeUpRequested: () -> Unit,
    private val onEmptySpaceLongPress: () -> Unit,
    private val onTwoFingerLongPress: () -> Unit
) : RecyclerView.Adapter<HomePagerAdapter.PageViewHolder>() {

    companion object {
        private const val SWIPE_UP_THRESHOLD = 100
        private const val SWIPE_UP_VELOCITY_THRESHOLD = 100
    }

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

        setupGestureWatcher(holder, context)
    }

    private fun setupGestureWatcher(holder: PageViewHolder, context: Context) {
        var activePointerCount = 1

        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onDown(e: MotionEvent): Boolean {
                Toast.makeText(context, "DEBUG: touch down received", Toast.LENGTH_SHORT).show()
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val childUnderTouch = holder.grid.findChildViewUnder(e.x, e.y)
                Toast.makeText(
                    context,
                    "DEBUG: long press fired, childUnderTouch=${childUnderTouch != null}, pointers=$activePointerCount",
                    Toast.LENGTH_LONG
                ).show()

                if (childUnderTouch != null) return

                if (activePointerCount >= 2) {
                    onTwoFingerLongPress()
                } else {
                    onEmptySpaceLongPress()
                }
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false
                val deltaY = e2.y - e1.y
                val deltaX = e2.x - e1.x

                Toast.makeText(
                    context,
                    "DEBUG: fling deltaY=${deltaY.toInt()} velocityY=${velocityY.toInt()}",
                    Toast.LENGTH_LONG
                ).show()

                if (abs(deltaY) > abs(deltaX) &&
                    deltaY < -SWIPE_UP_THRESHOLD &&
                    abs(velocityY) > SWIPE_UP_VELOCITY_THRESHOLD
                ) {
                    onSwipeUpRequested()
                    return true
                }
                return false
            }
        })

        holder.grid.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.actionMasked) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN ->
                        activePointerCount = e.pointerCount
                    MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP ->
                        activePointerCount = (e.pointerCount - 1).coerceAtLeast(1)
                }
                gestureDetector.onTouchEvent(e)
                return false
            }
        })
    }

    override fun getItemCount() = pages.size
}
