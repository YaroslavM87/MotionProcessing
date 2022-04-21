package com.yaroslavm87.meaningfulmotion.fragment.bezier

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.yaroslavm87.meaningfulmotion.R
import com.yaroslavm87.meaningfulmotion.fragment.BaseFragment
import com.yaroslavm87.meaningfulmotion.commonViews.TouchLogView
import com.yaroslavm87.meaningfulmotion.interfaces.MotionEventListener

class BezierFragment : BaseFragment(), MotionEventListener {

    private lateinit var touchLogView: TouchLogView
    private lateinit var bezierTouchSourceView: BezierTouchSourceView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_bezier, container, false)
        // bezierView = root.findViewWithTag(resources.getString(R.string.noisy_fragment_log))
        bezierTouchSourceView = root.findViewById<BezierTouchSourceView?>(R.id.bezier_touch_source_view).also {
            it.setMotionEventListener(this)
        }

        touchLogView = root.findViewById(R.id.bezier_touch_log_view)
        return root
    }

    override fun onMotionEvent(event: MotionEvent) {
        touchLogView.log(event)
    }

    companion object {
        @JvmStatic
        fun newInstance(): BezierFragment {
            return BezierFragment()
        }
    }
}