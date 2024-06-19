package nebulosa.api.autofocus

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.curve.fitting.*
import nebulosa.math.evenlySpacedNumbers
import org.springframework.stereotype.Component
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Component
class AutoFocusEventChartSerializer : StdSerializer<AutoFocusEvent.Chart>(AutoFocusEvent.Chart::class.java) {

    override fun serialize(chart: AutoFocusEvent.Chart?, gen: JsonGenerator, provider: SerializerProvider) {
        if (chart == null) {
            gen.writeNull()
        } else {
            gen.writeStartObject()

            gen.writePOJOField("predictedFocusPoint", chart.predictedFocusPoint)
            gen.writeNumberField("minX", chart.minX)
            gen.writeNumberField("minY", chart.minY)
            gen.writeNumberField("maxX", chart.maxX)
            gen.writeNumberField("maxY", chart.maxY)

            if (chart.trendLine != null || chart.parabolic != null || chart.hyperbolic != null) {
                val delta = chart.maxX - chart.minX
                val stepSize = max(3, min((delta / 10.0).roundToInt().let { if (it % 2 == 0) it + 1 else it }, 101))
                val points = if (delta <= 0.0) doubleArrayOf(chart.minX) else evenlySpacedNumbers(chart.minX, chart.maxX, stepSize)
                chart.trendLine?.serialize(gen, points)
                chart.parabolic?.serialize(gen, points)
                chart.hyperbolic?.serialize(gen, points)
            }

            gen.writeEndObject()
        }
    }

    companion object {

        @Suppress("NOTHING_TO_INLINE")
        private inline fun Double.isRSquaredValid() = isFinite() && this > 0.0

        private inline fun <T : FittedCurve> T?.serializeAsFittedCurve(gen: JsonGenerator, fieldName: String, block: (T) -> Unit = {}) {
            if (this != null && rSquared.isRSquaredValid()) {
                gen.writeObjectFieldStart(fieldName)
                gen.writeNumberField("rSquared", rSquared)
                gen.writePOJOField("minimum", minimum)
                block(this)
                gen.writeEndObject()
            }
        }

        @JvmStatic
        private fun TrendLineFitting.Curve?.serialize(gen: JsonGenerator, points: DoubleArray) {
            serializeAsFittedCurve(gen, "trendLine") {
                it.left.serialize(gen, "left", points)
                it.right.serialize(gen, "right", points)
                gen.writePOJOField("intersection", it.intersection)
            }
        }

        @JvmStatic
        private fun TrendLine.serialize(gen: JsonGenerator, fieldName: String, points: DoubleArray) {
            gen.writeObjectFieldStart(fieldName)
            gen.writeNumberField("slope", slope)
            gen.writeNumberField("intercept", intercept)
            gen.writeNumberField("rSquared", rSquared)

            if (rSquared.isRSquaredValid()) {
                makePoints(gen, points)
            }

            gen.writeEndObject()
        }

        @JvmStatic
        private fun QuadraticFitting.Curve?.serialize(gen: JsonGenerator, points: DoubleArray) {
            serializeAsFittedCurve(gen, "parabolic") {
                if (it.rSquared.isRSquaredValid()) {
                    it.makePoints(gen, points)
                }
            }
        }

        @JvmStatic
        private fun HyperbolicFitting.Curve?.serialize(gen: JsonGenerator, points: DoubleArray) {
            serializeAsFittedCurve(gen, "hyperbolic") {
                gen.writeNumberField("a", it.a)
                gen.writeNumberField("b", it.b)
                gen.writeNumberField("p", it.p)

                if (it.rSquared.isRSquaredValid()) {
                    it.makePoints(gen, points)
                }
            }
        }

        @JvmStatic
        private fun Curve.makePoints(gen: JsonGenerator, points: DoubleArray) {
            gen.writeArrayFieldStart("points")

            for (x in points) {
                gen.writeStartObject()
                gen.writeNumberField("x", x)
                gen.writeNumberField("y", this(x))
                gen.writeEndObject()
            }

            gen.writeEndArray()
        }
    }
}
