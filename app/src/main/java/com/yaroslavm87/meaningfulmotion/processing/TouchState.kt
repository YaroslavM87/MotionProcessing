package com.yaroslavm87.meaningfulmotion.processing

import android.util.Log
import com.yaroslavm87.meaningfulmotion.Const

/**
 * Container for holding relevant details about any in-progress motion events.
 */
class TouchState {

    private val className: String = this.javaClass.simpleName
    private val loggingEnabled: Boolean = true

    /**
     * No-Value for state.  Typically the [TouchState] fields will hold this value
     * if no touch event is in progress.
     */
    val none = -1f

    /**
     * The relative x coordinate where the motion event started.
     */
    var xDown = none
        set(value) {
            log("field = $value")
            field = value
        }
    var xDownRaw = none

    /**
     * The relative y coordinate where the motion event started.
     */
    var yDown = none
        set(value) {
            log("field = $value")
            field = value
        }
    var yDownRaw = none

    /**
     * The current x coordinate
     */
    var xCurrent = none
    var xCurrentRaw = none

    /**
     * The current y coordinate
     */
    var yCurrent = none
    var yCurrentRaw = none

    /**
     * The distance between the down and current coordinates.
     */
    var distance = none

    /**
     *
     */
    fun reset() {
        log("reset()")
        xDown = none
        yDown = none
        xCurrent = none
        yCurrent = none
        distance = none
    }

    override fun toString(): String {
        return "TouchState{" +
                "xDown=" + xDown +
                ", xDownRaw=" + xDownRaw +
                ", yDown=" + yDown +
                ", yDownRaw=" + yDownRaw +
                ", xCurrent=" + xCurrent +
                ", xCurrentRaw=" + xCurrentRaw +
                ", yCurrent=" + yCurrent +
                ", yCurrentRaw=" + yCurrentRaw +
                ", distance=" + distance +
                '}'
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(Const.logTag, "$className.$message")
    }
}