package nebulosa.star.detection

interface StarDetector<in T> {

    fun detect(input: T): List<DetectedStar>
}
