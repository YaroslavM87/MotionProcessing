package com.yaroslavm87.meaningfulmotion.fragment.tension

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import android.widget.*
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.yaroslavm87.meaningfulmotion.R
import com.yaroslavm87.meaningfulmotion.fragment.BaseFragment
import com.yaroslavm87.meaningfulmotion.interfaces.MotionEventListener

class TensionFragment : BaseFragment() {

    private val logTag: String = "MeaningfulMotion"
    private val className: String = this.javaClass.simpleName
    private val loggingEnabled: Boolean = true

    private lateinit var tensionTouchSourceView: TensionTouchSourceView
    private lateinit var tensionText: TextView
    private lateinit var seekBar: SeekBar

    private val tensionMin = 0.1F
    private val tensionMax = 0.9F
    private val tensionStep = (tensionMax - tensionMin) / 100

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_tension, container, false)
        initViewComponents(root)
        setViewComponents()
        return root
    }

    private fun initViewComponents(rootView: View) {
        tensionTouchSourceView = rootView.findViewById(R.id.tension_touch_source_view)
        tensionText = rootView.findViewById(R.id.tension_text)
        seekBar = rootView.findViewById(R.id.tension_seekbar)
    }

    private fun setViewComponents() {
        tensionText.text = getString(R.string.tension_value, tensionMin)
        seekBar.setOnSeekBarChangeListener(createOnSeekBarChangeListener())
    }

    private fun createOnSeekBarChangeListener(): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                with(progress) {
                    val tension = seekBarProgressToTension(this)
                    tensionText.text = getString(R.string.tension_value, tension)
                    tensionTouchSourceView.setTension(tension)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // do nothing
            }
        }
    }

    private fun seekBarProgressToTension(seekBarProgress: Int): Float {
        return when(seekBarProgress) {
            0 -> tensionMin
            100 -> tensionMax
            else -> tensionStep * seekBarProgress + tensionMin
        }.also {
            log("seekBarProgressToTension(): tension = $it")
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(): TensionFragment {
            return TensionFragment()
        }
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(logTag, "$className.$message")
    }
}