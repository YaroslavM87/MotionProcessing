package com.yaroslavm87.meaningfulmotion.fragment.interpolatedTension

import android.graphics.Path
import android.graphics.PathMeasure
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.yaroslavm87.meaningfulmotion.Const
import com.yaroslavm87.meaningfulmotion.processing.TouchState
import com.yaroslavm87.meaningfulmotion.fragment.bezier.TouchStateTracker
import com.yaroslavm87.meaningfulmotion.interfaces.TouchProcessor
import com.yaroslavm87.meaningfulmotion.util.Geometry

class InterpolatedTensionProcessor(
    private val touchState: TouchState,
    private val touchStateTracker: TouchStateTracker
) : TouchProcessor {

    private val className: String = this.javaClass.simpleName
    private val loggingEnabled: Boolean = true

    internal val radiiMinDelta = 50F
    internal var radiusMin = 100F
    internal var radiusMax = 900F

    var radiusInner = radiusMax * 0.3F
        set(value) {
            field = when (value) {
                in Float.MIN_VALUE..radiusMin -> radiusMin
                in (radiusOuter - radiiMinDelta)..Float.MAX_VALUE -> radiusOuter - radiiMinDelta
                else -> value
            }
        }

    var radiusOuter = radiusMax * 0.7F
        set(value) {
            field = when (value) {
                in Float.MIN_VALUE..(radiusInner + radiiMinDelta) -> (radiusInner + radiiMinDelta)
                in radiusMax..Float.MAX_VALUE -> radiusMax
                else -> value
            }
        }

    var tensionFactor = 0.5F
        set (value) {
            field = when (value) {
                in Float.MIN_VALUE..0F -> 0F
                else -> value
            }
        }

    private val interpolator = DecelerateInterpolator(0.5F)

    override fun doOnTouchEvent(view: View, event: MotionEvent) {
        touchStateTracker.doOnTouchEvent(view, event)

        if (event.action == MotionEvent.ACTION_MOVE) {

            val coords = findCoordsByDistance(touchState, interpolateDistance())

            with(touchState) {
                xCurrent = coords[0]
                yCurrent = coords[1]
                distance = Geometry.calcDistance(xDown, yDown, xCurrent, yCurrent)
            }
        }
    }

    private fun interpolateDistance(): Float {

        val tensionZone = radiusOuter - radiusInner
        val distanceRequiredToPull = tensionZone * (tensionFactor + 1)

        return when {
            touchState.distance <= radiusInner -> touchState.distance

            touchState.distance >= (distanceRequiredToPull + radiusInner) -> radiusOuter

            else -> {
                val progress = (touchState.distance - radiusInner) / distanceRequiredToPull
                tensionZone * interpolator.getInterpolation(progress) + radiusInner
            }
        }
    }

    private var path = Path()
    private var pathMeasure = PathMeasure()

    private fun findCoordsByDistance(state: TouchState, distance: Float): FloatArray {
        path.apply {
            reset()
            moveTo(state.xDown, state.yDown)
            lineTo(state.xCurrent, state.yCurrent)
        }

        val coords = floatArrayOf(0F, 0F)

        pathMeasure.apply {
            setPath(path, false)
            getPosTan(distance, coords, null)
        }

        return coords
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(Const.logTag, "$className.$message")
    }
}