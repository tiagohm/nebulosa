import io.kotest.matchers.shouldBe
import nebulosa.fits.fits
import nebulosa.image.Image.Companion.asImage
import nebulosa.image.algorithms.transformation.adjustment.Brightness
import nebulosa.image.algorithms.transformation.adjustment.Contrast
import nebulosa.image.algorithms.transformation.adjustment.Exposure
import nebulosa.image.algorithms.transformation.adjustment.Fade
import nebulosa.image.algorithms.transformation.adjustment.Gamma
import nebulosa.image.algorithms.transformation.adjustment.Saturation
import nebulosa.test.NGC3344_COLOR_8_FITS
import nebulosa.test.NGC3344_MONO_8_FITS
import nebulosa.test.save
import org.junit.jupiter.api.Test

class ImageAdjustmentAlgorithmTest {

    @Test
    fun monoBrightness() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.transform(Brightness(0.5f))
        mImage.save("fits-mono-brightness").second shouldBe "17d2e51441606c10772211ea22fcd492"
    }

    @Test
    fun monoContrast() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.transform(Contrast(0.5f, useMean = true))
        mImage.save("fits-mono-contrast").second shouldBe "24b24645782785ca04eadf12d60ddba2"
    }

    @Test
    fun monoExposure() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.transform(Exposure(0.5f))
        mImage.save("fits-mono-exposure").second shouldBe "52af420e4700b204de64339f6fad7f0d"
    }

    @Test
    fun monoGamma() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.transform(Gamma(0.5f))
        mImage.save("fits-mono-gamma").second shouldBe "b6306a198fa1ffbffcf3a1881cb8ec60"
    }

    @Test
    fun monoFade() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.transform(Fade(0.5f))
        mImage.save("fits-mono-fade").second shouldBe "9e65606412c1b883fc1ff84043a0995d"
    }

    @Test
    fun colorBrightness() {
        val mImage = NGC3344_COLOR_8_FITS.fits().asImage()
        mImage.transform(Brightness(-0.5f))
        mImage.save("fits-color-brightness").second shouldBe "f152363a1fd9bde3724834d24fad8620"
    }

    @Test
    fun colorContrast() {
        val mImage = NGC3344_COLOR_8_FITS.fits().asImage()
        mImage.transform(Contrast(-0.5f, useMean = true))
        mImage.save("fits-color-contrast").second shouldBe "55ee7ec15724ccea63b0ed8d04ce9682"
    }

    @Test
    fun colorSaturation() {
        val mImage = NGC3344_COLOR_8_FITS.fits().asImage()
        mImage.transform(Saturation(0.5f))
        mImage.save("fits-color-saturation").second shouldBe "e25eb22787abc5502dfe569ae8fcabf9"
    }

    @Test
    fun colorExposure() {
        val mImage = NGC3344_COLOR_8_FITS.fits().asImage()
        mImage.transform(Exposure(-0.5f))
        mImage.save("fits-color-exposure").second shouldBe "ba7bf3246cfe57e3ded8a1e37f9f7000"
    }

    @Test
    fun colorGamma() {
        val mImage = NGC3344_COLOR_8_FITS.fits().asImage()
        mImage.transform(Gamma(-0.5f))
        mImage.save("fits-color-gamma").second shouldBe "a41b55d6ded94deaacd5132067099e61"
    }

    @Test
    fun colorFade() {
        val mImage = NGC3344_COLOR_8_FITS.fits().asImage()
        mImage.transform(Fade(-0.5f))
        mImage.save("fits-color-fade").second shouldBe "bb52140ca354852fdae50cef90dc131d"
    }
}
