package nebulosa.api.image

import java.nio.file.Path

sealed interface ImageToken {

    val path: Path

    data class Saved(override val path: Path) : ImageToken

    data object Guiding : ImageToken {

        override val path = Path.of(GUIDING_TOKEN)
    }

    data object Framing : ImageToken {

        override val path = Path.of(FRAMING_TOKEN)
    }

    companion object {

        const val GUIDING_TOKEN = "@guiding"
        const val FRAMING_TOKEN = "@framing"

        @JvmStatic
        fun of(token: String) = when (token) {
            GUIDING_TOKEN -> Guiding
            FRAMING_TOKEN -> Framing
            else -> Saved(Path.of(token))
        }
    }
}
