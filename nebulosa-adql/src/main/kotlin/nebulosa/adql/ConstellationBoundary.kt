package nebulosa.adql

import adql.query.operand.NumericConstant
import adql.query.operand.function.geometry.PolygonFunction
import nebulosa.io.resource
import nebulosa.math.Angle.Companion.hours

class ConstellationBoundary internal constructor(override val operand: PolygonFunction) : Region {

    constructor(constellationName: String) : this(PolygonFunction(Region.ICRS, BOUNDARY[constellationName.uppercase()]))

    companion object {

        private val BOUNDARY = HashMap<String, MutableList<NumericConstant>>(88)

        init {
            for (line in resource("constellations_bound_in_20.txt")!!.bufferedReader().lines()) {
                if (line.isEmpty() || line.startsWith('#')) continue

                val parts = line.split(" ")
                val rightAscension = parts[0].hours.degrees
                val declination = parts[1].toDouble()
                val name = parts[2].trim()

                with(BOUNDARY.getOrPut(name) { ArrayList(150) }) {
                    add(NumericConstant(rightAscension))
                    add(NumericConstant(declination))
                }
            }
        }
    }
}
