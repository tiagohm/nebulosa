import { AfterViewInit, Component, ElementRef, Input, OnChanges, ViewChild, ViewEncapsulation } from '@angular/core'

@Component({
	selector: 'neb-moon',
	templateUrl: './moon.component.html',
	encapsulation: ViewEncapsulation.None,
})
export class MoonComponent implements AfterViewInit, OnChanges {
	@Input()
	protected height = 256

	@Input()
	protected width = 256

	@Input()
	protected illuminationRatio = 0

	@Input()
	protected waning = false

	@ViewChild('moon')
	private readonly moon?: ElementRef<HTMLCanvasElement>

	ngAfterViewInit() {
		this.draw()
	}

	ngOnChanges() {
		this.draw()
	}

	// Adapted from https://codepen.io/ardathksheyna/pen/adMyXx.
	private draw() {
		const canvas = this.moon?.nativeElement
		if (!canvas) return
		const ctx = canvas.getContext('2d')!

		ctx.clearRect(0, 0, canvas.width, canvas.height)

		const height = canvas.height
		const width = canvas.width

		canvas.style.backgroundImage = `url('assets/images/moon.png')`
		canvas.style.backgroundSize = `${height - 2}px`

		const cx = width / 2
		const cy = height / 2

		const pointsA: [number, number][] = []
		const pointsB: [number, number][] = []

		for (let a = 0; a < 180; a++) {
			const angle = ((a - 90) * Math.PI) / 180
			let x1 = Math.ceil(Math.cos(angle) * cx)
			const y1 = Math.ceil(Math.sin(angle) * cy)
			const w = x1 * 2
			let x2 = Math.floor(w * this.illuminationRatio)

			if (this.waning) {
				x1 = cx + x1
				x2 = x1 - (w - x2)
			} else {
				x1 = cx - x1
				x2 = x1 + (w - x2)
			}

			const y2 = cy + y1
			const p1: [number, number] = [x1, y2]
			const p2: [number, number] = [x2, y2]

			pointsA.push(p1)
			pointsB.push(p2)
		}

		const newPoints = pointsA.concat(pointsB.reverse())
		ctx.beginPath()

		ctx.fillStyle = '#121212E8'
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
