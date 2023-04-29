package nebulosa.desktop.gui.guider

import eu.hansolo.fx.charts.*
import eu.hansolo.fx.charts.data.XYChartItem
import eu.hansolo.fx.charts.data.XYItem
import eu.hansolo.fx.charts.series.XYSeriesBuilder
import javafx.geometry.Orientation
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import nebulosa.desktop.helper.anchor
import nebulosa.guiding.GuideStats

class GuiderChart : AnchorPane() {

    private val raSerieItems = ArrayList<XYItem>(100)
    private val decSerieItems = ArrayList<XYItem>(100)
    private val raDurationSerieItems = ArrayList<XYItem>(100)
    private val decDurationSerieItems = ArrayList<XYItem>(100)
    private var scale = 1.0

    private val raSerie = XYSeriesBuilder.create()
        .chartType(ChartType.LINE)
        .fill(Color.TRANSPARENT)
        .stroke(Color.web("#039BE5"))
        .symbolsVisible(false)
        .strokeWidth(1.0)
        .build()

    private val raDurationSerie = XYSeriesBuilder.create()
        .chartType(ChartType.BAR)
        .fill(Color.web("#039BE540"))
        .stroke(Color.web("#039BE540"))
        .symbolsVisible(false)
        .strokeWidth(1.0)
        .build()

    private val decSerie = XYSeriesBuilder.create()
        .chartType(ChartType.LINE)
        .fill(Color.TRANSPARENT)
        .stroke(Color.web("#43A047"))
        .symbolsVisible(false)
        .strokeWidth(1.0)
        .build()

    private val decDurationSerie = XYSeriesBuilder.create()
        .chartType(ChartType.BAR)
        .fill(Color.web("#43A04740"))
        .stroke(Color.web("#43A04740"))
        .symbolsVisible(false)
        .strokeWidth(1.0)
        .build()

    private val xAxis = AxisBuilder.create(Orientation.HORIZONTAL, Position.BOTTOM)
        .type(AxisType.LINEAR)
        .prefHeight(0.0)
        .minValue(0.0)
        .maxValue(100.0) // status size is always 100.
        .autoScale(true)
        .axisColor(Color.web("#607D8B50"))
        .tickLabelsVisible(false)
        .tickMarksVisible(false)
        .build()

    private val yAxis = AxisBuilder.create(Orientation.VERTICAL, Position.LEFT)
        .type(AxisType.LINEAR)
        .prefWidth(0.0)
        .minValue(-1.0)
        .maxValue(1.0)
        .autoScale(true)
        .axisColor(Color.web("#607D8B50"))
        .tickLabelsVisible(false)
        .tickMarksVisible(false)
        .build()

    private val grid = GridBuilder.create(xAxis, yAxis)
        .gridLinePaint(Color.web("#CFD8DC50"))
        .minorHGridLinesVisible(false)
        .mediumHGridLinesVisible(false)
        .minorVGridLinesVisible(false)
        .mediumVGridLinesVisible(false)
        .gridLineDashes(4.0, 4.0)
        .build()

    private val pane = XYPane(raDurationSerie, decDurationSerie, raSerie, decSerie)

    private val chart = XYChart(pane, grid, xAxis, yAxis)

    init {
        xAxis.anchor(0.0, 0.0, 0.0, 0.0)
        yAxis.anchor(0.0, 0.0, 0.0, 0.0)
        grid.anchor(0.0, 0.0, 0.0, 0.0)
        pane.anchor(0.0, 0.0, 0.0, 0.0)
        chart.anchor(0.0, 0.0, 0.0, 0.0)

        children.add(chart)
    }

    fun draw(
        stats: List<GuideStats>,
        maxRADuration: Double, maxDECDuration: Double,
    ) {
        if (stats.size > raSerieItems.size) {
            val x = raSerieItems.size.toDouble()
            raSerieItems.add(XYChartItem(x, 0.0))
            decSerieItems.add(XYChartItem(x, 0.0))
            raDurationSerieItems.add(XYChartItem(x, 0.0))
            decDurationSerieItems.add(XYChartItem(x, 0.0))
        }

        for (i in stats.indices) {
            raSerieItems[i].y = stats[i].ra / scale
            decSerieItems[i].y = stats[i].dec / scale
            raDurationSerieItems[i].y = (stats[i].raDuration / maxRADuration) / scale
            decDurationSerieItems[i].y = (stats[i].decDuration / maxDECDuration) / scale
        }

        raSerie.items.setAll(raSerieItems)
        decSerie.items.setAll(decSerieItems)
        raDurationSerie.items.setAll(raDurationSerieItems)
        decDurationSerie.items.setAll(decDurationSerieItems)
    }

    fun changeScale(scale: Double) {
        val factor = this.scale / scale
        this.scale = scale

        for (i in raSerieItems.indices) {
            raSerieItems[i].y *= factor
            decSerieItems[i].y *= factor
            raDurationSerieItems[i].y *= factor
            decDurationSerieItems[i].y *= factor
        }

        raSerie.items.setAll(raSerieItems)
        decSerie.items.setAll(decSerieItems)
        raDurationSerie.items.setAll(raDurationSerieItems)
        decDurationSerie.items.setAll(decDurationSerieItems)
    }
}
