package nebulosa.guiding.internal

import nebulosa.guiding.Guider

interface InternalGuider : Guider {

    var xGuideAlgorithm: GuideAlgorithm?

    var yGuideAlgorithm: GuideAlgorithm?

    var camera: GuidingCamera

    var mount: GuidingMount

    var pulse: GuidingPulse

    var rotator: GuidingRotator?
}
