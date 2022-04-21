package com.yaroslavm87.meaningfulmotion.fragment.shuffle;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.yaroslavm87.meaningfulmotion.R;
import com.yaroslavm87.meaningfulmotion.fragment.bezier.TouchStateTracker;
import com.yaroslavm87.meaningfulmotion.fragment.interpolatedTension.InterpolatedTensionProcessor;
import com.yaroslavm87.meaningfulmotion.processing.TouchState;

public class ShuffleView extends FrameLayout {

    private static final float TENSION = 0.8f;
    private static final float AFFORDANCE = .05f;
    private static final int RADIUS_MIN = 100;
    private static final int RADIUS_MAX = 370;
    private static final long SETTLE_ANIMATION_DURATION = 200L;
    private static final long ELEVATION_ANIMATION_DURATION = 200L;
    private static final int FLAG_CARD_A = 1;
    private static final int FLAG_CARD_B = 2;

    private View cardA;
    private View cardB;

    private TouchState mState;
    private InterpolatedTensionProcessor mTouchProcessor;
    /**
     * The resting coordinates of the top card.
     */
    private Rect mRestingBoundary;
    /**
     * The absolute value amount that each card is shifted [x,y] so that they're not
     * stacked right on top of each other.
     */
    private float mCardOffset;
    /**
     * Extra touch tracking variable.  Remembers the last known drag distance.
     */
    private float mLastDistance = 0;
    /**
     * Has the current gesture ever passed the radius threshold to swap the cards?
     */
    private boolean mDidPassThreshold = false;
    /**
     * Was the last observed {@link MotionEvent#ACTION_DOWN} inside the top card's boundary?
     */
    private boolean mDownInsideBounds = false;
    /**
     * Flag to indicate which card is on top.
     */
    private int mTopCardFlag = FLAG_CARD_A;
    private float mXOffset;
    private float mYOffset;
    private float mElevationLow;
    private float mElevationMid;
    private float mElevationHigh;

    public ShuffleView(Context context) {
        super(context);
        init(context);
    }

