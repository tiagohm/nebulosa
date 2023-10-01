package nebulosa.adql

import adql.query.operand.StringConstant
import adql.query.operand.function.geometry.GeometryFunction

interface Region : Operand<GeometryFunction> {

    companion object {

        internal val ICRS = StringConstant("ICRS")
    }
}
