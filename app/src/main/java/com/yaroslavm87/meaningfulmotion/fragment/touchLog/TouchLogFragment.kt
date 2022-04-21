package com.yaroslavm87.meaningfulmotion.fragment.touchLog

import android.os.Bundle
import android.view.*
import com.yaroslavm87.meaningfulmotion.R
import com.yaroslavm87.meaningfulmotion.fragment.BaseFragment
import com.yaroslavm87.meaningfulmotion.interfaces.MotionEventListener
import com.yaroslavm87.meaningfulmotion.commonViews.TouchLogView

class TouchLogFragment : BaseFragment(), MotionEventListener {

    private lateinit var touchSourceView: TouchSourceView
    private lateinit var touchLogView: TouchLogView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_touch_log, container, false)
        // touchSourceView = root.findViewWithTag(resources.getString(R.string.touch_source_view))
        touchSourceView = root.findViewById<TouchSourceView>(R.id.touch_source_view).also {
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
        fun newInstance(): TouchLogFragment {
            return TouchLogFragment()
        }
    }

}