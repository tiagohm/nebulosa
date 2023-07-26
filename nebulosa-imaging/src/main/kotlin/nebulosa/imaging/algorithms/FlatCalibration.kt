package nebulosa.imaging.algorithms

import nebulosa.imaging.Image

class FlatCalibration(private val flatFrame: Image) : TransformAlgorithm {

    override fun transform(source: Image): Image {
        require(source.width == flatFrame.width) { "calibration image width does not match source width" }
        require(source.height == flatFrame.height) { "calibration image height does not match source height" }

        return source
    }
}