    public ShuffleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ShuffleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public ShuffleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.view_shuffle, this);
        mState = new TouchState();
        mCardOffset = context.getResources().getDimension(R.dimen.card_top_translation);
        mTouchProcessor = new InterpolatedTensionProcessor(mState, new TouchStateTracker(mState));
        mTouchProcessor.setRadiusInner(RADIUS_MIN);
        mTouchProcessor.setRadiusOuter(RADIUS_MAX);
        mTouchProcessor.setTensionFactor(TENSION);
        mRestingBoundary = new Rect();
        mElevationLow = getResources().getDimension(R.dimen.elevation_small);
        mElevationMid = getResources().getDimension(R.dimen.elevation_medium);
        mElevationHigh = getResources().getDimension(R.dimen.elevation_large);
        cardA = findViewById(R.id.card_a);
        cardB = findViewById(R.id.card_b);
        cardB.setZ(mElevationLow);
        cardA.setZ(mElevationMid);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mRestingBoundary.set(
                    cardA.getLeft() + (int) mCardOffset,
                    cardA.getTop() + (int) mCardOffset,
                    cardA.getRight() + (int) mCardOffset,
                    cardA.getBottom() + (int) mCardOffset);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d("MyApp", "onInterceptTouchEvent(): MotionEvent=" + ev);
        final int action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN && insideRestingRect(ev)) {
            mXOffset = ev.getX() - mRestingBoundary.left;
            mYOffset = ev.getY() - mRestingBoundary.top;
            mDownInsideBounds = true;
        } else if (action == MotionEvent.ACTION_CANCEL
                || action == MotionEvent.ACTION_UP) {
            if (mDownInsideBounds) {
                settle();
            }
            mState.reset();
            mDownInsideBounds = false;
            return super.onInterceptTouchEvent(ev);
        }

        mTouchProcessor.doOnTouchEvent(this, ev);
        drawState(mState);
        return super.onInterceptTouchEvent(ev);
    }

    private void drawState(TouchState s) {
        if (!mDownInsideBounds) {
            return;
        }
        float toX = s.getXCurrent();
        float toY = s.getYCurrent();

        Log.d("MyApp", "drawState(): toX=" + toX + ", toY=" + toY);

        if (toX == mState.getNone() || toY == mState.getNone()) {
            toX = s.getXDown();
            toY = s.getYDown();
            mDidPassThreshold = false;
        }

        final View topCard = getTopCard();
        final View bottomCard = getBottomCard();

        if (areEqualWithinGivenApproximation(s.getDistance(), RADIUS_MAX, AFFORDANCE)
                && mLastDistance < (RADIUS_MAX - AFFORDANCE)) {
            mDidPassThreshold = true;
            // use some value animators instead of View.animate() so we don't
            // conflict with the translation x/y View.animate() call on the top card.
            ValueAnimator topAnimator = ValueAnimator.ofFloat(cardA.getZ(), mElevationLow)
                    .setDuration(ELEVATION_ANIMATION_DURATION);
            topAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    topCard.setZ((float) animation.getAnimatedValue());
                }
            });
            topAnimator.start();

            ValueAnimator bottomAnimator = ValueAnimator.ofFloat(cardB.getZ(), mElevationHigh)
                    .setDuration(ELEVATION_ANIMATION_DURATION);
            bottomAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    bottomCard.setZ((float) animation.getAnimatedValue());
                }
            });
            bottomAnimator.start();
            mLastDistance = RADIUS_MAX;
        } else {
            mLastDistance = s.getDistance();
        }

        topCard.animate()
                .x(toX - mXOffset)
                .y(toY - mYOffset)
                .setDuration(0)
                .start();
    }

    /**
     * Move child views to their resting positions.
     */
    private void settle() {
        final View topCard = getTopCard();
        final View bottomCard = getBottomCard();
        float topCardRestingX;
        float topCardRestingY;
        float topCardRestingZ;
        float bottomCardRestingX;
        float bottomCardRestingY;
        float bottomCardRestingZ;

        if (mDidPassThreshold) {
            topCardRestingX = mRestingBoundary.left - 2 * mCardOffset;
            topCardRestingY = mRestingBoundary.top - 2 * mCardOffset;
            topCardRestingZ = mElevationLow;
            bottomCardRestingX = mRestingBoundary.left;
            bottomCardRestingY = mRestingBoundary.top;
            bottomCardRestingZ = mElevationMid;
        } else {
            topCardRestingX = mRestingBoundary.left;
            topCardRestingY = mRestingBoundary.top;
            topCardRestingZ = mElevationMid;
            bottomCardRestingX = mRestingBoundary.left - 2 * mCardOffset;
            bottomCardRestingY = mRestingBoundary.top - 2 * mCardOffset;
            bottomCardRestingZ = mElevationLow;
        }

        topCard.animate()
                .x(topCardRestingX)
                .y(topCardRestingY)
                .z(topCardRestingZ)
                .setDuration(SETTLE_ANIMATION_DURATION)
                .start();

        bottomCard.animate()
                .x(bottomCardRestingX)
                .y(bottomCardRestingY)
                .z(bottomCardRestingZ)
                .setDuration(SETTLE_ANIMATION_DURATION)
                .start();
        if (mDidPassThreshold) {
            mTopCardFlag = mTopCardFlag == FLAG_CARD_A ? FLAG_CARD_B : FLAG_CARD_A;
        }
    }

    private boolean insideRestingRect(MotionEvent ev) {
        final float x = ev.getX();
        final float y = ev.getY();
        return mRestingBoundary.contains((int) x, (int) y);
    }

    /**
     * Get whichever card is on top right now.
     */
    private View getTopCard() {
        return mTopCardFlag == FLAG_CARD_A ? cardA : cardB;
    }

    /**
     * Get whichever card is on bottom right now.
     */
    private View getBottomCard() {
        return mTopCardFlag == FLAG_CARD_A ? cardB : cardA;
    }

    private Boolean areEqualWithinGivenApproximation(float val1, float val2, float approximation)  {
        float difference = Math.abs(val1 - val2);
        return difference <= approximation;
    }
}
