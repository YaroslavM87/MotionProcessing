package com.yaroslavm87.meaningfulmotion.fragment.reorder

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Paint
import android.graphics.Point
import android.util.AttributeSet
import android.util.Log
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import com.yaroslavm87.meaningfulmotion.Const
import com.yaroslavm87.meaningfulmotion.R
import com.yaroslavm87.meaningfulmotion.fragment.bezier.BezierTouchSourceView
import com.yaroslavm87.meaningfulmotion.processing.TouchState
import com.yaroslavm87.meaningfulmotion.processing.TouchStateView

class ReorderBezierTouchSourceView : BezierTouchSourceView, TouchStateView {

    private val className: String = this.javaClass.simpleName
    private val loggingEnabled: Boolean = true

    private val anchorSizeMin = 5F
    private val anchorSizeMax = 70F
    private val anchorEventHorizon = 120F
    private val animationDuration = 2000L
    private var anchorRadius = anchorSizeMin

    private val dragProcessor = DragProcessor(touchState, this)
        .apply {
            eventHorizon = anchorEventHorizon }
        .also {
            setTouchProcessor(it)
        }

    private val argbEvaluator: ArgbEvaluator = ArgbEvaluator()
    private val valueAnimator = ValueAnimator.ofFloat(0F, 1F)
    private val decelerateInterpolator = DecelerateInterpolator()

    private var anchor1 = Point()
    private var anchor2 = Point()

    private var paint = createPaint()

    private val anchorColorStart = ContextCompat.getColor(context, R.color.anchor)
    private val anchorColorEnd = ContextCompat.getColor(context, R.color.transparent)


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) :
            super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    override fun drawTouchState(touchState: TouchState) {
        calculatePath(touchState)
        lastDownX = touchState.xDown
        lastDownY = touchState.yDown
        invalidate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            val height = bottom - top
            val width = right - left
            val landscape = resources.getBoolean(R.bool.is_landscape)
            if (landscape) {
                anchor1.x = width / 4
                anchor1.y = height / 2
                anchor2.x = width / 4 * 3
                anchor2.y = height / 2
            } else {
                anchor1.x = width / 2
                anchor1.y = height / 4
                anchor2.x = width / 2
                anchor2.y = height / 4 * 3
            }
            dragProcessor.anchor1 = anchor1
            dragProcessor.anchor2 = anchor2
            startAnchorAnimation()
        }
    }

    private fun createPaint(): Paint {
        return Paint().apply {
            style = Paint.Style.FILL_AND_STROKE
            color = ContextCompat.getColor(context, R.color.anchor)
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
            strokeWidth = 1f
        }
    }

    private fun startAnchorAnimation() {
        valueAnimator.apply {
            removeAllUpdateListeners()
            cancel()
            addUpdateListener(createAnimatorUpdateListener())
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = decelerateInterpolator
            duration = animationDuration
            start()
        }

    }

    private fun createAnimatorUpdateListener(): ValueAnimator.AnimatorUpdateListener {
        return ValueAnimator.AnimatorUpdateListener {
            val fraction = it.animatedFraction
            anchorRadius = (anchorSizeMax - anchorSizeMin) * fraction + anchorSizeMin
            paint.color = argbEvaluator.evaluate(fraction, anchorColorStart, anchorColorEnd) as Int
            invalidate()
        }
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(Const.logTag, "$className.$message")
    }
}