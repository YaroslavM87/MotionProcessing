package com.yaroslavm87.meaningfulmotion.fragment.bezier

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.yaroslavm87.meaningfulmotion.Const
import com.yaroslavm87.meaningfulmotion.R
import com.yaroslavm87.meaningfulmotion.processing.TouchState
import com.yaroslavm87.meaningfulmotion.interfaces.MotionEventListener
import com.yaroslavm87.meaningfulmotion.interfaces.MotionEventStream
import com.yaroslavm87.meaningfulmotion.interfaces.TouchProcessor
import com.yaroslavm87.meaningfulmotion.processing.OnTouchElevator
import com.yaroslavm87.meaningfulmotion.util.Geometry
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.sqrt

open class BezierTouchSourceView : View, MotionEventStream {

    private val className: String = this.javaClass.simpleName
    private val loggingEnabled: Boolean = true

    private lateinit var motionEventListener : MotionEventListener
    private val onTouchElevator = OnTouchElevator()
    private val paint = createPaint()
    private val path = Path()
    private val touchSlopIncrement = 10
    private val scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop + touchSlopIncrement
    protected val touchState = TouchState()
    private var touchStateTracker: TouchProcessor = TouchStateTracker(touchState)
    protected var lastDownX = touchState.none
    protected var lastDownY = touchState.none

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) :
            super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    private fun createPaint(): Paint {
        return Paint().apply {
            style = Paint.Style.STROKE
            color = ContextCompat.getColor(context, R.color.colorAccent)
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
            strokeWidth = 4f
        }
    }

    override fun setMotionEventListener(listener: MotionEventListener) {
        motionEventListener = listener
    }

    /**
     * Override the default touch processor.
     */
    fun setTouchProcessor(t: TouchProcessor) {
        touchStateTracker = t
    }

    fun setColor(@ColorInt color: Int) {
        paint.color = color
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {

            fun passEventToListener(e: MotionEvent) {
                if (this::motionEventListener.isInitialized) {
                    motionEventListener.onMotionEvent(e)
                }
            }

            onTouchElevator.doOnTouchEvent(this, event)
            touchStateTracker.doOnTouchEvent(this, event)

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    lastDownX = event.x
                    lastDownY = event.y
                    passEventToListener(event)
                }

                MotionEvent.ACTION_MOVE ->
                    if (touchState.distance > scaledTouchSlop) {
                        passEventToListener(event)
                    }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    passEventToListener(event)

            }
            calculatePath(touchState)
            invalidate()
        }

        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //log("onDraw()")
        // Draw a circle around the last known ACTION_DOWN event, if there was one.
        if (
            (touchState.xDown == touchState.none && lastDownX != touchState.none)
            || (touchState.distance != touchState.none && touchState.distance < scaledTouchSlop)
        ) {
            canvas.drawCircle(lastDownX, lastDownY, scaledTouchSlop.toFloat(),paint)
        }
        canvas.save()
        canvas.rotate(angle(touchState), touchState.xCurrent, touchState.yCurrent)
        canvas.drawPath(path, paint)
        canvas.restore()
    }

    protected fun calculatePath(touchState: TouchState) {
        path.reset()
        if (
            touchState.xCurrent == this.touchState.none
            || touchState.yCurrent == this.touchState.none
            || touchState.distance == this.touchState.none
        ) return

        /* center around down point */
        val xMod: Float = x(touchState)
        val yMod: Float = y(touchState)

        path.moveTo(touchState.xCurrent, touchState.yCurrent)
        val controlPointX = touchState.xCurrent + touchState.distance * .66f
        val controlPointY = touchState.yCurrent + yMod / 3
        path.quadTo(controlPointX, controlPointY, touchState.xCurrent + xMod, touchState.yCurrent + yMod)
        val sweep: Float = sweep(
            touchState.xCurrent + xMod, touchState.yCurrent + yMod,
            touchState.xCurrent + xMod, touchState.yCurrent - yMod
        )
        path.arcTo(
            touchState.xCurrent + touchState.distance - scaledTouchSlop,
            touchState.yCurrent - scaledTouchSlop,
            touchState.xCurrent + touchState.distance + scaledTouchSlop,
            touchState.yCurrent + scaledTouchSlop,
            sweep / 2,
            -sweep,
            false
        )
        val controlPointXMirror = touchState.xCurrent + touchState.distance * .66f
        val controlPointYMirror = touchState.yCurrent - yMod / 3
        path.moveTo(touchState.xCurrent, touchState.yCurrent)
        path.quadTo(controlPointXMirror, controlPointYMirror, touchState.xCurrent + xMod, touchState.yCurrent - yMod)
    }

    /**
     * The change in the x value that is required to move from the current touch point to
     * the tangent.
     */
    private fun x(s: TouchState): Float {
        val currToTan = sqrt(s.distance * s.distance - scaledTouchSlop * scaledTouchSlop)
        return currToTan * (currToTan / s.distance)
    }

    /**
     * The change in the y value that is required to move from the current touch point to
     * the tangent.
     */
    private fun y(s: TouchState): Float {
        val currToTan = sqrt(s.distance * s.distance - scaledTouchSlop * scaledTouchSlop)
        return currToTan * (scaledTouchSlop / s.distance)
    }

    /**
     * Angle between the current touch coordinates and the down coordinates
     */
    private fun angle(s: TouchState): Float {
        return Math.toDegrees(
            atan2(
                (s.yDown - s.yCurrent).toDouble(),
                (s.xDown - s.xCurrent).toDouble()
            )
        ).toFloat()
    }

    /**
     * Find the angle in degrees between two tangent points
     *
     * @param tan1X the x coordinate of the first tangent point
     * @param tan1Y the y coordinate of the first tangent point
     * @param tan2X the x coordinate of the second tangent point
     * @param tan2Y the y coordinate of the second tangent point
     * @return the major sweep angle between the two tangent point, in degrees.
     */
    private fun sweep(tan1X: Float, tan1Y: Float, tan2X: Float, tan2Y: Float): Float {
        val minorSweep = Math.toDegrees(
            2 * asin(
                .5 * Geometry.calcDistance(
                    tan1X, tan1Y,
                    tan2X, tan2Y
                ) / scaledTouchSlop
            )
        ).toFloat()
        return 360 - minorSweep
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(Const.logTag, "$className.$message")
    }

}