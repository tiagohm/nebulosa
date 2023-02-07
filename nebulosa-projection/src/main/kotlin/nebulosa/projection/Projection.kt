package nebulosa.projection

import nebulosa.math.Vector3D

interface Projection {

    fun project(position: Vector3D): Point
}
