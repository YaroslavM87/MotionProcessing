package com.yaroslavm87.meaningfulmotion.processing

import android.view.MotionEvent
import android.view.View
import com.yaroslavm87.meaningfulmotion.interfaces.TouchProcessor

class OnTouchElevator : TouchProcessor {

    private val durationInMills: Long = 300
    private val zMin = 3f
    private val zMax = 20f

    override fun doOnTouchEvent(view: View, event: MotionEvent) {

        when (event.action) {

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                view.animate().translationZ(zMin)
                    .setDuration(durationInMills)
                    .start()
                return
            }

            MotionEvent.ACTION_DOWN -> {
                view.animate().translationZ(zMax)
                    .setDuration(durationInMills)
                    .start()
                return
            }

            else -> return
        }
    }
}