package com.mdmac.fbop.ui

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class HomeGridDragCallback(
    private val adapter: HomeGridAdapter,
    private val onReordered: () -> Unit,
    private val onMergeRequested: (fromPosition: Int, toPosition: Int) -> Unit,
    private val onLongPressReleasedWithoutMove: (position: Int) -> Unit
) : ItemTouchHelper.Callback() {

    private var pendingMergeTarget: Int? = null
    private var dragStartPosition: Int? = null

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        return makeMovementFlags(dragFlags, 0)
    }

    override fun isLongPressDragEnabled(): Boolean = false // we start drag manually

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {
            dragStartPosition = viewHolder.bindingAdapterPosition
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val from = viewHolder.bindingAdapterPosition
        val to = target.bindingAdapterPosition
        if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) return false

        val overlapRatio = computeOverlapRatio(viewHolder, target)

        if (overlapRatio > 0.6f) {
            pendingMergeTarget = to
            return false
        }

        pendingMergeTarget = null
        adapter.moveItem(from, to)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Swipe-to-dismiss disabled
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        val from = dragStartPosition
        val finalPosition = viewHolder.bindingAdapterPosition
        val target = pendingMergeTarget

        when {
            target != null && from != null && from != target -> {
                onMergeRequested(from, target)
            }
            from != null && finalPosition == from -> {
                onLongPressReleasedWithoutMove(finalPosition)
            }
            else -> {
                onReordered()
            }
        }

        pendingMergeTarget = null
        dragStartPosition = null
    }

    private fun computeOverlapRatio(a: RecyclerView.ViewHolder, b: RecyclerView.ViewHolder): Float {
        val aLeft = a.itemView.left + a.itemView.translationX
        val aTop = a.itemView.top + a.itemView.translationY
        val aRight = aLeft + a.itemView.width
        val aBottom = aTop + a.itemView.height

        val bLeft = b.itemView.left.toFloat()
        val bTop = b.itemView.top.toFloat()
        val bRight = bLeft + b.itemView.width
        val bBottom = bTop + b.itemView.height

        val overlapLeft = maxOf(aLeft, bLeft)
        val overlapTop = maxOf(aTop, bTop)
        val overlapRight = minOf(aRight, bRight)
        val overlapBottom = minOf(aBottom, bBottom)

        if (overlapRight <= overlapLeft || overlapBottom <= overlapTop) return 0f

        val overlapArea = (overlapRight - overlapLeft) * (overlapBottom - overlapTop)
        val bArea = b.itemView.width * b.itemView.height
        return if (bArea == 0) 0f else overlapArea / bArea
    }
}
