package com.yaroslavm87.meaningfulmotion.fragment.reorder

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Path
import android.graphics.Point
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.yaroslavm87.meaningfulmotion.Const
import com.yaroslavm87.meaningfulmotion.interfaces.TouchProcessor
import com.yaroslavm87.meaningfulmotion.processing.TouchState
import com.yaroslavm87.meaningfulmotion.processing.TouchStateView
import com.yaroslavm87.meaningfulmotion.util.Geometry
import kotlin.properties.Delegates

class DragProcessor(
    private val touchState: TouchState,
    private val touchStateView: TouchStateView
    ) : TouchProcessor {

    private val className: String = this.javaClass.simpleName
    private val loggingEnabled: Boolean = true

    private val pathToAnchor = Path()

    private val animator = ValueAnimator.ofFloat(0F, 1F).apply {
        interpolator = AccelerateDecelerateInterpolator()
        duration = 300L
    }

    internal lateinit var anchor1: Point
    internal lateinit var anchor2: Point
    private lateinit var closestAnchor: Point
    private lateinit var animationTarget: Point
    private var animationDistance by Delegates.notNull<Float>()
    internal var eventHorizon = 10F
    private var isAnimating = false
    private var isPointerDown = false

    override fun doOnTouchEvent(view: View, event: MotionEvent) {
        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                isPointerDown = true
                if (isAnimating) animator.cancel()

                with(touchState) {
                    xDown = event.x
                    yDown = event.y
                    xDownRaw = event.rawX
                    yDownRaw = event.rawY

                    closestAnchor = findClosestAnchor(xDown, yDown)
                    if (shouldAnimateTo(closestAnchor, this))
                        animateTo(closestAnchor)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                with(touchState) {
                    xCurrent = event.x
                    yCurrent = event.y
                    xCurrentRaw = event.rawX
                    yCurrentRaw = event.rawY

                    if (!isAnimating)
                        distance = Geometry.calcDistance(xCurrent, yCurrent, xDown, yDown)

                    closestAnchor = findClosestAnchor(xCurrent, yCurrent)
                    if (shouldAnimateTo(closestAnchor, this))
                        animateTo(closestAnchor)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isPointerDown = false
                if (!isAnimating) touchState.reset()
            }
        }
    }


    private fun findClosestAnchor(x: Float, y: Float): Point {
        val distance1 = Geometry.calcDistance(x, y, anchor1.x.toFloat(), anchor1.y.toFloat())
        val distance2 = Geometry.calcDistance(x, y, anchor2.x.toFloat(), anchor2.y.toFloat())

        return when {
            Geometry.areEqualWithinGivenApproximation(
                distance1,
                distance2,
                0.1F
            ) || distance1 < distance2 ->
                anchor1
            else ->
                anchor2
        }
    }

    private fun shouldAnimateTo(point: Point, state: TouchState): Boolean {
        if (isAnimating && point == animationTarget) {
            return false
        }
        val distance = Geometry.calcDistance(state.xCurrent, state.yCurrent, point.x.toFloat(), point.y.toFloat())
        return distance < eventHorizon
    }

    private fun animateTo(point: Point) {
        animationTarget = point
        isAnimating = true
        animationDistance = Geometry.calcDistance(
            point.x.toFloat(),
            point.y.toFloat(), touchState.xDown, touchState.yDown
        )
        pathToAnchor.apply {
            reset()
            moveTo(touchState.xDown, touchState.yDown)
            lineTo(point.x.toFloat(), point.y.toFloat())
        }
        animator.apply {
            removeAllUpdateListeners()
            removeAllListeners()
            cancel()
            addUpdateListener(createAnimatorUpdateListener())
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isAnimating = false
                }
            })
            start()
        }
    }

    private fun createAnimatorUpdateListener(): ValueAnimator.AnimatorUpdateListener {
        return ValueAnimator.AnimatorUpdateListener {
            val fraction = it.animatedFraction
            val points = Geometry.findPointsFromPercent(pathToAnchor, animationDistance, fraction)

            with(touchState) {
                xDown = points.first
                yDown = points.second

                if (isPointerDown) {
                    distance = Geometry.calcDistance(
                        xCurrent,
                        yCurrent,
                        points.first,
                        points.second
                    )
                } else {
                    xCurrent = xDown
                    yCurrent = yDown
                    distance = 0F
                }

                touchStateView.drawTouchState(this)
            }
        }
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(Const.logTag, "$className.$message")
    }

}