import io.kotest.matchers.ints.shouldBeExactly
import nebulosa.nasa.spk.NAIF
import org.junit.jupiter.api.Test

@Suppress("DEPRECATION")
class NAIFTest {

    @Test
    fun yeomans() {
        NAIF.originalPermanentAsteroidNumber(2956) shouldBeExactly 2002956
        NAIF.extendedPermanentAsteroidNumber(2956) shouldBeExactly 20002956
    }

    @Test
    fun didymos() {
        NAIF.extendedPermanentAsteroidNumber(65803) shouldBeExactly NAIF.DIDYMOS_BARYCENTER
        NAIF.extendedPrimaryBodyOfPermanentAsteroidNumber(65803) shouldBeExactly NAIF.DIDYMOS
        NAIF.extendedSatelliteOfPermanentAsteroidNumber(65803, 1) shouldBeExactly NAIF.DIMORPHOS
    }
}
