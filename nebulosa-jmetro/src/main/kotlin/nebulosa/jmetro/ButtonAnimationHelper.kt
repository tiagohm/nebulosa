package nebulosa.jmetro

import javafx.animation.Interpolator
import javafx.animation.ScaleTransition
import javafx.beans.property.BooleanProperty
import javafx.event.EventHandler
import javafx.scene.control.ButtonBase
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.util.Duration

internal class ButtonAnimationHelper private constructor(
    private val button: ButtonBase,
    private val buttonShrinkAnimateOnPressProperty: BooleanProperty,
) {

    private val buttonPressed = EventHandler(::onButtonPressed)
    private val buttonReleased = EventHandler(::onButtonReleased)
    private val keyPressed = EventHandler(::onKeyPressed)
    private val keyReleased = EventHandler(::onKeyReleased)
    private var isKeyPressed = false

    init {
        button.addEventHandler(MouseEvent.MOUSE_PRESSED, buttonPressed)
        button.addEventHandler(MouseEvent.MOUSE_RELEASED, buttonReleased)
        button.addEventHandler(KeyEvent.KEY_PRESSED, keyPressed)
        button.addEventHandler(KeyEvent.KEY_RELEASED, keyReleased)
    }

    private fun onButtonPressed(mouseEvent: MouseEvent) {
        if (mouseEvent.button != MouseButton.PRIMARY) return
        performShrink()
    }

    private fun onButtonReleased(mouseEvent: MouseEvent) {
        if (mouseEvent.button != MouseButton.PRIMARY) return
        performUnshrink()
    }

    private fun onKeyPressed(event: KeyEvent) {
        if (event.code === KeyCode.ENTER || event.code === KeyCode.SPACE) {
            performShrink()
            isKeyPressed = true
        }
    }

    private fun onKeyReleased(event: KeyEvent) {
        if (isKeyPressed) {
            performUnshrink()
            isKeyPressed = false
        }
    }

    private fun performShrink() {
        if (buttonShrinkAnimateOnPressProperty.get()) {
            button.scaleX = SCALE_ON_PRESS
            button.scaleY = SCALE_ON_PRESS
        }
    }

    private fun performUnshrink() {
        if (buttonShrinkAnimateOnPressProperty.get()) {
            val scaleTransition = ScaleTransition(SCALE_TRANSITION_DURATION, button)
            scaleTransition.interpolator = Interpolator.EASE_OUT
            scaleTransition.toX = 1.0
            scaleTransition.toY = 1.0
            scaleTransition.play()
        }
    }

    fun dispose() {
        button.removeEventHandler(MouseEvent.MOUSE_PRESSED, buttonPressed)
        button.removeEventHandler(MouseEvent.MOUSE_RELEASED, buttonReleased)
        button.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressed)
        button.removeEventHandler(KeyEvent.KEY_RELEASED, keyReleased)
    }

    companion object {

        const val SCALE_ON_PRESS = 0.97
        const val SHRINK_ANIMATE_ON_PRESS_PROPERTY_NAME = "-shrink-animate-on-press"

        @JvmStatic private val SCALE_TRANSITION_DURATION = Duration.millis(400.0)

        @JvmStatic
        fun setupButton(button: ButtonBase, buttonShrinkAnimateOnPressProperty: BooleanProperty): ButtonAnimationHelper {
            return ButtonAnimationHelper(button, buttonShrinkAnimateOnPressProperty)
        }
    }
}
