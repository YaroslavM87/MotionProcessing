package com.yaroslavm87.meaningfulmotion.fragment.animatedBezier

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

class AnimatedBezierFragment : BaseFragment() {

    private val logTag: String = "MeaningfulMotion"
    private val className: String = this.javaClass.simpleName
    private val loggingEnabled: Boolean = true

    private lateinit var animatedBezierTouchSourceView: AnimatedBezierTouchSourceView
    private lateinit var durationText: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var spinner: Spinner

    private val animationDurationMin = 100L
    private val animationDurationMax = 5000L
    private val animationDurationStep = (animationDurationMax - animationDurationMin) / 100

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_animated_bezier, container, false)
        initViewComponents(root)
        setViewComponents()
        return root
    }

    private fun initViewComponents(rootView: View) {
        animatedBezierTouchSourceView = rootView.findViewById(R.id.animated_bezier_touch_source_view)
        durationText = rootView.findViewById(R.id.animated_bezier_duration_text)
        seekBar = rootView.findViewById(R.id.animated_bezier_duration_seekbar)
        spinner = rootView.findViewById(R.id.animated_bezier_interpolator_spinner)
    }

    private fun setViewComponents() {
        durationText.text = getString(R.string.duration, animationDurationMin)
        seekBar.setOnSeekBarChangeListener(createOnSeekBarChangeListener())
        spinner.adapter = createSpinnerAdapter()
        spinner.onItemSelectedListener = createSpinnerOnItemSelectedListener()
    }

    private fun createSpinnerAdapter(): SpinnerAdapter {
        return ArrayAdapter.createFromResource(
            requireContext(),
            R.array.interpolator_array,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun createOnSeekBarChangeListener(): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                with(progress) {
                    val duration = seekBarProgressToAnimationDuration(this)
                    durationText.text = getString(R.string.duration, duration)
                    animatedBezierTouchSourceView.setAnimationDuration(duration)
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

    private fun seekBarProgressToAnimationDuration(seekBarProgress: Int): Long {
        return when(seekBarProgress) {
            0 -> animationDurationMin
            100 -> animationDurationMax
            else -> animationDurationStep * seekBarProgress + animationDurationMin
        }.also {
            log("seekBarProgressToAnimationDuration(): animationDuration = $it")
        }
    }

    private fun createSpinnerOnItemSelectedListener(): AdapterView.OnItemSelectedListener {
        return object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                animatedBezierTouchSourceView.setInterpolator(
                    getInterpolatorFromSpinnerPosition(position)
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }
        }
    }

    private fun getInterpolatorFromSpinnerPosition(position: Int): Interpolator {
        return when (position) {
            0 -> AccelerateInterpolator(.5f)
            1 -> AccelerateDecelerateInterpolator()
            2 -> AnticipateInterpolator()
            3 -> AnticipateOvershootInterpolator()
            4 -> BounceInterpolator()
            5 -> DecelerateInterpolator()
            6 -> FastOutLinearInInterpolator()
            7 -> FastOutSlowInInterpolator()
            8 -> LinearInterpolator()
            9 -> OvershootInterpolator()
            else -> AccelerateInterpolator(.5f)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): AnimatedBezierFragment {
            return AnimatedBezierFragment()
        }
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(logTag, "$className.$message")
    }
}