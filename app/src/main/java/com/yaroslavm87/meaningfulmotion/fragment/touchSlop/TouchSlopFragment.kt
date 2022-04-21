package com.yaroslavm87.meaningfulmotion.fragment.touchSlop

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSeekBar
import com.yaroslavm87.meaningfulmotion.R
import com.yaroslavm87.meaningfulmotion.fragment.BaseFragment
import com.yaroslavm87.meaningfulmotion.interfaces.MotionEventListener
import com.yaroslavm87.meaningfulmotion.commonViews.TouchLogView

class TouchSlopFragment : BaseFragment(), MotionEventListener {

    private val logTag: String = "MeaningfulMotion"
    private val className: String = this.javaClass.simpleName
    private val loggingEnabled: Boolean = true

    private lateinit var slopTouchSourceView: SlopTouchSourceView
    private lateinit var touchLogView: TouchLogView
    private lateinit var seekBar: AppCompatSeekBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_touch_slop, container, false)
        slopTouchSourceView = root.findViewById<SlopTouchSourceView>(R.id.slop_touch_source_view).also {
            it.setMotionEventListener(this)
        }
        touchLogView = root.findViewById(R.id.slop_touch_log_view)
        seekBar = initSeekbar(root)

        return root
    }

    override fun onMotionEvent(event: MotionEvent) {
        touchLogView.log(event)
    }

    private fun initSeekbar(root: View): AppCompatSeekBar {
        return root.findViewById<AppCompatSeekBar?>(R.id.slop_seek_bar).apply {
            setOnSeekBarChangeListener(
                object: SeekBar.OnSeekBarChangeListener {

                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        slopTouchSourceView.changeSizeOfTouchSlop(progress)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        // do nothing
                    }
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        // do nothing
                    }

                }
            )
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): TouchSlopFragment {
            return TouchSlopFragment()
        }
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(logTag, "$className.$message")
    }

}