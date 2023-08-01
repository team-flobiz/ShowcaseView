package com.valorem.flobooks.showcaselib

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.max

sealed interface ShowcaseShape {
    /* draw shape with paint over canvas with target rect bound */
    fun draw(targetRect: Rect, canvas: Canvas, paint: Paint)

    object Circle : ShowcaseShape {
        override fun draw(targetRect: Rect, canvas: Canvas, paint: Paint) {
            val radius = max(targetRect.width(), targetRect.height()).div(2f)
            canvas.drawCircle(targetRect.centerX().toFloat(), targetRect.centerY().toFloat(), radius, paint)
        }
    }

    object Rectangle : ShowcaseShape {
        override fun draw(targetRect: Rect, canvas: Canvas, paint: Paint) {
            canvas.drawRect(targetRect, paint)
        }
    }

    data class RoundedRectangle(private val cornerRadius: Float) : ShowcaseShape {
        override fun draw(targetRect: Rect, canvas: Canvas, paint: Paint) {
            canvas.drawRoundRect(RectF(targetRect), cornerRadius, cornerRadius, paint)
        }
    }
}