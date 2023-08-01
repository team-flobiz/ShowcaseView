package com.valorem.flobooks.showcaselib

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.graphics.applyCanvas
import androidx.core.view.isVisible
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.valorem.flobooks.showcaselib.databinding.LayoutShowcaseBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class ShowcaseView(context: Context) : FrameLayout(context), View.OnClickListener {
    init {
        setWillNotDraw(false)
        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true
        setOnClickListener(this)
    }

    private val contentBinding by lazy {
        LayoutShowcaseBinding.inflate(LayoutInflater.from(context), this@ShowcaseView, true)
    }

    private val layoutBinder by lazy { ShowcaseLayoutBinder(contentBinding) }

    private val maskOverlayColor by lazy { Color.parseColor("#CC37474F") }
    private val targetPaint by lazy {
        Paint().apply {
            color = Color.WHITE
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            flags = Paint.ANTI_ALIAS_FLAG
        }
    }

    private var bitmap: Bitmap? = null
        set(value) {
            field = value
            invalidate()
        }

    /* flows */
    private val targetState = MutableStateFlow<TargetInfo?>(null)
    private val rectState = targetState.flatMapLatest { target ->
        target?.anchor?.globalLayoutFlow
            ?.mapLatest {
                it.absPositionRect.apply {
                    /* negative inset as padding */
                    val inset = target.style.padding?.dp?.times(-1)?.toInt() ?: 0
                    inset(inset, inset)
                }
            }
            ?: flowOf(null)
    }
    private val bitmapState = targetState.combine(rectState) { config, rect ->
        rect?.let { _ ->
            context.screenRect
                .let { Bitmap.createBitmap(it.width(), it.height(), Bitmap.Config.ARGB_8888) }
                .applyCanvas {
                    /* clear screen */
                    drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                    /* mask color */
                    drawColor(maskOverlayColor)
                    /* draw target bounds rect */
                    config?.style?.shape?.draw(rect, this, targetPaint)
                }
        }
    }

    /* internal dismiss callback to control blocking flow */
    private var onDismissCallback: (Boolean) -> Unit = {}

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
            launch {
                targetState.filterNotNull()
                    .collectLatest { layoutBinder.invoke(it) }
            }
            launch {
                bitmapState.collectLatest {
                    bitmap = it
                    isVisible = it != null
                    it?.also { requestFocus() }
                }
            }
        }
        contentBinding.apply {
            btnDismiss.setOnClickListener(this@ShowcaseView)
            imgClose.setOnClickListener(this@ShowcaseView)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK)
            dismiss()
        return true
    }

    override fun onClick(v: View?) {
        when (v) {
            contentBinding.btnDismiss -> dismiss(fromAction = true)
            contentBinding.imgClose -> dismiss()
            else -> if (targetState.value?.style?.dismissAction == null) dismiss()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        bitmap?.also { canvas?.drawBitmap(it, 0f, 0f, null) }
        super.onDraw(canvas)
    }

    /* showcase for [target] */
    fun show(target: TargetInfo) {
        targetState.tryEmit(target)
    }

    /* showcase for [target] as suspend */
    suspend fun showBlocking(target: TargetInfo): Boolean = suspendCoroutine { cont ->
        show(target)
        onDismissCallback = { cont.resume(it) }
    }

    /* dismiss showcase */
    fun dismiss(fromAction: Boolean = false) {
        targetState.tryEmit(null)
        onDismissCallback.invoke(fromAction)
    }

    internal data class TargetInfo(
        val anchor: View,
        val style: ShowcaseTarget.Style
    ) {
        companion object {
            fun fromTarget(target: ShowcaseTarget): TargetInfo? =
                target.findTarget()?.let { TargetInfo(anchor = it, style = target.style) }
        }
    }
}