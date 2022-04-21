package com.yaroslavm87.meaningfulmotion.fragment.shuffle

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.yaroslavm87.meaningfulmotion.Const
import com.yaroslavm87.meaningfulmotion.R
import com.yaroslavm87.meaningfulmotion.fragment.bezier.TouchStateTracker
import com.yaroslavm87.meaningfulmotion.fragment.interpolatedTension.InterpolatedTensionProcessor
import com.yaroslavm87.meaningfulmotion.processing.TouchState

class ShuffleTouchSourceView : FrameLayout {

    private val className: String = this.javaClass.simpleName
    private val loggingEnabled: Boolean = true

    private val settleAnimationDuration = 400L
    private val elevationAnimationDuration = 400L
    private var isAnimatingZ = false

    private val touchState = TouchState()

    private val tensionFactorDefault = 1.1f
    private val dragDistanceWithoutTension = 100F
    private val maxDragDistance = 100F
    private val touchProcessor = InterpolatedTensionProcessor(touchState, TouchStateTracker(touchState)).apply {
        radiusMin = dragDistanceWithoutTension
        radiusMax = maxDragDistance
        tensionFactor = tensionFactorDefault
    }

    private lateinit var cardTop: View
    private lateinit var cardBottom: View
    private val flagCardA = 1
    private val flagCardB = 2

    /**
     * Flag to indicate which card is on top.
     */
    private var topCardFlag: Int = flagCardA

    /**
     * The resting coordinates of the top card.
     */
    private val restingTopBoundary = Rect()
    private val restingBottomBoundary = Rect()

    /**
     * The absolute value amount that each card is shifted [x,y] so that they're not
     * stacked right on top of each other.
     */
    private var cardTopOffset = 0F
    private var cardBottomOffset = 0F

    /**
     * Extra touch tracking variable.  Remembers the last known drag distance.
     */
    private var lastDragDistance = 0F

    /**
     * Has the current gesture ever passed the radius threshold to swap the cards?
     */
    private var didPassThreshold = false

    /**
     * Was the last observed [MotionEvent.ACTION_DOWN] inside the top card's boundary?
     */
    private var wasDownInsideBounds = false

