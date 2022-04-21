package com.yaroslavm87.meaningfulmotion.fragment.animatedBezier

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.animation.AccelerateInterpolator
import android.view.animation.Interpolator
import com.yaroslavm87.meaningfulmotion.processing.TouchState
import com.yaroslavm87.meaningfulmotion.fragment.bezier.BezierTouchSourceView
import com.yaroslavm87.meaningfulmotion.processing.SettleAnimator
import com.yaroslavm87.meaningfulmotion.processing.TouchStateView

open class AnimatedBezierTouchSourceView : BezierTouchSourceView, TouchStateView {

    private val logTag: String = "MeaningfulMotion"
    private val className: String = this.javaClass.simpleName
    private val loggingEnabled: Boolean = true

    private var defaultAnimationDuration = 150L
    private val defaultAnimationInterpolator = AccelerateInterpolator(.5f)
    private val settleAnimator = SettleAnimator().apply {
        setDuration(defaultAnimationDuration)
        setInterpolator(defaultAnimationInterpolator)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) :
            super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    fun setAnimationDuration(durationMs: Long) {
        settleAnimator.setDuration(durationMs)
    }

    fun setInterpolator(interpolator: Interpolator) {
        settleAnimator.setInterpolator(interpolator)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {

            when (event.action) {
                MotionEvent.ACTION_UP ->
                    settleAnimator.start(touchState, this)

                else ->
                    settleAnimator.cancel()
            }
        }
        return super.onTouchEvent(event)
    }

    override fun drawTouchState(touchState: TouchState) {
        calculatePath(touchState)
        invalidate()
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(logTag, "$className.$message")
    }

}