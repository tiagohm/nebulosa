package nebulosa.jmetro

import javafx.animation.FadeTransition
import javafx.beans.property.BooleanProperty
import javafx.beans.value.ObservableValue
import javafx.css.*
import javafx.css.converter.BooleanConverter
import javafx.event.EventHandler
import javafx.geometry.Orientation
import javafx.scene.chart.NumberAxis
import javafx.scene.control.Slider
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.util.Duration
import java.util.*
import kotlin.math.max

class SliderSkin(slider: Slider) : javafx.scene.control.skin.SliderSkin(slider) {

    private val fill = StackPane()
    private val thumb = skinnable.lookup(".thumb") as StackPane
    private val track = skinnable.lookup(".track") as StackPane
    private val trackToTickGap = 2.0
    private val popup = SliderPopup()

    private fun thickMarksChanged(observableValue: ObservableValue<*>) {
        children.add(children.indexOf(track) + 1, fill)
    }

    private fun mousePressedOnTrack(mouseEvent: MouseEvent) {
        showValuePopup()
    }

    private fun mouseDraggedOnTrack(mouseEvent: MouseEvent) {
        displaceValuePopup()
    }

    private fun mouseReleasedFromTrack(mouseEvent: MouseEvent) {
        hideValuePopup()
    }

    private fun mousePressedOnThumb(mouseEvent: MouseEvent) {
        showValuePopup()
    }

    private fun mouseDraggedOnThumb(mouseEvent: MouseEvent) {
        displaceValuePopup()
    }

    private fun mouseReleasedFromThumb(mouseEvent: MouseEvent) {
        hideValuePopup()
    }

    private fun showValuePopup() {
        if (!showValueOnInteraction) return

        popup.value = skinnable.value

        val thumbScreenPos = thumb.localToScreen(thumb.boundsInLocal.minX, thumb.boundsInLocal.minY)
        val orientation = skinnable.orientation

        if (orientation == Orientation.HORIZONTAL) {
            popup.show(thumb, thumbScreenPos.x + thumb.width / 2, thumbScreenPos.y - POPUP_DISTANCE_FROM_THUMB)
            popup.x = popup.x - popup.width / 2
        } else if (orientation == Orientation.VERTICAL) {
            popup.show(thumb, thumbScreenPos.x - POPUP_DISTANCE_FROM_THUMB, thumbScreenPos.y + thumb.height / 2)
            popup.y = popup.y - popup.height / 2
        }

        val fadeInTransition = FadeTransition(POPUP_FADE_DURATION, popup.scene.root)
        fadeInTransition.fromValue = 0.0
        fadeInTransition.toValue = 1.0
        fadeInTransition.play()
    }

    private fun displaceValuePopup() {
        if (!showValueOnInteraction) return

        if (popup.isShowing) {
            popup.value = skinnable.value
            val thumbScreenPos = thumb.localToScreen(thumb.boundsInLocal.minX, thumb.boundsInLocal.minY)
            val orientation = skinnable.orientation

            if (orientation == Orientation.HORIZONTAL) {
                popup.x = thumbScreenPos.x + thumb.width / 2 - popup.width / 2
            } else if (orientation == Orientation.VERTICAL) {
                popup.y = thumbScreenPos.y + thumb.height / 2 - popup.height / 2
            }
        }
    }

    private fun hideValuePopup() {
        if (!showValueOnInteraction) return

        val fadeOutTransition = FadeTransition(POPUP_FADE_DURATION, popup.scene.root)
        fadeOutTransition.fromValue = 1.0
        fadeOutTransition.toValue = 0.0
        fadeOutTransition.onFinished = EventHandler { popup.hide() }
        fadeOutTransition.play()
    }

