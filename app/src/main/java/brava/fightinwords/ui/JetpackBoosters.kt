package brava.fightinwords.ui

import android.util.Log
import brava.fightinwords.botlin.blog
import org.jetbrains.annotations.Contract

object JetpackBoosters {
    @Contract(pure = true)
    inline fun <I> shrinkToFit(
        maxValue: I,
        minValue: I,
        checkFit: (I) -> Boolean,
        bigShrinker: (I) -> I,
        littleGrower: (I) -> I,
        maxShrinks: Int = 10,
        maxGrows: Int = 10,
    ): I {
        val logLevel = Log.VERBOSE

        // If the `minValue` doesn't fit, then we know we're going to use it anyways
        if (checkFit(minValue) == false) {
            blog(level = logLevel) { "minValue $minValue doesn't fit; so that's what we're going to wind up with anyways" }
            return minValue
        }

        var shrunk = maxValue
        var shrinkCount = 0
        while (checkFit(shrunk) == false) {
            shrinkCount += 1
            if (shrinkCount > maxShrinks) {
                blog(level = logLevel) { "Still couldn't fit after maxShrinks of $maxShrinks with the value: $shrunk" }
                return shrunk
            }
            shrunk = bigShrinker(shrunk)
        }

        blog(level = logLevel) { "Shrunk $maxValue down to: $shrunk; growing..." }

        if (shrinkCount == 0) {
            blog(level = logLevel) { "maxValue $maxValue already fits!" }
            return shrunk
        }

        var grown = shrunk
        var growCount = 0
        while (checkFit(grown)) {
            growCount += 1
            if (growCount > maxGrows) {
                blog(level = logLevel) { "Still fit after maxGrows of $maxGrows with the value: $grown" }
                return grown
            }
            shrunk = grown
            grown = littleGrower(grown)
        }

        blog(level = logLevel) { "Grown value $grown does NOT fit; returning previous value: $shrunk" }

        return shrunk
    }
}