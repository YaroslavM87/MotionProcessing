package com.yaroslavm87.meaningfulmotion.fragment.interpolatedTension

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.yaroslavm87.meaningfulmotion.R
import com.yaroslavm87.meaningfulmotion.fragment.BaseFragment
import io.apptik.widget.MultiSlider
import kotlin.properties.Delegates

class InterpolatedTensionFragment : BaseFragment() {

    private val logTag: String = "MeaningfulMotion"
    private val className: String = this.javaClass.simpleName
    private val loggingEnabled: Boolean = true

    private lateinit var interpolatedTensionView: InterpolatedTensionTouchSourceView
    private lateinit var tensionText: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var mMultiSlider: MultiSlider

    private val tensionMin = 0F
    private val tensionMax = 5F
    private val tensionStep = (tensionMax - tensionMin) / 100
    private val tensionDefault = (tensionMax - tensionMin) / 2

    private var radiusMin by Delegates.notNull<Int>()
    private var radiusInner by Delegates.notNull<Int>()
//    private val radiusMinDefault = radiusMin / 10
    private var radiusMax by Delegates.notNull<Int>()
    private var radiusOuter by Delegates.notNull<Int>()
//    private val radiusMaxDefault = radiusMax / 10

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_interpolated_tension, container, false)
        initViewComponents(root)
        setViewComponents()
        return root
    }

    private fun initViewComponents(rootView: View) {
        interpolatedTensionView = rootView.findViewById(R.id.interpolated_tension_touch_source_view)
        tensionText = rootView.findViewById(R.id.interpolated_tension_text)
        seekBar = rootView.findViewById(R.id.interpolated_tension_seek_bar)
        mMultiSlider = rootView.findViewById(R.id.interpolated_tension_multi_slider)
    }

    private fun setViewComponents() {
//        interpolatedTensionView.apply {
//            setTension(tensionDefault)
////            setRadiusOuter(radiusMaxDefault)
////            setRadiusInner(radiusMinDefault)
//        }

        radiusMin = interpolatedTensionView.tensionProcessor.radiusMin.toInt()
        radiusMax = interpolatedTensionView.tensionProcessor.radiusMax.toInt()

        tensionText.text = getString(R.string.tension_value, tensionMin)
        seekBar.apply {
            setOnSeekBarChangeListener(createOnSeekBarChangeListener())
            progress = 50
        }
        mMultiSlider.apply {
            setMin(radiusMin)
            setMax(radiusMax)
            getThumb(0).value = interpolatedTensionView.tensionProcessor.radiusInner.toInt()
            getThumb(1).value = interpolatedTensionView.tensionProcessor.radiusOuter.toInt()
            setOnThumbValueChangeListener(createOnThumbValueChangeListener())
        }
    }

    private fun createOnSeekBarChangeListener(): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                with(progress) {
                    val tension = seekBarProgressToTension(this)
                    tensionText.text = getString(R.string.tension_value, tension)
                    interpolatedTensionView.setTension(tension)
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

    private var isNotRecursiveCall = true
    private fun createOnThumbValueChangeListener(): MultiSlider.OnThumbValueChangeListener {
        return MultiSlider.OnThumbValueChangeListener { multiSlider, thumb, thumbIndex, value ->

            if (isNotRecursiveCall) {

                isNotRecursiveCall = false

                val value0 = mMultiSlider.getThumb(0).value
                val value1 = mMultiSlider.getThumb(1).value
                val minDelta = interpolatedTensionView.tensionProcessor.radiiMinDelta

                when (thumbIndex) {

                    0 -> {
                        val valToSet =
                            if (value0 + minDelta <= value1) value0
                            else value1 - minDelta.toInt()
                        interpolatedTensionView.setRadiusInner(valToSet.toFloat())
                        mMultiSlider.getThumb(0).value = valToSet
                    }

                    1 -> {
                        val valToSet =
                            if (value1 - minDelta > value0) value1
                            else value0 + minDelta.toInt()
                        interpolatedTensionView.setRadiusOuter(valToSet.toFloat())
                        mMultiSlider.getThumb(1).value = valToSet
                    }
                }

            } else isNotRecursiveCall = true

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
        fun newInstance(): InterpolatedTensionFragment {
            return InterpolatedTensionFragment()
        }
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(logTag, "$className.$message")
    }
}