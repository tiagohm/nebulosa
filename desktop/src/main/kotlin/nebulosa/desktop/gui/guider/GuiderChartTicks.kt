package nebulosa.desktop.gui.guider

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.control.Label
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import nebulosa.desktop.logic.on
import java.math.BigDecimal

class GuiderChartTicks : GridPane() {

    val amountProperty = SimpleIntegerProperty(11)

    val minScaleProperty = SimpleObjectProperty(BigDecimal("0.5"))

    val maxScaleProperty = SimpleObjectProperty(BigDecimal("16.0"))

    val minValueProperty = SimpleObjectProperty(BigDecimal("-1.0"))

    val maxValueProperty = SimpleObjectProperty(BigDecimal("1.0"))

    val tickAlignmentProperty = SimpleObjectProperty(Pos.CENTER_RIGHT)

    var scale = BigDecimal("1.0")
        private set

    var stepSize = BigDecimal("5.0")
        private set

    init {

        maxHeight = Double.POSITIVE_INFINITY
        columnConstraints.add(ColumnConstraints().also { it.hgrow = Priority.ALWAYS })
        rowConstraints.add(RowConstraints().also { it.vgrow = Priority.ALWAYS; it.percentHeight = 100.0 / amount })

        refreshLabels()

        amountProperty.on { refreshLabels(); updateStepSize() }
        maxScaleProperty.on { updateTickLabels() }
        minScaleProperty.on { updateTickLabels() }
        maxValueProperty.on { updateStepSize() }
        minValueProperty.on { updateStepSize() }
        tickAlignmentProperty.on { refreshLabels(true) }
    }

    var tickAlignment
        get() = tickAlignmentProperty.get()!!
        set(value) {
            tickAlignmentProperty.set(value)
        }

    var amount
        get() = amountProperty.get()
        set(value) {
            require(amount % 2 == 1) { "odd amount is required: $amount" }
            amountProperty.set(value)
        }

    var maxScale
        get() = maxScaleProperty.get()!!
        set(value) {
            maxScaleProperty.set(value)
        }

    var minScale
        get() = minScaleProperty.get()!!
        set(value) {
            minScaleProperty.set(value)
        }

    var maxValue
        get() = maxValueProperty.get()!!
        set(value) {
            maxValueProperty.set(value)
        }

    var minValue
        get() = minValueProperty.get()!!
        set(value) {
            minValueProperty.set(value)
        }

    private fun updateStepSize() {
        stepSize = BigDecimal(amount - 1).divide(maxValue.subtract(minValue))
        updateTickLabels()
    }

    fun incrementScale() {
        if (scale < maxScale) {
            scale = scale.multiply(SCALE_DIVISOR)
            updateTickLabels()
        }
    }

    fun decrementScale() {
        if (scale > minScale) {
            scale = scale.divide(SCALE_DIVISOR)
            updateTickLabels()
        }
    }

    private fun refreshLabels(onlyProperties: Boolean = false) {
        if (onlyProperties) {
            for (child in children) {
                with(child as Label) {
                    alignment = tickAlignment
                }
            }
        } else {
            children.clear()

            val mid = amount / 2

            for (i in 0 until amount) {
                with(Label()) {
                    styleClass.add("text-xs")
                    minWidth = 28.0
                    alignment = tickAlignment
                    setVgrow(this, Priority.ALWAYS)
                    if (i < mid) setValignment(this, VPos.TOP)
                    else if (i == mid) setValignment(this, VPos.CENTER)
                    else setValignment(this, VPos.BOTTOM)
                    setRowIndex(this, i)
                    this@GuiderChartTicks.children.add(this)
                }
            }

            updateTickLabels()
        }
    }

    private fun updateTickLabels() {
        var value = maxValue.multiply(scale)
        val step = scale.divide(stepSize)

        for (label in children) {
            (label as Label).text = "%.1f\"".format(value)
            value -= step
        }
    }

    companion object {

        @JvmStatic private val SCALE_DIVISOR = BigDecimal("2.0")
    }
}
