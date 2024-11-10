package nebulosa.image.algorithms.transformation

import nebulosa.image.Image
import nebulosa.image.algorithms.ComputationAlgorithm
import nebulosa.image.algorithms.TransformAlgorithm

data object AutoScreenTransformFunction : ComputationAlgorithm<ScreenTransformFunction.Parameters>, TransformAlgorithm {

    override fun compute(source: Image): ScreenTransformFunction.Parameters {
        return AdaptativeScreenTransformFunction.DEFAULT.compute(source)
    }

    override fun transform(source: Image): Image {
        return ScreenTransformFunction(compute(source)).transform(source)
    }
}
