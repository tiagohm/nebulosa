package nebulosa.desktop.gui.atlas

import eu.hansolo.fx.charts.*
import eu.hansolo.fx.charts.data.XYChartItem
import eu.hansolo.fx.charts.data.XYItem
import eu.hansolo.fx.charts.series.XYSeriesBuilder
import javafx.geometry.Orientation
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import javafx.util.StringConverter
import nebulosa.desktop.helper.anchor
import nebulosa.desktop.view.atlas.DateTimeProvider
import nebulosa.math.pmod
import java.time.LocalTime

class AltitudeChart : AnchorPane() {

    fun interface NowListener {

        fun onNowChanged(time: LocalTime, manual: Boolean)
    }

    private val civilDawn = DoubleArray(2)
    private val nauticalDawn = DoubleArray(2)
    private val astronomicalDawn = DoubleArray(2)
    private val civilDusk = DoubleArray(2)
    private val nauticalDusk = DoubleArray(2)
    private val astronomicalDusk = DoubleArray(2)
    private val night = DoubleArray(2)

    private val altitudeSerie = XYSeriesBuilder.create()
        .chartType(ChartType.LINE)
        .fill(Color.TRANSPARENT)
        .stroke(Color.web("#039BE5"))
        .symbolFill(Color.web("#01579B"))
        .symbolStroke(Color.TRANSPARENT)
        .symbolsVisible(true)
        .strokeWidth(3.0)
        .symbolSize(6.0)
        .build()

    private val civilDawnSerie = XYSeriesBuilder.create()
        .chartType(ChartType.AREA)
        .fill(Color.web("#B39DDB60"))
        .symbolsVisible(false)
        .build()

    private val nauticalDawnSerie = XYSeriesBuilder.create()
        .chartType(ChartType.AREA)
        .fill(Color.web("#3F51B560"))
        .symbolsVisible(false)
        .build()

    private val astronomicalDawnSerie = XYSeriesBuilder.create()
        .chartType(ChartType.AREA)
        .fill(Color.web("#3949AB60"))
        .symbolsVisible(false)
        .build()

    private val nightSerie = XYSeriesBuilder.create()
        .chartType(ChartType.AREA)
        .fill(Color.web("#0F154D60"))
        .symbolsVisible(false)
        .build()

    private val civilDuskSerie = XYSeriesBuilder.create()
        .chartType(ChartType.AREA)
        .fill(Color.web("#B39DDB60"))
        .symbolsVisible(false)
        .build()

    private val nauticalDuskSerie = XYSeriesBuilder.create()
        .chartType(ChartType.AREA)
        .fill(Color.web("#3F51B560"))
        .symbolsVisible(false)
        .build()

    private val astronomicalDuskSerie = XYSeriesBuilder.create()
        .chartType(ChartType.AREA)
        .fill(Color.web("#3949AB60"))
        .symbolsVisible(false)
        .build()

    private val dayFirstSerie = XYSeriesBuilder.create()
        .chartType(ChartType.AREA)
        .fill(Color.web("#FFF17660"))
        .symbolsVisible(false)
        .build()

    private val dayLastSerie = XYSeriesBuilder.create()
        .chartType(ChartType.AREA)
        .fill(Color.web("#FFF17660"))
        .symbolsVisible(false)
        .build()

    private val nowSerie = XYSeriesBuilder.create()
        .chartType(ChartType.AREA)
        .fill(Color.web("#F4433660"))
        .symbolsVisible(false)
        .build()

    private val xAxis = AxisBuilder.create(Orientation.HORIZONTAL, Position.BOTTOM)
        .type(AxisType.LINEAR)
        .prefHeight(16.0)
        .minValue(0.0)
        .maxValue(24.0)
        .autoScale(false)
        .numberFormatter(YAxisStringConverter)
        .axisColor(Color.web("#B0BEC5"))
        .tickLabelColor(Color.web("#B0BEC5"))
        .tickMarkColor(Color.web("#B0BEC5"))
        .tickMarksVisible(true)
        .majorTickMarksVisible(true)
        .mediumTickMarksVisible(true)
        .build()

    private val yAxis = AxisBuilder.create(Orientation.VERTICAL, Position.LEFT)
        .type(AxisType.LINEAR)
        .prefWidth(24.0)
        .minValue(0.0)
        .maxValue(90.0)
        .autoScale(true)
        .axisColor(Color.web("#B0BEC5"))
        .tickLabelColor(Color.web("#B0BEC5"))
        .tickMarkColor(Color.web("#B0BEC5"))
        .tickMarksVisible(true)
        .build()

    private val grid = GridBuilder.create(xAxis, yAxis)
        .gridLinePaint(Color.web("#CFD8DC80"))
        .minorHGridLinesVisible(false)
        .mediumHGridLinesVisible(false)
        .majorHGridLinesVisible(true)
        .minorVGridLinesVisible(true)
        .mediumVGridLinesVisible(true)
        .majorVGridLinesVisible(true)
        .gridLineDashes(4.0, 4.0)
        .build()

