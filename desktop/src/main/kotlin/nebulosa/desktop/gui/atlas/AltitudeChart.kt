package nebulosa.desktop.gui.atlas

import eu.hansolo.fx.charts.*
import eu.hansolo.fx.charts.data.XYChartItem
import eu.hansolo.fx.charts.data.XYItem
import eu.hansolo.fx.charts.series.XYSeriesBuilder
import javafx.geometry.Orientation
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import javafx.util.StringConverter
import nebulosa.desktop.helper.anchor

class AltitudeChart : AnchorPane() {

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
    private var now = 0.0

    init {
        xAxis.anchor(null, 16.0, 0.0, 24.0)
        yAxis.anchor(8.0, null, 16.0, 0.0)
        grid.anchor(8.0, 16.0, 16.0, 24.0)
        pane.anchor(8.0, 16.0, 16.0, 24.0)
        chart.anchor(0.0, 0.0, 0.0, 0.0)

        children.add(chart)
    }

    fun draw(
        points: List<XYItem> = this.points,
        now: Double = this.now,
        civilDawn: DoubleArray = this.civilDawn,
        nauticalDawn: DoubleArray = this.nauticalDawn,
        astronomicalDawn: DoubleArray = this.astronomicalDawn,
        civilDusk: DoubleArray = this.civilDusk,
        nauticalDusk: DoubleArray = this.nauticalDusk,
        astronomicalDusk: DoubleArray = this.astronomicalDusk,
        night: DoubleArray = this.night,
    ) {
        if (points !== this.points) {
            this.points.clear()
            this.points.addAll(points)
            altitudeSerie.items.setAll(points)
        }

        civilDawn.copyInto(this.civilDawn)
        nauticalDawn.copyInto(this.nauticalDawn)
        astronomicalDawn.copyInto(this.astronomicalDawn)
        civilDusk.copyInto(this.civilDusk)
        nauticalDusk.copyInto(this.nauticalDusk)
        astronomicalDusk.copyInto(this.astronomicalDusk)
        night.copyInto(this.night)
        this.now = now

        dayFirstSerie.items.setAll(XYChartItem(0.0, 90.0), XYChartItem(civilDusk[0], 90.0))
        dayLastSerie.items.setAll(XYChartItem(civilDawn[1], 90.0), XYChartItem(24.0, 90.0))
        nightSerie.items.setAll(XYChartItem(night[0], 90.0), XYChartItem(night[1], 90.0))
        nowSerie.items.setAll(XYChartItem(now - 2.0 / 60.0, 90.0), XYChartItem(now + 2.0 / 60.0, 90.0))

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
