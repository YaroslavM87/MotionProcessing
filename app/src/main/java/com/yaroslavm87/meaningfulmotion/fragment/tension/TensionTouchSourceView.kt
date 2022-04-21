package com.yaroslavm87.meaningfulmotion.fragment.tension

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.yaroslavm87.meaningfulmotion.Const
import com.yaroslavm87.meaningfulmotion.fragment.animatedBezier.AnimatedBezierTouchSourceView
import com.yaroslavm87.meaningfulmotion.fragment.bezier.TouchStateTracker
import com.yaroslavm87.meaningfulmotion.interfaces.TouchProcessor

class TensionTouchSourceView : AnimatedBezierTouchSourceView {

    private val className: String = this.javaClass.simpleName
    private val loggingEnabled: Boolean = true

    private var tensionProcessor = TensionProcessor(touchState, TouchStateTracker(touchState))
    init {
        setTouchProcessor(tensionProcessor)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) :
            super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    fun setTension(tension: Float) {
        tensionProcessor.tensionFactor = tension
        invalidate()
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(Const.logTag, "$className.$message")
    }

}