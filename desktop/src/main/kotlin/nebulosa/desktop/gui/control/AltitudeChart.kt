package nebulosa.desktop.gui.control

import eu.hansolo.fx.charts.*
import eu.hansolo.fx.charts.data.XYChartItem
import eu.hansolo.fx.charts.data.XYItem
import eu.hansolo.fx.charts.series.XYSeriesBuilder
import javafx.geometry.Orientation
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color

class AltitudeChart : AnchorPane() {

    private val civilDawn = DoubleArray(2)
    private val nauticalDawn = DoubleArray(2)
    private val astronomicalDawn = DoubleArray(2)
    private val civilDusk = DoubleArray(2)
    private val nauticalDusk = DoubleArray(2)
    private val astronomicalDusk = DoubleArray(2)
    private val night = DoubleArray(2)

    private val altitudeSerie = XYSeriesBuilder.create()
        .chartType(ChartType.SMOOTH_LINE)
        .fill(Color.TRANSPARENT)
        .stroke(Color.web("#039BE5"))
        .symbolFill(Color.web("#01579B"))
        .symbolStroke(Color.TRANSPARENT)
        .symbolsVisible(true)
        .strokeWidth(3.0)
        .symbolSize(6.0)
        .build()

    private val civilDawnSerie = XYSeriesBuilder.create()
        .chartType(ChartType.SMOOTH_AREA)
        .fill(Color.web("#B39DDB60"))
        .symbolsVisible(false)
        .build()

    private val nauticalDawnSerie = XYSeriesBuilder.create()
        .chartType(ChartType.SMOOTH_AREA)
        .fill(Color.web("#3F51B560"))
        .symbolsVisible(false)
        .build()

    private val astronomicalDawnSerie = XYSeriesBuilder.create()
        .chartType(ChartType.SMOOTH_AREA)
        .fill(Color.web("#3949AB60"))
        .symbolsVisible(false)
        .build()

    private val nightSerie = XYSeriesBuilder.create()
        .chartType(ChartType.SMOOTH_AREA)
        .fill(Color.web("#0F154D60"))
        .symbolsVisible(false)
        .build()

    private val civilDuskSerie = XYSeriesBuilder.create()
        .chartType(ChartType.SMOOTH_AREA)
        .fill(Color.web("#B39DDB60"))
        .symbolsVisible(false)
        .build()

    private val nauticalDuskSerie = XYSeriesBuilder.create()
        .chartType(ChartType.SMOOTH_AREA)
        .fill(Color.web("#3F51B560"))
        .symbolsVisible(false)
        .build()

    private val astronomicalDuskSerie = XYSeriesBuilder.create()
        .chartType(ChartType.SMOOTH_AREA)
        .fill(Color.web("#3949AB60"))
        .symbolsVisible(false)
        .build()

    private val dayFirstSerie = XYSeriesBuilder.create()
        .chartType(ChartType.SMOOTH_AREA)
        .fill(Color.web("#FFF17660"))
        .symbolsVisible(false)
        .build()

    private val dayLastSerie = XYSeriesBuilder.create()
        .chartType(ChartType.SMOOTH_AREA)
        .fill(Color.web("#FFF17660"))
        .symbolsVisible(false)
        .build()

    private val xAxis = AxisBuilder.create(Orientation.HORIZONTAL, Position.BOTTOM)
        .type(AxisType.TEXT)
        .prefHeight(25.0)
        .categories(DEFAULT_CATEGORIES)
        .minValue(0.0)
        .maxValue(24.0)
        .autoScale(true)
        .axisColor(Color.web("#607D8B"))
        .tickLabelColor(Color.web("#607D8B"))
        .tickMarkColor(Color.web("#607D8B"))
        .tickMarksVisible(true)
        .majorTickMarksVisible(true)
        .mediumTimeAxisTickLabelsVisible(true)
        .sameTickMarkLength(true)
        .build()

    private val yAxis = AxisBuilder.create(Orientation.VERTICAL, Position.LEFT)
        .type(AxisType.LINEAR)
        .prefWidth(25.0)
        .minValue(0.0)
        .maxValue(90.0)
        .autoScale(true)
        .axisColor(Color.web("#607D8B"))
        .tickLabelColor(Color.web("#607D8B"))
        .tickMarkColor(Color.web("#607D8B"))
        .tickMarksVisible(true)
        .build()

    private val grid = GridBuilder.create(xAxis, yAxis)
        .gridLinePaint(Color.web("#CFD8DC"))
        .minorHGridLinesVisible(false)
        .mediumHGridLinesVisible(false)
        .minorVGridLinesVisible(false)
        .mediumVGridLinesVisible(false)
        .gridLineDashes(4.0, 4.0)
        .build()

    private val pane = XYPane(
        civilDawnSerie, nauticalDawnSerie, astronomicalDawnSerie,
        nightSerie,
        civilDuskSerie, nauticalDuskSerie, astronomicalDuskSerie,
        dayFirstSerie, dayLastSerie,
        altitudeSerie,
    )

    private val chart = XYChart(pane, grid, xAxis, yAxis)

    private val points = arrayListOf<XYItem>()
    private var now = 0.0

    init {
        setBottomAnchor(xAxis, 0.0)
        setLeftAnchor(xAxis, 25.0)
        setRightAnchor(xAxis, 25.0)

        setTopAnchor(yAxis, 0.0)
        setBottomAnchor(yAxis, 25.0)
        setLeftAnchor(yAxis, 0.0)

        setRightAnchor(grid, 25.0)
        setLeftAnchor(grid, 25.0)
        setBottomAnchor(grid, 25.0)
        setTopAnchor(grid, 0.0)

        setRightAnchor(pane, 0.0)
        setLeftAnchor(pane, 25.0)
        setBottomAnchor(pane, 25.0)
        setTopAnchor(pane, 0.0)

        setRightAnchor(chart, 0.0)
        setLeftAnchor(chart, 0.0)
        setBottomAnchor(chart, 0.0)
        setTopAnchor(chart, 0.0)

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
        }

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

        altitudeSerie.items.setAll(points)
    }

    companion object {

        @JvmStatic private val DEFAULT_CATEGORIES = listOf(
            "12h", "13h", "14h", "15h", "16h", "17h", "18h", "19h", "20h", "21h", "22h", "23h",
            "0h", "1h", "2h", "3h", "4h", "5h", "6h", "7h", "8h", "9h", "10h", "11h", "12h",
        )
    }
}
