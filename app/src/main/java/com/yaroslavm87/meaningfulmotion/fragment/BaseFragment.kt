package com.yaroslavm87.meaningfulmotion.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    private var mHasOnSaveInstanceStateBeenCalled = false

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mHasOnSaveInstanceStateBeenCalled = true
    }

    override fun onResume() {
        super.onResume()
        mHasOnSaveInstanceStateBeenCalled = false
    }

    open fun hasOnSaveInstanceStateBeenCalled(): Boolean {
        return mHasOnSaveInstanceStateBeenCalled
    }


}