import { AfterViewInit, Component, ElementRef, Input, OnChanges, SimpleChanges, ViewChild } from '@angular/core'

@Component({
	selector: 'neb-moon',
	templateUrl: './moon.component.html',
	styleUrls: ['./moon.component.scss'],
})
export class MoonComponent implements AfterViewInit, OnChanges {
	@ViewChild('moon')
	private readonly moon!: ElementRef<HTMLCanvasElement>

	@Input()
	height = 256

	@Input()
	width = 256

	@Input()
	illuminationRatio = 0

	@Input()
	waning = false

	ngAfterViewInit() {
		this.draw()
	}

	ngOnChanges(changes: SimpleChanges) {
		this.draw()
	}

	// Adapted from https://codepen.io/ardathksheyna/pen/adMyXx.
	private draw() {
		const canvas = this.moon?.nativeElement

		if (!canvas) return

		const ctx = canvas.getContext('2d')!

		ctx.clearRect(0, 0, canvas.width, canvas.height)

		const offset = 32
		const offset4 = offset / 4

		const height = canvas.height - offset
		const width = canvas.width - offset

		canvas.style.backgroundImage = `url('assets/images/moon.png')`
		canvas.style.backgroundSize = `${height + offset4 * 2 - 2}px`

		const cx = width / 2 + offset4
		const cy = height / 2 + offset4

		const pointsA: [number, number][] = []
		const pointsB: [number, number][] = []

		for (let a = 0; a < 180; a++) {
			const angle = ((a - 90) * Math.PI) / 180
			let x1 = Math.ceil(Math.cos(angle) * cx)
			const y1 = Math.ceil(Math.sin(angle) * cy)
			const moonWidth = x1 * 2
			let x2 = Math.floor(moonWidth * this.illuminationRatio)

			if (this.waning) {
				x1 = cx + x1
				x2 = x1 - (moonWidth - x2)
			} else {
				x1 = cx - x1
				x2 = x1 + (moonWidth - x2)
			}

			const y2 = cy + y1
			const p1: [number, number] = [x1 + offset4, y2 + offset4]
			const p2: [number, number] = [x2 + offset4, y2 + offset4]

			pointsA.push(p1)
			pointsB.push(p2)
		}

		const newPoints = pointsA.concat(pointsB.reverse())
		ctx.beginPath()

		ctx.fillStyle = '#121212D8'
		ctx.filter = 'blur(1px)'

		let first = true

		for (const p of newPoints) {
			if (first) {
				first = false
				ctx.moveTo(p[0], p[1])
			} else {
				ctx.lineTo(p[0], p[1])
			}
		}

		ctx.fill()
		ctx.closePath()
	}
}
