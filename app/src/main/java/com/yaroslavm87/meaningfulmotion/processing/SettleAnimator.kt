package com.yaroslavm87.meaningfulmotion.processing

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Path
import android.graphics.PathMeasure
import android.util.Log
import android.view.animation.AccelerateInterpolator
import android.view.animation.Interpolator
import com.yaroslavm87.meaningfulmotion.Const
import com.yaroslavm87.meaningfulmotion.util.Geometry

class SettleAnimator : TouchStateAnimator {

    private val className: String = this.javaClass.simpleName
    private val loggingEnabled: Boolean = true

    private var animationInterpolator: Interpolator = AccelerateInterpolator()
    private var animationDuration = 300L
    private var animator: ValueAnimator? = null
    private val path = Path()
    private val pathMeasure = PathMeasure()

    override fun start(touchState: TouchState, touchStateView: TouchStateView) {

        path.apply {
            reset()
            moveTo(touchState.xCurrent, touchState.yCurrent)
            lineTo(touchState.xDown, touchState.yDown)
        }

        val fromDistance = touchState.distance

        /*
        * The need for these values saving touch X and Y is dictated by call TouchState.reset()
        * performed in call BezierTouchSourceView.onTouchEvent() following call
        * AnimatedBezierTouchSourceView.onTouchEvent() in which SettleAnimator.start() is performed
        *
        * This chain of calls leads to resetting TouchState right after SettleAnimator.start()
        * call was performed
        * */
        val xTo = touchState.xDown
        val yTo = touchState.yDown

        animator = ValueAnimator.ofFloat(1F, 0F).apply {
            interpolator = animationInterpolator
            duration = animationDuration.also {
                log("start(): duration = $it")
            }

            addUpdateListener {
                with(touchState) {
                    xDown = xTo
                    yDown = yTo
                    val fraction = it.animatedFraction
                    distance = (1 - fraction) * fromDistance
                    val points = Geometry.findPointsFromPercent(path, fromDistance, fraction)
                    xCurrent = points.first
                    yCurrent = points.second
                    touchStateView.drawTouchState(this)
                }
            }

            addListener(
                object : AnimatorListenerAdapter() {

                    override fun onAnimationCancel(animation: Animator?) {
                        touchState.reset()
                        touchStateView.drawTouchState(touchState)
                    }

                    override fun onAnimationPause(animation: Animator?) {
                        touchState.reset()
                        touchStateView.drawTouchState(touchState)
                    }
                }
            )

            start()
        }
    }

    override fun cancel() {
        if (animator != null && animator!!.isRunning) {
            animator!!.cancel()
            animator = null
        }
    }

    override fun setDuration(durationMs: Long) {
        animationDuration = if (durationMs > 0) durationMs else animationDuration
    }

    override fun setInterpolator(interpolator: Interpolator) {
        animationInterpolator = interpolator
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(Const.logTag, "$className.$message")
    }

}