    private var elevationLow = 0F
    private var elevationMid = 0F
    private var elevationHigh = 0F

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) :
            super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes) {
        init(context)
    }

    private fun init(context: Context) {
        inflate(context, R.layout.view_shuffle, this)
        cardTopOffset = context.resources.getDimension(R.dimen.card_top_translation)
        cardBottomOffset = context.resources.getDimension(R.dimen.card_bottom_translation)
        elevationLow = context.resources.getDimension(R.dimen.elevation_small)
        elevationMid = context.resources.getDimension(R.dimen.elevation_medium)
        elevationHigh = context.resources.getDimension(R.dimen.elevation_large)
        cardTop = findViewById<View?>(R.id.card_a).apply {
            z = elevationMid
        }
        cardBottom = findViewById<View?>(R.id.card_b).apply {
            z = elevationLow
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        log("onLayout(): changed = $changed")
        if (changed) {
            restingTopBoundary.setCoordsAndOffset(cardTop, cardTopOffset.toInt())
            restingBottomBoundary.setCoordsAndOffset(cardBottom, cardBottomOffset.toInt())
        }
    }

    private fun Rect.setCoordsAndOffset(coordSource: View, offset: Int) {
        this.set(coordSource.left + offset,
            coordSource.top + offset,
            coordSource.right + offset,
            coordSource.bottom + offset
        )
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {

        touchProcessor.doOnTouchEvent(this, ev)

        if (actionDownIsInsideRestingTopRect(ev)) {
            setOffsetForDownCoordsWithinTopCard()

        } else if (ev.action == MotionEvent.ACTION_CANCEL || ev.action == MotionEvent.ACTION_UP) {
            if (wasDownInsideBounds) setCardsWithRestingCoords()
            touchState.reset()
            wasDownInsideBounds = false
            isAnimatingZ = false
            resetOffsetForDownCoordsWithinTopCard()
            return super.onInterceptTouchEvent(ev)
        }

        if (wasDownInsideBounds) drawState(touchState)

        return super.onInterceptTouchEvent(ev)
    }

    private fun actionDownIsInsideRestingTopRect(event: MotionEvent): Boolean {
        return event.action == MotionEvent.ACTION_DOWN
                && isInsideRestingTopRect(event.x.toInt(), event.y.toInt())
    }

    private fun isInsideRestingTopRect(x: Int, y: Int): Boolean {
        wasDownInsideBounds = restingTopBoundary.contains(x, y)
        return wasDownInsideBounds
    }

    /**
     * Distances:
     * from left boundary of the Rect to the event.x
     * from right boundary of the Rect to the event.x
     * from top boundary of the Rect to the event.y
     * from bottom boundary of the Rect to the event.y
     */
    private var xDownLeftOffset = 0F
    private var xDownRightOffset = 0F
    private var yDownTopOffset = 0F
    private var yDownBottomOffset = 0F

    private fun setOffsetForDownCoordsWithinTopCard() {
        with (restingTopBoundary) {
            xDownLeftOffset = touchState.xDown - left
            xDownRightOffset = right - touchState.xDown
            yDownTopOffset = touchState.yDown - top
            yDownBottomOffset = bottom - touchState.yDown
            log("setOffsetForDownCoordsWithinTopCard()")
            with (restingTopBoundary) {
                log("cardTop coords: LX=${this.left}, RX=${this.right}, TY=${this.top}, BY=${this.bottom}")
            }
            log("offset: LX=$xDownLeftOffset, RX=$xDownRightOffset, TY=$yDownTopOffset, BY=$yDownBottomOffset")
        }
    }

    private fun resetOffsetForDownCoordsWithinTopCard() {
        xDownLeftOffset = 0F
        xDownRightOffset = 0F
        yDownTopOffset = 0F
        yDownBottomOffset = 0F
    }

    private fun drawState(touchState: TouchState) {
        var touchActualX = touchState.xCurrent
        var touchActualY = touchState.yCurrent

        if (touchActualX == touchState.none || touchActualY == touchState.none) {
            log("drawState(): xCurrent, yCurrent = none, take down coords")
            touchActualX = touchState.xDown
            touchActualY = touchState.yDown
        }

        didPassThreshold = isOutsideOfBottomRect(touchActualX, touchActualY)
        log("drawState(): didPassThreshold = $didPassThreshold")

        val topCard = getTopCard()
        val bottomCard = getBottomCard()

        if (didPassThreshold && !isAnimatingZ) {
            log("drawState(): animate Z")
            isAnimatingZ = true

            // use some value animators instead of View.animate() so we don't
            // conflict with the translation x/y View.animate() call on the top card.
            ValueAnimator.ofFloat(cardTop.z, elevationLow).apply {
                duration = elevationAnimationDuration
                addUpdateListener { animation ->
                    topCard.z = animation.animatedValue as Float
                }
                start()
            }

            ValueAnimator.ofFloat(cardBottom.z, elevationHigh).apply {
                duration = elevationAnimationDuration
                addUpdateListener { animation ->
                    bottomCard.z = animation.animatedValue as Float
                }
                start()
            }

            lastDragDistance = maxDragDistance


        } else {
            lastDragDistance = touchState.distance
        }

        topCard.animate()
            .x(touchActualX - xDownLeftOffset)
            .y(touchActualY - yDownTopOffset)
            .setDuration(0)
            .start()
    }

    private fun isOutsideOfBottomRect(x: Float, y: Float): Boolean {
        val leftX = (x - xDownLeftOffset).toInt()
        val topY = (y - yDownTopOffset).toInt()
        val rightX = (x + xDownRightOffset).toInt()
        val bottomY = (y + yDownBottomOffset).toInt()
        log("")
        log("movingCardA: LT=$leftX,$topY, RT=$rightX,$topY, RB=$rightX,$bottomY, LB=$leftX,$bottomY")
        with (restingBottomBoundary) {
            log("cardB: LT=${this.left},${this.top}, RT=${this.right},${this.top}, RB=${this.right},${this.bottom}, LB=${this.left},${this.bottom}")
        }
        log("")
        log("")

        return !(restingBottomBoundary.contains(leftX, topY) ||
                restingBottomBoundary.contains(rightX, topY) ||
                restingBottomBoundary.contains(rightX, bottomY) ||
                restingBottomBoundary.contains(leftX, bottomY))
    }

    /**
     * Move child views to their resting positions.
     */
    private fun setCardsWithRestingCoords() {
        val cardARestingX: Float
        val cardARestingY: Float
        val cardARestingZ: Float
        val cardBRestingX: Float
        val cardBRestingY: Float
        val cardBRestingZ: Float

        if (didPassThreshold) {
            log("settle(): didPassThreshold = $didPassThreshold")
            cardARestingX = restingBottomBoundary.left.toFloat()
            cardARestingY = restingBottomBoundary.top.toFloat()
            cardARestingZ = elevationLow
            cardBRestingX = restingTopBoundary.left.toFloat()
            cardBRestingY = restingTopBoundary.top.toFloat()
            cardBRestingZ = elevationMid

        } else {
            log("settle(): didPassThreshold = $didPassThreshold")
            cardARestingX = restingTopBoundary.left.toFloat()
            cardARestingY = restingTopBoundary.top.toFloat()
            cardARestingZ = elevationMid
            cardBRestingX = restingBottomBoundary.left.toFloat()
            cardBRestingY = restingBottomBoundary.top.toFloat()
            cardBRestingZ = elevationLow
        }

        getTopCard().animate()
            .x(cardARestingX)
            .y(cardARestingY)
            .z(cardARestingZ)
            .setDuration(settleAnimationDuration)
            .start()

        getBottomCard().animate()
            .x(cardBRestingX)
            .y(cardBRestingY)
            .z(cardBRestingZ)
            .setDuration(settleAnimationDuration)
            .start()

        if (didPassThreshold) {
            topCardFlag = if (topCardFlag == flagCardA) flagCardB else flagCardA
        }
    }

    /**
     * Get whichever card is on top right now.
     */
    private fun getTopCard(): View {
        return if (topCardFlag == flagCardA) cardTop else cardBottom
    }

    /**
     * Get whichever card is on bottom right now.
     */
    private fun getBottomCard(): View {
        return if (topCardFlag == flagCardA) cardBottom else cardTop
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(Const.logTag, "$className.$message")
    }

}
