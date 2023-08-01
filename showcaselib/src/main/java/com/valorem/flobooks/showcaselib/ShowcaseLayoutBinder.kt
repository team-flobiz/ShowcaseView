package com.valorem.flobooks.showcaselib

import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.valorem.flobooks.showcaselib.databinding.LayoutShowcaseBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class ShowcaseLayoutBinder(private val binding: LayoutShowcaseBinding) {

    suspend fun invoke(target: ShowcaseView.TargetInfo) = withContext(Dispatchers.Main) {
        /* layout */
        target.anchor.absPositionRect
            .run {
                when (target.style.gravity) {
                    ShowcaseGravity.Top -> Triple(ConstraintSet.BOTTOM, ConstraintSet.TOP, top)
                    ShowcaseGravity.Bottom -> Triple(
                        ConstraintSet.TOP,
                        ConstraintSet.BOTTOM,
                        bottom
                    )
                }
            }
            .let { (startSide, endSide, guidelineBeginMargin) ->
                ConstraintSet()
                    .apply {
                        clone(binding.content)
                        setGuidelineBegin(R.id.guide, guidelineBeginMargin)
                        clear(R.id.txt_tooltip, endSide)
                        clear(R.id.container_info, endSide)
                        clear(R.id.btn_dismiss, endSide)
                        (target.style.hintMargin?.dp?.toInt() ?: 0)
                            .also { margin ->
                                connect(R.id.txt_tooltip, startSide, R.id.guide, endSide, margin)
                                connect(R.id.container_info, startSide, R.id.txt_tooltip, endSide, margin)
                            }
                        (target.style.actionMargin?.dp?.toInt() ?: 0)
                            .also { margin -> connect(R.id.btn_dismiss, startSide, R.id.container_info, endSide, margin) }
                    }
                    .applyTo(binding.content)
            }

        /* bind */
        binding.apply {
            txtTooltip.apply {
                isVisible = target.style.hint != null
                text = target.style.hint
            }
            btnDismiss.apply {
                isVisible = target.style.dismissAction != null
                text = target.style.dismissAction
            }
            imgClose.apply {
                isVisible = target.style.hasCloseButton
            }
        }
    }
}