    override fun layoutChildren(x: Double, y: Double, w: Double, h: Double) {
        super.layoutChildren(x, y, w, h)
        val control = skinnable
        val showTickMarks = control.isShowTickMarks || control.isShowTickLabels
        val thumbWidth = snapSizeX(thumb.prefWidth(-1.0))
        val thumbHeight = snapSizeY(thumb.prefHeight(-1.0))

        val trackRadius = if (track.background == null) 0.0
        else if (track.background.fills.size > 0) track.background.fills[0].radii.topLeftHorizontalRadius
        else 0.0

        val tickLine = control.lookup("NumberAxis") as NumberAxis

        if (skinnable.orientation == Orientation.HORIZONTAL) {
            val tickLineHeight: Double = if (showTickMarks) tickLine.prefHeight(-1.0) else 0.0
            val trackHeight = snapSizeY(track.prefHeight(-1.0))
            val trackAreaHeight = max(trackHeight, thumbHeight)
            val totalHeightNeeded = trackAreaHeight + if (showTickMarks) trackToTickGap + tickLineHeight else 0.0
            val startY = y + (h - totalHeightNeeded) / 2
            val trackStart = snapPositionX(x + thumbWidth / 2)
            val trackTop = (startY + (trackAreaHeight - trackHeight) / 2).toInt().toDouble()

            fill.resizeRelocate(
                (trackStart - trackRadius).toInt().toDouble(),
                trackTop,
                trackStart.toInt() - trackRadius + thumb.layoutX,
                trackHeight
            )
        } else {
            val tickLineWidth: Double = if (showTickMarks) tickLine.prefWidth(-1.0) else 0.0
            val trackWidth = snapSizeX(track.prefWidth(-1.0))
            val trackAreaWidth = max(trackWidth, thumbWidth)
            val totalWidthNeeded: Double = trackAreaWidth + if (showTickMarks) trackToTickGap + tickLineWidth else 0.0
            val startX = x + (w - totalWidthNeeded) / 2
            val trackLength = snapSizeY(h - thumbHeight)
            val trackStart = snapPositionY(y + thumbHeight / 2)
            val trackLeft = (startX + (trackAreaWidth - trackWidth) / 2).toInt().toDouble()

            fill.resizeRelocate(
                trackLeft,
                trackStart.toInt() - trackRadius + thumb.layoutY,
                trackWidth,
                trackLength - thumb.layoutY
            )
        }
    }

    val showValueOnInteractionProperty: BooleanProperty = SimpleStyleableBooleanProperty(SHOW_VALUE_ON_INTERACTION_META_DATA, true)

    private val showValueOnInteraction
        get() = showValueOnInteractionProperty.get()

    init {
        fill.styleClass.add("fill")

        children.add(children.indexOf(track) + 1, fill)
        track.addEventHandler(MouseEvent.MOUSE_PRESSED) { mousePressedOnTrack(it) }
        track.addEventHandler(MouseEvent.MOUSE_DRAGGED) { mouseDraggedOnTrack(it) }
        track.addEventHandler(MouseEvent.MOUSE_RELEASED) { mouseReleasedFromTrack(it) }

        fill.eventDispatcher = track.eventDispatcherProperty().get()

        thumb.addEventHandler(MouseEvent.MOUSE_PRESSED) { mousePressedOnThumb(it) }
        thumb.addEventHandler(MouseEvent.MOUSE_DRAGGED) { mouseDraggedOnThumb(it) }
        thumb.addEventHandler(MouseEvent.MOUSE_RELEASED) { mouseReleasedFromThumb(it) }
        registerChangeListener(slider.showTickMarksProperty()) { thickMarksChanged(it) }
        registerChangeListener(slider.showTickLabelsProperty()) { thickMarksChanged(it) }
    }

    override fun getCssMetaData() = STYLEABLES

    companion object {

        private const val POPUP_DISTANCE_FROM_THUMB = 50

        @JvmStatic private val POPUP_FADE_DURATION = Duration.millis(200.0)

        @JvmStatic private val SHOW_VALUE_ON_INTERACTION_META_DATA =

            object : CssMetaData<Slider, Boolean>("-show-value-on-interaction", BooleanConverter.getInstance(), true) {
                override fun isSettable(slider: Slider): Boolean {
                    val skin = slider.skin as SliderSkin
                    return !skin.showValueOnInteractionProperty.isBound
                }

                override fun getStyleableProperty(slider: Slider): StyleableProperty<Boolean> {
                    val skin = slider.skin as SliderSkin
                    return skin.showValueOnInteractionProperty as StyleableBooleanProperty
                }
            }

        @JvmStatic val STYLEABLES: List<CssMetaData<out Styleable?, *>>

        init {
            val styleables = ArrayList(getClassCssMetaData())
            styleables.add(SHOW_VALUE_ON_INTERACTION_META_DATA)
            STYLEABLES = Collections.unmodifiableList(styleables)
        }
    }
}
