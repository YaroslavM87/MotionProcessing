package com.yaroslavm87.meaningfulmotion.fragment.tension

import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.yaroslavm87.meaningfulmotion.Const
import com.yaroslavm87.meaningfulmotion.processing.TouchState
import com.yaroslavm87.meaningfulmotion.fragment.bezier.TouchStateTracker
import com.yaroslavm87.meaningfulmotion.interfaces.TouchProcessor
import com.yaroslavm87.meaningfulmotion.util.Geometry

class TensionProcessor(
    private val touchState: TouchState,
    private val touchStateTracker: TouchStateTracker
    ): TouchProcessor {

    private val className: String = this.javaClass.simpleName
    private val loggingEnabled: Boolean = true

    private val defaultTensionFactor: Float = 0.1F

    internal var tensionFactor = defaultTensionFactor
        set(value) {
            field = if (value in 0F..1F) value else defaultTensionFactor.also {
                log("tensionFactor = $it")
            }
        }

    override fun doOnTouchEvent(view: View, event: MotionEvent) {
        log("doOnTouchEvent()")
        touchStateTracker.doOnTouchEvent(view, event)

        if (event.action == MotionEvent.ACTION_MOVE) {
            with (touchState) {
                val deltaX = xCurrent - xDown
                val deltaY = yCurrent - yDown
                val tensionDeltaX = deltaX * (1 - tensionFactor)
                val tensionDeltaY = deltaY * (1 - tensionFactor)
                xCurrent = tensionDeltaX + xDown
                yCurrent = tensionDeltaY + yDown
                distance = Geometry.calcDistance(xDown, yDown, xCurrent, yCurrent)
            }
        }
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(Const.logTag, "$className.$message")
    }

}