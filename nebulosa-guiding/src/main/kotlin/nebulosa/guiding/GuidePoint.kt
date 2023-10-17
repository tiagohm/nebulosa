package nebulosa.guiding

interface GuidePoint {

    val x: Int

    val y: Int

    companion object {

        @JvmStatic val ZERO = object : GuidePoint {
            override val x = 0
            override val y = 0
        }
    }
}
