package nebulosa.watney.platesolver.quad

/**
 * Represents a star quad match, i.e. a pair of quads,
 * one from the image and one from the quad database.
 */
data class StarQuadMatch(
    @JvmField val catalogQuad: StarQuad,
    @JvmField val imageQuad: ImageStarQuad,
) : Comparable<StarQuadMatch> {

    @JvmField val scaleRatio = imageQuad.largestDistance / catalogQuad.largestDistance

    override fun compareTo(other: StarQuadMatch): Int {
        return compare(this, other)
    }

    companion object : Comparator<StarQuadMatch> {

        override fun compare(a: StarQuadMatch, b: StarQuadMatch): Int {
            return a.scaleRatio.compareTo(b.scaleRatio)
        }
    }
}
