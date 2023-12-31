import { AfterContentInit, AfterViewInit, Component, ElementRef, ViewChild } from '@angular/core'

@Component({
    selector: 'p-histogram',
    templateUrl: './histogram.component.html',
    styleUrls: ['./histogram.component.scss'],
})
export class HistogramComponent implements AfterViewInit {

    @ViewChild('canvas')
    private readonly canvas!: ElementRef<HTMLCanvasElement>

    private ctx!: CanvasRenderingContext2D

    ngAfterViewInit() {
        this.ctx = this.canvas.nativeElement.getContext('2d')!
    }

    get data() {
        return []
    }

    update(data: number[], dontClear: boolean = false) {
        const canvas = this.canvas.nativeElement

        if (!dontClear) {
            this.ctx.clearRect(0, 0, canvas.width, canvas.height)
        }

        const max = data.reduce((a, b) => Math.max(a, b))

        this.drawColorGraph(max, data, '#FFF')
    }

    private drawColorGraph(max: number, data: number[], color: string | CanvasGradient | CanvasPattern) {
        const canvas = this.canvas.nativeElement

        const graphHeight = canvas.height
        const graphWidth = canvas.width
        const graphX = 0
        const graphY = canvas.height

        this.ctx.fillStyle = color
        this.ctx.beginPath()
        this.ctx.moveTo(graphX, graphHeight)

        for (let i = 0; i < data.length; i++) {
            const value = data[i]
            const drawHeight = Math.round((value / max) * graphHeight)
            const drawX = graphX + (graphWidth / (data.length - 1)) * i
            this.ctx.lineTo(drawX, graphY - drawHeight)
        }

        this.ctx.lineTo(graphX + graphWidth, graphY)
        this.ctx.closePath()
        this.ctx.fill()

    }
}