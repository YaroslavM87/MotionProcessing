package com.yaroslavm87.meaningfulmotion.processing

import android.view.animation.Interpolator

interface TouchStateAnimator {

    fun cancel()

    fun setDuration(durationMs: Long)

    fun setInterpolator(interpolator: Interpolator)

    fun start(touchState: TouchState, touchStateView: TouchStateView)

}