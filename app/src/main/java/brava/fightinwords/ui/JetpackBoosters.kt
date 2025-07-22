package brava.fightinwords.ui

object JetpackBoosters {
    fun <I> shrinkToFit(
        maxValue: I,
        checkFit: (I) -> Boolean,
        bigShrinker: (I) -> I,
        littleGrower: (I) -> I,
        maxShrinks: Int = 10,
        maxGrows: Int = 10
    ): I {
        var shrunk = maxValue
        var shrinkCount = 0
        while (checkFit(shrunk) == false) {
            shrinkCount += 1
            if (shrinkCount > maxShrinks) {
                break
            }
            shrunk = bigShrinker(shrunk)
        }

        var grown = shrunk
        var growCount = 0
        while (checkFit(grown)) {
            growCount += 1
            if (growCount > maxGrows) {
                break
            }
            shrunk = grown
            grown = littleGrower(grown)
        }

        return shrunk
    }
}