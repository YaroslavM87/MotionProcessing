package com.yaroslavm87.meaningfulmotion.fragment.shuffle

import android.os.Bundle
import android.view.*
import com.yaroslavm87.meaningfulmotion.R
import com.yaroslavm87.meaningfulmotion.fragment.BaseFragment

class ShuffleFragment : BaseFragment() {

    private lateinit var shuffleTouchSourceView: ShuffleTouchSourceView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_shuffle, container, false)
        shuffleTouchSourceView = root.findViewById(R.id.shuffle_touch_source_view)
        return root
    }

    companion object {
        @JvmStatic
        fun newInstance(): ShuffleFragment {
            return ShuffleFragment()
        }
    }

}