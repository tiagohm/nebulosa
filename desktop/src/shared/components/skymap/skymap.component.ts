import { AfterViewInit, Component, ElementRef, ViewChild } from '@angular/core'
import * as d3 from 'd3'

// https://observablehq.com/@d3/star-map
// https://armchairastronautics.blogspot.com/p/skymap.html
// https://github.com/ofrohn/d3-celestial/blob/master/src/celestial.js
// https://observablehq.com/@fil/galactic-rotations

@Component({
    selector: 'p-skymap',
    template: `
    <div #container></div>
  `,
    styles: ``
})
export class SkyMapComponent implements AfterViewInit {

    @ViewChild('container')
    private readonly container!: ElementRef<HTMLDivElement>

    ngAfterViewInit() {
        const width = 954 + 28
        const height = width
        const cx = width / 2
        const cy = height / 2
        const radius = d3.scaleLinear([6, -1], [0, 8])
        const outline = d3.geoCircle().radius(90).center([0, 90])()
        const graticule = d3.geoGraticule().stepMinor([15, 10])()

        const data: d3.Delaunay.Point[] = []

        for (let i = 0; i < 2000; i++) {
            const ra = Math.random() * 360.0
            const dec = Math.random() * 180.0 - 90.0
            data.push([ra, dec])
        }

        const projection = d3.geoStereographic()
            .reflectY(true)
            .scale(1000.0)
            .clipExtent([[0, 0], [width, height]])
            .rotate([0, -90])
            .center([0.7 * 15, 45])
            .translate([width / 2, height / 2])
            .precision(0.1)

        const path = d3.geoPath(projection)

        const voronoi = d3.Delaunay
            .from(data.map(e => projection(e)!))
            .voronoi([0, 0, width, height])

        const svg = d3.create("svg")
            .attr("width", width)
            .attr("height", height)
            .attr("viewBox", [0, 0, width, height])
            .attr("style", "display: block; margin: 0 -14px; width: auto; height: 550px; font: 10px sans-serif; color: white; background: radial-gradient(#081f4b 0%, #061616 100%);")
            .attr("text-anchor", "middle")
            .attr("fill", "currentColor")

        svg.append("path")
            .attr("d", path(graticule))
            .attr("fill", "none")
            .attr("stroke", "currentColor")
            .attr("stroke-opacity", 0.2)

        svg.append("path")
            .attr("d", path(outline))
            .attr("fill", "none")
            .attr("stroke", "currentColor")

        // 5-minute ticks
        svg.append("g")
            .attr("stroke", "currentColor")
            .selectAll()
            .data(d3.range(0, 1440, 5)) // every 5 minutes
            .join("line")
            .datum(d => [
                projection([d / 4, 0])!,
                projection([d / 4, d % 60 ? -1 : -2])!
            ])
            .attr("x1", (e) => e[0][0])
            .attr("x2", (e) => e[1][0])
            .attr("y1", (e) => e[0][1])
            .attr("y2", (e) => e[1][1])

        // hourly ticks and labels
        svg.append("g")
            .selectAll()
            .data(d3.range(0, 1440, 60)) // every hour
            .join("text")
            .attr("dy", "0.35em")
            .text(d => `${d / 60}h`)
            .attr("font-size", d => d % 360 ? null : 14)
            .attr("font-weight", d => d % 360 ? null : "bold")
            .datum(d => projection([d / 4, -4]))
            .attr("x", (e) => e![0])
            .attr("y", (e) => e![1])

        // 10° labels
        svg.append("g")
            .selectAll()
            .data(d3.range(-90, 90, 10))
            .join("text")
            .attr("dy", "0.35em")
            .text(d => `${d}°`)
            .datum(d => projection([0, d]))
            .attr("x", (e) => e![0])
            .attr("y", (e) => e![1])

        const focusDeclination = svg.append("circle")
            .attr("cx", cx)
            .attr("cy", cy)
            .attr("fill", "none")
            .attr("stroke", "yellow")

        const focusRightAscension = svg.append("line")
            .attr("x1", cx)
            .attr("y1", cy)
            .attr("x2", cx)
            .attr("y2", cy)
            .attr("stroke", "yellow")

        svg.append("g")
            .attr("stroke", "black")
            .selectAll()
            .data(data)
            .join("circle")
            // .attr("r", d => radius(d.magnitude))
            .attr("r", d => 3)
            .attr("transform", d => `translate(${projection(d)})`)

        svg.append("g")
            .attr("pointer-events", "all")
            .attr("fill", "none")
            .selectAll()
            .data(data)
            .join("path")
            // .on("mouseover", mouseovered)
            // .on("mouseout", mouseouted)
            .attr("d", (d, i) => voronoi.renderCell(i))
            .append("title")
            .text('kkkkkkk')

        this.container.nativeElement.append(svg.node()!)
    }
}
