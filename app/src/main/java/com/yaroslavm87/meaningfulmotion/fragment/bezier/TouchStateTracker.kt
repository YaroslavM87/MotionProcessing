package com.yaroslavm87.meaningfulmotion.fragment.bezier

import android.view.MotionEvent
import android.view.View
import com.yaroslavm87.meaningfulmotion.processing.TouchState
import com.yaroslavm87.meaningfulmotion.interfaces.TouchProcessor
import com.yaroslavm87.meaningfulmotion.util.Geometry

class TouchStateTracker(private val touchState: TouchState) : TouchProcessor {

    override fun doOnTouchEvent(view: View, event: MotionEvent) {

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                with(touchState) {
                    xDown = event.x
                    yDown = event.y
                    xDownRaw = event.rawX
                    yDownRaw = event.rawY
                }
            }

            MotionEvent.ACTION_MOVE -> {
                with(touchState) {
                    xCurrent = event.x
                    yCurrent = event.y
                    xCurrentRaw = event.rawX
                    yCurrentRaw = event.rawY
                    distance = Geometry.calcDistance(xDown, yDown, xCurrent, yCurrent)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                touchState.reset()
        }
    }

}