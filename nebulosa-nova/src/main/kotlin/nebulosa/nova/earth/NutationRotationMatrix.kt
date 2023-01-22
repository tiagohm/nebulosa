package nebulosa.nova.earth

import nebulosa.math.Angle
import nebulosa.math.Matrix3D
import org.ejml.data.DMatrix3x3

class NutationRotationMatrix(
    val meanObliquity: Angle,
    val trueObliquity: Angle,
    val psi: Angle,
) : Matrix3D(compute(meanObliquity, trueObliquity, psi)) {

    companion object {

        @JvmStatic
        private fun compute(
            meanObliquity: Angle,
            trueObliquity: Angle,
            psi: Angle,
        ): DMatrix3x3 {
            val cobm = meanObliquity.cos
            val sobm = meanObliquity.sin
            val cobt = trueObliquity.cos
            val sobt = trueObliquity.sin
            val cpsi = psi.cos
            val spsi = psi.sin

            return DMatrix3x3(
                cpsi, -spsi * cobm, -spsi * sobm,
                spsi * cobt, cpsi * cobm * cobt + sobm * sobt, cpsi * sobm * cobt - cobm * sobt,
                spsi * sobt, cpsi * cobm * sobt - sobm * cobt, cpsi * sobm * sobt + cobm * cobt,
            )
        }
    }
}
