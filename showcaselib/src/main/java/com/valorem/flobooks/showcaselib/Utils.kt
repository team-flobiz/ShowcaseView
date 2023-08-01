package com.valorem.flobooks.showcaselib

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.annotation.Nullable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/* convert dp value to pixels */
val Int.dp: Float
    get() = this.times(Resources.getSystem().displayMetrics.density)

/* finds screen position of this [View] */
val View.absPositionRect: Rect
    get() = intArrayOf(0, 0)
        /* calculate target view left/top points */
        .also(::getLocationInWindow)
        /* create rect */
        .let { Rect(it[0], it[1], it[0] + measuredWidth, it[1] + measuredHeight) }

/* physical screen rect */
val Context.screenRect: Rect
    get() = (applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
        .run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) currentWindowMetrics.bounds
            else DisplayMetrics().also { defaultDisplay.getRealMetrics(it) }
                .run { Rect(0, 0, widthPixels, heightPixels) }
        }

/* global layout flow for this [View] */
val View.globalLayoutFlow: Flow<View>
    get() = callbackFlow{
        ViewTreeObserver.OnGlobalLayoutListener { trySend(this@globalLayoutFlow) }
            .also(viewTreeObserver::addOnGlobalLayoutListener)
            .also { awaitClose { viewTreeObserver.removeOnGlobalLayoutListener(it) } }
    }

/**
 * find first visible(top/bottom) view holder from recycler view based on check
 *
 * @param predicate check for required view holder
 * @return nullable [VH] view holder
 */
@Nullable
inline fun <reified VH : RecyclerView.ViewHolder> RecyclerView.findFirstVisibleViewHolder(predicate: (viewHolder: VH?) -> Boolean): VH? {
    /* find first and last visible layout positions */
    val visibleItemRange = when (val lm = layoutManager) {
        is LinearLayoutManager -> Pair(
            lm.findFirstVisibleItemPosition(),
            lm.findLastVisibleItemPosition()
        )
        else -> throw Exception("find first visible view holder from layoutManager:$layoutManager not implemented")
    }

    /* safe range check */
    if (visibleItemRange.first >= 0 && visibleItemRange.second < (layoutManager?.itemCount ?: 0))
        for (index in visibleItemRange.first..visibleItemRange.second) {
            val viewHolder = findViewHolderForLayoutPosition(index) as? VH
            /* check if required view holder matches predicate */
            if (predicate(viewHolder))
                return viewHolder
        }
    return null
}
