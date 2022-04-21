package com.yaroslavm87.meaningfulmotion.fragment.touchSlop

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.ContextCompat
import com.yaroslavm87.meaningfulmotion.R
import com.yaroslavm87.meaningfulmotion.processing.TouchState
import com.yaroslavm87.meaningfulmotion.interfaces.MotionEventListener
import com.yaroslavm87.meaningfulmotion.interfaces.MotionEventStream
import com.yaroslavm87.meaningfulmotion.processing.OnTouchElevator
import kotlin.math.abs
import kotlin.math.sqrt

class SlopTouchSourceView : View, MotionEventStream {

    private val logTag: String = "MeaningfulMotion"
    private val className: String = this.javaClass.simpleName
    private val loggingEnabled: Boolean = true

    private lateinit var motionEventListener : MotionEventListener
    private val onTouchElevator = OnTouchElevator()
    private val touchState = TouchState()
    private val paint = createPaint()
    private var modifiedTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var lastDownX = touchState.none
    private var lastDownY = touchState.none

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) :
            super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    override fun setMotionEventListener(listener: MotionEventListener) {
        motionEventListener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        onTouchElevator.doOnTouchEvent(this, event)
        when (event.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchState.reset()
                motionEventListener.onMotionEvent(event)
            }
            MotionEvent.ACTION_DOWN -> {
                touchState.xDown = event.rawX
                touchState.yDown = event.rawY
                lastDownX = event.x
                lastDownY = event.y
                motionEventListener.onMotionEvent(event)
            }
            MotionEvent.ACTION_MOVE -> {
                val distance: Float = distance(
                    touchState.xDown,
                    touchState.yDown,
                    event.rawX,
                    event.rawY
                )
                if (distance > modifiedTouchSlop) {
                    motionEventListener.onMotionEvent(event)
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (lastDownX != touchState.none && lastDownY != touchState.none) {
            canvas.drawCircle(
                lastDownX,
                lastDownY,
                (modifiedTouchSlop).toFloat(),
                paint
            )
        }
    }

    private val multiplier = 3
    internal fun changeSizeOfTouchSlop(newSize: Int) {
        modifiedTouchSlop = when (newSize) {
            in Integer.MIN_VALUE until 0 ->
                ViewConfiguration.get(context).scaledTouchSlop
            in 100..Integer.MAX_VALUE ->
                100 * multiplier
            else ->
                newSize * multiplier
        }
        invalidate()
    }

    private fun createPaint(): Paint {
        val p = Paint()
        p.style = Paint.Style.STROKE
        p.color = ContextCompat.getColor(context, R.color.colorPrimary)
        p.strokeWidth = 3f
        return p
    }

    private fun distance(xDown: Float, yDown: Float, xCurrent: Float, yCurrent: Float): Float {
        val xAbs = abs(xDown - xCurrent)
        val yAbs = abs(yDown - yCurrent)
        return sqrt((yAbs * yAbs + xAbs * xAbs).toDouble()).toFloat()
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(logTag, "$className.$message")
    }
}