package com.yaroslavm87.meaningfulmotion.fragment.reorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.yaroslavm87.meaningfulmotion.R
import com.yaroslavm87.meaningfulmotion.fragment.BaseFragment
import com.yaroslavm87.meaningfulmotion.commonViews.TouchLogView
import com.yaroslavm87.meaningfulmotion.interfaces.MotionEventListener

class ReorderFragment : BaseFragment(), MotionEventListener {

    private lateinit var touchLogView: TouchLogView
    private lateinit var reorderBezierTouchSourceView: ReorderBezierTouchSourceView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_reorder, container, false)
        reorderBezierTouchSourceView = root.findViewById<ReorderBezierTouchSourceView?>(R.id.reorder_touch_target).also {
            it.setMotionEventListener(this)
        }

        touchLogView = root.findViewById(R.id.reorder_touch_log_view)
        return root
    }

    companion object {
        @JvmStatic
        fun newInstance(): ReorderFragment {
            return ReorderFragment()
        }
    }

    override fun onMotionEvent(event: MotionEvent) {
        touchLogView.log(event)
    }
}