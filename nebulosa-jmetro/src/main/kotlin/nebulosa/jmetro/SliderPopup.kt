package nebulosa.jmetro

import javafx.beans.binding.StringBinding
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.control.Label
import javafx.scene.control.PopupControl
import javafx.scene.control.Skin
import kotlin.math.roundToInt

internal class SliderPopup : PopupControl() {

    val valueProperty: DoubleProperty = SimpleDoubleProperty()

    init {
        styleClass.add(DEFAULT_STYLE_CLASS)
    }

    var value
        get() = valueProperty.get()
        set(value) {
            valueProperty.set(value)
        }

    override fun createDefaultSkin(): Skin<*> = SliderPopupSkin(this)

    private inner class SliderPopupSkin(private val sliderPopup: SliderPopup) : Skin<SliderPopup> {

        private val valueText = Label()

        init {
            valueText.textProperty().bind(object : StringBinding() {

                init {
                    super.bind(sliderPopup.valueProperty)
                }

                override fun computeValue() = sliderPopup.value.roundToInt().toString()
            })
        }

        override fun getSkinnable() = sliderPopup

        override fun getNode() = valueText

        override fun dispose() {}
    }

    companion object {

        private const val DEFAULT_STYLE_CLASS = "slider-popup"
    }
}
