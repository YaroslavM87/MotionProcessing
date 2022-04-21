package com.yaroslavm87.meaningfulmotion.activity

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentTransaction
import com.yaroslavm87.meaningfulmotion.Const
import com.yaroslavm87.meaningfulmotion.R
import com.yaroslavm87.meaningfulmotion.fragment.BaseFragment
import com.yaroslavm87.meaningfulmotion.fragment.animatedBezier.AnimatedBezierFragment
import com.yaroslavm87.meaningfulmotion.fragment.bezier.BezierFragment
import com.yaroslavm87.meaningfulmotion.fragment.interpolatedTension.InterpolatedTensionFragment
import com.yaroslavm87.meaningfulmotion.fragment.reorder.ReorderFragment
import com.yaroslavm87.meaningfulmotion.fragment.shuffle.ShuffleFragment
import com.yaroslavm87.meaningfulmotion.fragment.tension.TensionFragment
import com.yaroslavm87.meaningfulmotion.fragment.touchLog.TouchLogFragment
import com.yaroslavm87.meaningfulmotion.fragment.touchSlop.TouchSlopFragment

class MainActivity : AppCompatActivity() {

    private val className: String = this.javaClass.simpleName
    private val loggingEnabled: Boolean = true

    private lateinit var toolbar: Toolbar
    private val currentFragmentKey: String = "CurrentFragment"
    private var currentFragmentId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        log("onCreate(): savedInstanceState = $savedInstanceState, currentFragmentId = ${savedInstanceState?.getInt(currentFragmentKey)}")
        getFragmentById(savedInstanceState?.getInt(currentFragmentKey)).also {
            toolbar.title = it.javaClass.simpleName
            showFragment(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(currentFragmentKey, currentFragmentId)
    }

    private fun showFragment(fragment: BaseFragment) {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.add(R.id.content_frame, fragment, fragment.javaClass.simpleName)
        ft.commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        currentFragmentId = item.itemId
        getFragmentById(item.itemId).also {
            toolbar.title = it.javaClass.simpleName
            navigateTo(it)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getFragmentById(id: Int?): BaseFragment {
        log("getFragmentByTag()")
        return when(id) {

            R.id.action_touch_log -> {
                log("--- TouchLogFragment")
                TouchLogFragment.newInstance()
            }

            R.id.action_touch_slop -> {
                log("--- TouchSlopFragment")
                TouchSlopFragment.newInstance()
            }

            R.id.action_bezier -> {
                log("--- BezierFragment")
                BezierFragment.newInstance()
            }

            R.id.action_animated_bezier -> {
                log("--- AnimatedBezierFragment")
                AnimatedBezierFragment.newInstance()
            }

            R.id.action_tension -> {
                log("--- TensionFragment")
                TensionFragment.newInstance()
            }

            R.id.action_interpolated_tension -> {
                log("--- InterpolatedTensionFragment")
                InterpolatedTensionFragment.newInstance()
            }

            R.id.action_shuffle -> {
                log("--- ShuffleFragment")
                ShuffleFragment.newInstance()
            }

            R.id.action_reorder -> {
                log("--- ReorderFragment")
                ReorderFragment.newInstance()
            }

            else -> {
                log("--- else")
                ShuffleFragment.newInstance()
            }
        }
    }

    private fun navigateTo(fragment: BaseFragment) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.content_frame)
            ?: throw IllegalStateException("MainActivity.navigateTo(): currentFragment cannot be null")

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                0,
            0,
                R.anim.slide_out_right
            )
            .remove(currentFragment)
            .replace(R.id.content_frame, fragment, fragment.javaClass.simpleName)
            .commit()
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(Const.logTag, "$className.$message")
    }
}