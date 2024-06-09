package nebulosa.stardetector

interface StarDetector<in T> {

    fun detect(input: T): List<StarPoint>
}
