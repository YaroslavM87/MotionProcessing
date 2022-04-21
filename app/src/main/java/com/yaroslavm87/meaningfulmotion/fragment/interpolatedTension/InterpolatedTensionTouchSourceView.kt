package com.yaroslavm87.meaningfulmotion.fragment.interpolatedTension

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.yaroslavm87.meaningfulmotion.R
import com.yaroslavm87.meaningfulmotion.processing.TouchState
import com.yaroslavm87.meaningfulmotion.fragment.animatedBezier.AnimatedBezierTouchSourceView
import com.yaroslavm87.meaningfulmotion.fragment.bezier.TouchStateTracker

class InterpolatedTensionTouchSourceView : AnimatedBezierTouchSourceView {

    private val logTag: String = "MeaningfulMotion"
    private val className: String = this.javaClass.simpleName
    private val loggingEnabled: Boolean = true

    internal var tensionProcessor = InterpolatedTensionProcessor(touchState, TouchStateTracker(touchState))
    private var radiusMin = tensionProcessor.radiusInner
    private var radiusMax = tensionProcessor.radiusOuter

    private var argbEvaluator = ArgbEvaluator()
    private var colorNoTension = ContextCompat.getColor(context, R.color.tensionNone)
    private var colorStartTension = ContextCompat.getColor(context, R.color.tensionStart)
    private var colorEndTension = ContextCompat.getColor(context, R.color.tensionEnd)

    private var paintRadiusMin = createRadiusPaint(colorStartTension)
    private var paintRadiusMax = createRadiusPaint(colorEndTension)
//
//    protected var lastDownX = touchState.none
//    private var lastDownY = touchState.none

    init {
        setTouchProcessor(tensionProcessor)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) :
            super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {

            if (event.action == MotionEvent.ACTION_DOWN) {
                lastDownX = event.x
                lastDownY = event.y
            }
            else if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                lastDownX = touchState.none
                lastDownY = touchState.none
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        setColor(getInterpolatedColor(touchState))
        super.onDraw(canvas)
        if (lastDownX != touchState.none && lastDownY != touchState.none) {
            //log("onDraw()")
            canvas.drawCircle(lastDownX, lastDownY, radiusMin, paintRadiusMin)
            canvas.drawCircle(lastDownX, lastDownY, radiusMax, paintRadiusMax)
        }
    }

    fun setRadiusInner(radius: Float) {
        radiusMin = radius
        tensionProcessor.radiusInner = radius
        invalidate()
    }

    fun setRadiusOuter(radius: Float) {
        radiusMax = radius
        tensionProcessor.radiusOuter = radius
        invalidate()
    }

    fun setTension(tension: Float) {
        tensionProcessor.tensionFactor = tension
        invalidate()
    }

    private fun createRadiusPaint(@ColorInt color: Int): Paint {
        return Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 2F
            isAntiAlias = true
            this.color = color
        }
    }

    private fun getInterpolatedColor(touchState: TouchState): Int {
        if (touchState.distance == touchState.none) return colorNoTension

        if (radiusMin == radiusMax) return argbEvaluator.evaluate(
            touchState.distance / radiusMax,
            colorNoTension,
            colorEndTension
        ) as Int

        if (touchState.distance <= radiusMin) return argbEvaluator.evaluate(
            touchState.distance / radiusMin,
            colorNoTension,
            colorStartTension
        ) as Int

        val fractionalDistance: Float = (touchState.distance - radiusMin) / (radiusMax - radiusMin)

        return argbEvaluator.evaluate(fractionalDistance, colorStartTension, colorEndTension) as Int
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(logTag, "$className.$message")
    }

}