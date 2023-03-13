package nebulosa.jmetro

import javafx.beans.property.BooleanProperty
import javafx.css.CssMetaData
import javafx.css.SimpleStyleableBooleanProperty
import javafx.css.Styleable
import javafx.css.StyleableProperty
import javafx.css.converter.BooleanConverter
import javafx.scene.control.Button
import javafx.scene.control.SkinBase
import javafx.scene.control.skin.ButtonSkin
import java.util.*

class FluentButtonSkin(button: Button) : ButtonSkin(button) {

    private val buttonAnimationHelper: ButtonAnimationHelper

    val shrinkAnimateOnPressProperty: BooleanProperty = SimpleStyleableBooleanProperty(SHRINK_ANIMATE_ON_PRESS_META_DATA, true)

    init {
        buttonAnimationHelper = ButtonAnimationHelper.setupButton(button, shrinkAnimateOnPressProperty)
    }

    val shrinkAnimateOnPress
        get() = shrinkAnimateOnPressProperty.get()

    override fun getCssMetaData() = STYLEABLES

    override fun dispose() {
        buttonAnimationHelper.dispose()
        super.dispose()
    }

    companion object {

        @JvmStatic private val SHRINK_ANIMATE_ON_PRESS_META_DATA =
            object : CssMetaData<Button, Boolean>(ButtonAnimationHelper.SHRINK_ANIMATE_ON_PRESS_PROPERTY_NAME, BooleanConverter.getInstance(), true) {

                override fun isSettable(button: Button): Boolean {
                    val skin = button.skin as FluentButtonSkin
                    return !skin.shrinkAnimateOnPressProperty.isBound
                }

                override fun getStyleableProperty(button: Button): StyleableProperty<Boolean> {
                    val skin = button.skin as FluentButtonSkin
                    return skin.shrinkAnimateOnPressProperty as SimpleStyleableBooleanProperty
                }
            }

        @JvmStatic val STYLEABLES: List<CssMetaData<out Styleable, *>>

        init {
            val styleables = ArrayList(getClassCssMetaData())
            styleables.add(SHRINK_ANIMATE_ON_PRESS_META_DATA)
            STYLEABLES = Collections.unmodifiableList(styleables)
        }
    }
}
