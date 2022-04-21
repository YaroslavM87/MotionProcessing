package com.yaroslavm87.meaningfulmotion.interfaces

import android.view.MotionEvent
import android.view.View

interface TouchProcessor {

    fun doOnTouchEvent(view: View, event: MotionEvent)
}