import { Component, ElementRef, ViewEncapsulation, effect, viewChild } from '@angular/core'
import { ImageHistrogram } from '../types/image.types'

@Component({
	standalone: false,
	selector: 'neb-histogram',
	template: `
		<canvas
			#canvas
			class="w-full h-full"></canvas>
	`,
	encapsulation: ViewEncapsulation.None,
})
export class HistogramComponent {
	private readonly canvas = viewChild.required<ElementRef<HTMLCanvasElement>>('canvas')

	private ctx?: CanvasRenderingContext2D | null

	constructor() {
		effect(() => {
			this.ctx = this.canvas().nativeElement.getContext('2d')
		})
	}

	update(data: ImageHistrogram, dontClear: boolean = false) {
		const canvas = this.canvas().nativeElement

		if (!dontClear || !data.length) {
			this.ctx?.clearRect(0, 0, canvas.width, canvas.height)
		}

		if (!data.length) {
			return
		}

		const max = data.reduce((a, b) => Math.max(a, b))
		const start = data.findIndex((e) => e != 0)
		const end = data.findLastIndex((e) => e != 0)

		this.drawColorGraph(data, max, start, end, '#FFF')
	}

	private drawColorGraph(data: ImageHistrogram, max: number, start: number = 0, end: number = data.length - 1, color: string | CanvasGradient | CanvasPattern) {
		if (this.ctx) {
			const canvas = this.canvas().nativeElement

			const graphHeight = canvas.height
			const graphWidth = canvas.width
			const graphX = 0
			const graphY = canvas.height

			this.ctx.fillStyle = color
			this.ctx.beginPath()
			this.ctx.moveTo(graphX, graphHeight)

			const length = end - start + 1

			for (let i = 0; i < length; i++) {
				const value = data[start + i]
				const drawHeight = Math.round((value / max) * graphHeight)
				const drawX = graphX + (graphWidth / length) * i
				this.ctx.lineTo(drawX, graphY - drawHeight)
			}

			this.ctx.lineTo(graphX + graphWidth, graphY)
			this.ctx.closePath()
			this.ctx.fill()
		}
	}
}
