package com.yaroslavm87.meaningfulmotion.commonViews

import android.content.Context
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.yaroslavm87.meaningfulmotion.R

class TouchLogView : LinearLayout {

    class LoggedEvent(event: MotionEvent) {
        var action = event.action
        var x = event.x
        var y = event.y
    }

    constructor(context: Context) : super(context)
    { init(context) }

    constructor(context: Context, attrs: AttributeSet) :
            super(context, attrs)
    { init(context) }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)
    { init(context) }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)
    { init(context) }

    private lateinit var textViewHistory: TextView
    private val loggedEvents = mutableListOf<LoggedEvent>()
    private val logCapacity = 6

    private fun init(context: Context) {
        orientation = VERTICAL
        inflate(context, R.layout.view_touch_log, this)
        textViewHistory = this.findViewById(R.id.touch_log_text_view)
    }

    fun log(event: MotionEvent?) {
        if (event == null) return
        addOrUpdate(event)
        trim()
        showHistory()
    }

    private fun trim() {
        if (loggedEvents.size > logCapacity) loggedEvents.removeAt(loggedEvents.size - 1)
    }

    private fun addOrUpdate(event: MotionEvent) {
        if (loggedEvents.isEmpty() || event.action != loggedEvents[0].action) {
            loggedEvents.add(0, LoggedEvent(event))
        }
        else {
            loggedEvents[0].apply {
                action = event.action
                x = event.x
                y = event.y
            }
        }
    }

    private fun showHistory() {
        if (loggedEvents.isEmpty()) return
        val history = SpannableStringBuilder()
        for (i in loggedEvents.size - 1 downTo 0) {
            history.append(buildStringFor(loggedEvents[i]))
            if (i != 0) {
                history.append("\n")
            }
        }
        textViewHistory.text = history
    }

    private fun buildStringFor(event: LoggedEvent): SpannableString {
        val s = SpannableString(MotionEvent.actionToString(event.action) + getLocationOf(event))
        val color = pickTextColorFor(event)
        s.setSpan(ForegroundColorSpan(color), 0, s.length, 0)
        return s
    }

    private fun getLocationOf(event: LoggedEvent): String {
        return " (${ event.x }, ${ event.y })"
    }

    private fun pickTextColorFor(event: LoggedEvent): Int {
        return when (event.action) {

            MotionEvent.ACTION_DOWN ->
                ContextCompat.getColor(context, R.color.textColorPrimary)

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                ContextCompat.getColor(context, R.color.textColorAccentError)

            MotionEvent.ACTION_MOVE ->
                ContextCompat.getColor(context, R.color.textColorAccent)

            else ->
                ContextCompat.getColor(context, R.color.textColorAccent)
        }
    }
}