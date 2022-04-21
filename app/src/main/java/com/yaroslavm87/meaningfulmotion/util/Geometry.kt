package com.yaroslavm87.meaningfulmotion.util

import android.graphics.Path
import android.graphics.PathMeasure
import kotlin.math.abs
import kotlin.math.sqrt

object Geometry {

    private val sPathMeasure = PathMeasure()

    /**
     * Finds the distance between two given points represented as (x, y)
     */
    fun calcDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val xDelta = abs(x1 - x2)
        val yDelta = abs(y1 - y2)
        return sqrt(yDelta * yDelta + xDelta * xDelta)
    }

    /**
     * Checks if x and y are within affordance of each other.
     */
    fun areEqualWithinGivenApproximation(val1: Float, val2: Float, approximation: Float): Boolean {
        val difference = abs(val1 - val2)
        return difference <= approximation
    }

    /**
     * Given some path and its length, finds the point ([x,y]) on that path at
     * the given percentage of length. Returns result as Pair<Float, Float>.
     *
     * @param path    any path
     * @param length  the length of `path`
     * @param percent the percentage along the path's length to find a point
     */
    fun findPointsFromPercent(path: Path?, length: Float, percent: Float): Pair<Float, Float> {
        val points = FloatArray(2)
        synchronized(sPathMeasure) {
            sPathMeasure.setPath(path, false)
            sPathMeasure.getPosTan(length * percent, points, null)
        }
        return Pair(points[0], points[1])
    }

}