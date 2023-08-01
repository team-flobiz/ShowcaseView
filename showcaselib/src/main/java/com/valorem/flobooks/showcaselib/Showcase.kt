package com.valorem.flobooks.showcaselib

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Showcase(private val activity: Activity) {
    private val prefs: ShowcasePrefs = ShowcasePrefs(activity)

    /* current screen target sequence state */
    private val sequenceState = MutableStateFlow<List<ShowcaseTarget>>(listOf())

    /** adds target to for showcase in sequence */
    fun add(vararg config: ShowcaseTarget): Showcase {
        sequenceState.update { it + config }
        return this
    }

    /* register with owner */
    fun register(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch {
            sequenceState
                .combine(activity.window.decorView.globalLayoutFlow) { sequence, _ -> sequence }
                /* warmup delay for decor view to complete layout */
                .onEach { delay(250) }
                .flatMapLatest { it.asFlow() }
                .mapNotNull { target -> ShowcaseView.TargetInfo.fromTarget(target)?.let { it to target } }
                .filter { (targetInfo, target) -> target.isEligible() && targetInfo.isAvailable() }
                .cancellable()
                .collect { (targetInfo, target) ->
                    withContext(Dispatchers.Main) {
                        /* todo - hide keyboard before showcase */
                        /* create showcase view */
                        ShowcaseView(activity)
                            /* add showcase view to activity decor view */
                            .also { (activity.window.decorView as ViewGroup).addView(it) }
                            /* show showcase blocking */
                            .apply { showBlocking(targetInfo).also(target.onDismiss::invoke) }
                            /* set shown to prefs */
                            .also { prefs.setShown(target.id) }
                            /* remove showcase view from decor view */
                            .also { (activity.window.decorView as ViewGroup).removeView(it) }
                    }
                    /* delay between each showcase */
                    delay(500)
                }
        }
    }

    /* check if target is eligible to showcase */
    private fun ShowcaseTarget.isEligible(): Boolean = prefs.canShow(id)

    private fun ShowcaseView.TargetInfo.isAvailable(): Boolean =
        anchor.run { windowVisibility == View.VISIBLE && isVisible }

    /* reset all prefs */
    fun reset() {
        prefs.clearAll()
    }
}