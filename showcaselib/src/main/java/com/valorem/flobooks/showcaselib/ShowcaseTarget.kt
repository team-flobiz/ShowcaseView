package com.valorem.flobooks.showcaselib

import android.view.View
import androidx.recyclerview.widget.RecyclerView

sealed class ShowcaseTarget {
    abstract val id: String
    abstract val style: Style
    abstract val onDismiss: (fromAction: Boolean) -> Unit
    internal abstract fun findTarget(): View?

    data class Sync(
        override val id: String,
        override val style: Style = Style(),
        private val target: View,
        override val onDismiss: (fromAction: Boolean) -> Unit = {},
    ): ShowcaseTarget() {
        override fun findTarget(): View = target
    }

    data class Async(
        override val id: String,
        override val style: Style = Style(),
        private val targetFinder: () -> View?,
        override val onDismiss: (fromAction: Boolean) -> Unit = {},
    ): ShowcaseTarget() {
        override fun findTarget(): View? = targetFinder.invoke()
    }

    data class ViewHolderItem(
        override val id: String,
        override val style: Style = Style(),
        override val onDismiss: (fromAction: Boolean) -> Unit = {},
        val parent: RecyclerView,
        val predicate: (RecyclerView.ViewHolder) -> Boolean,
        val targetFinder: (RecyclerView.ViewHolder) -> View
    ): ShowcaseTarget() {
        override fun findTarget(): View? =
            parent.findFirstVisibleViewHolder<RecyclerView.ViewHolder>(predicate = { it?.run(predicate) ?: false })
                ?.run(targetFinder)
    }

    data class Style(
        val hint: String? = null,
        /* if null -> overlay become clickable to dismiss */
        val dismissAction: String? = null,
        val shape: ShowcaseShape = ShowcaseShape.Rectangle,
        val gravity: ShowcaseGravity = ShowcaseGravity.Bottom,
        val hintMargin: Int? = null,
        val actionMargin: Int? = null,
        /* provide negative value for inset padding */
        val padding: Int? = 8,
        val hasCloseButton: Boolean = false
    )
}