    private val pane = XYPane(
        civilDawnSerie, nauticalDawnSerie, astronomicalDawnSerie,
        nightSerie,
        civilDuskSerie, nauticalDuskSerie, astronomicalDuskSerie,
        dayFirstSerie, dayLastSerie,
        altitudeSerie,
        nowSerie,
    )

    private val chart = XYChart(pane, grid, xAxis, yAxis)
    private val points = arrayListOf<XYItem>()
    private val nowListeners = hashSetOf<NowListener>()

    var manualMode = false
        private set

    var dateTimeProvider: DateTimeProvider = DateTimeProvider.Utc

    init {
        xAxis.anchor(null, 16.0, 0.0, 24.0)
        yAxis.anchor(8.0, null, 16.0, 0.0)
        grid.anchor(8.0, 16.0, 16.0, 24.0)
        pane.anchor(8.0, 16.0, 16.0, 24.0)
        chart.anchor(0.0, 0.0, 0.0, 0.0)

        children.add(chart)

        chart.addEventHandler(ScrollEvent.SCROLL) {
            val delta = if (it.deltaY == 0.0 && it.deltaX != 0.0) it.deltaX else it.deltaY
            if (delta == 0.0) return@addEventHandler
            val stepSize = if (it.isShiftDown) 60L else if (it.isControlDown) 30L else 1L
            val amount = if (delta < 0.0) -stepSize else stepSize
            drawNow(amount)
        }

        chart.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            if (it.button == MouseButton.PRIMARY && it.clickCount == 2) {
                drawNow(reset = true)
            }
        }
    }

    fun registerNowListener(listener: NowListener) {
        nowListeners.add(listener)
    }

    fun unregisterNowListener(listener: NowListener) {
        nowListeners.remove(listener)
    }

    fun drawPoints(points: List<XYItem> = this.points) {
        if (points !== this.points) {
            this.points.clear()
            this.points.addAll(points)
            altitudeSerie.items.setAll(points)
        }
    }

    fun drawNow(amount: Long = 0L, reset: Boolean = false) {
        val now: LocalTime

        if (amount != 0L) {
            manualMode = true
            now = dateTimeProvider.time.plusMinutes(amount)
            nowListeners.forEach { it.onNowChanged(now, true) }
        } else if (reset || !manualMode) {
            dateTimeProvider.resetTime()
            now = dateTimeProvider.time
            nowListeners.forEach { it.onNowChanged(now, manualMode) }
            if (reset) manualMode = false
        } else {
            now = dateTimeProvider.time
        }

        val x = (now.toSecondOfDay() / 3600.0 - 12.0) pmod 24.0
        nowSerie.items.setAll(XYChartItem(x - 4.0 / 60.0, 90.0), XYChartItem(x + 4.0 / 60.0, 90.0))
    }

    fun drawTwilight(
        civilDawn: DoubleArray = this.civilDawn,
        nauticalDawn: DoubleArray = this.nauticalDawn,
        astronomicalDawn: DoubleArray = this.astronomicalDawn,
        civilDusk: DoubleArray = this.civilDusk,
        nauticalDusk: DoubleArray = this.nauticalDusk,
        astronomicalDusk: DoubleArray = this.astronomicalDusk,
        night: DoubleArray = this.night,
    ) {
        civilDawn.copyInto(this.civilDawn)
        nauticalDawn.copyInto(this.nauticalDawn)
        astronomicalDawn.copyInto(this.astronomicalDawn)
        civilDusk.copyInto(this.civilDusk)
        nauticalDusk.copyInto(this.nauticalDusk)
        astronomicalDusk.copyInto(this.astronomicalDusk)
        night.copyInto(this.night)

        dayFirstSerie.items.setAll(XYChartItem(0.0, 90.0), XYChartItem(civilDusk[0], 90.0))
        dayLastSerie.items.setAll(XYChartItem(civilDawn[1], 90.0), XYChartItem(24.0, 90.0))
        nightSerie.items.setAll(XYChartItem(night[0], 90.0), XYChartItem(night[1], 90.0))

        civilDawnSerie.items.setAll(XYChartItem(civilDawn[0], 90.0), XYChartItem(civilDawn[1], 90.0))
        nauticalDawnSerie.items.setAll(XYChartItem(nauticalDawn[0], 90.0), XYChartItem(nauticalDawn[1], 90.0))
        astronomicalDawnSerie.items.setAll(XYChartItem(astronomicalDawn[0], 90.0), XYChartItem(astronomicalDawn[1], 90.0))
        astronomicalDuskSerie.items.setAll(XYChartItem(astronomicalDusk[0], 90.0), XYChartItem(astronomicalDusk[1], 90.0))
        nauticalDuskSerie.items.setAll(XYChartItem(nauticalDusk[0], 90.0), XYChartItem(nauticalDusk[1], 90.0))
        civilDuskSerie.items.setAll(XYChartItem(civilDusk[0], 90.0), XYChartItem(civilDusk[1], 90.0))
    }

    private object YAxisStringConverter : StringConverter<Number>() {

        override fun toString(number: Number) = "%dh".format((number.toInt() + 12) % 24)

        override fun fromString(string: String?) = null
    }
}
