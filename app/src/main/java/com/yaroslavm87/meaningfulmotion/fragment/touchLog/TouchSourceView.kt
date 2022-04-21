package com.yaroslavm87.meaningfulmotion.fragment.touchLog

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.yaroslavm87.meaningfulmotion.interfaces.MotionEventListener
import com.yaroslavm87.meaningfulmotion.interfaces.MotionEventStream
import com.yaroslavm87.meaningfulmotion.processing.OnTouchElevator

class TouchSourceView : View, MotionEventStream {

    private lateinit var motionEventListener : MotionEventListener
    private val onTouchElevator = OnTouchElevator()

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

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            if (this::motionEventListener.isInitialized) {
                motionEventListener.onMotionEvent(event)
            }
            onTouchElevator.doOnTouchEvent(this, event)
        }
        return super.onTouchEvent(event)
    }

}