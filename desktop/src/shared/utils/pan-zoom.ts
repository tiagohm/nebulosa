// Adapted from https://github.com/timmywil/panzoom

import type { Point } from 'electron'
import { EventEmitter } from 'events'

export type PanZoomEvent = 'panzoomstart' | 'panzoomchange' | 'panzoompan' | 'panzoomzoom' | 'panzoomreset' | 'panzoomend'

export type PanZoomOriginalEvent = PointerEvent | TouchEvent | MouseEvent

export interface PanZoomTransformation extends Point {
	scale: number
}

export interface PanZoomEventDetail {
	transformation: PanZoomTransformation
	originalEvent?: PanZoomOriginalEvent
}

interface PanZoomMiscOptions {
	canExclude: (element: Element) => boolean
	force: boolean
	handleStartEvent: (event: Event) => void
	noBind: boolean
	setTransform: typeof setTransform
	silent: boolean
	startX: number
	startY: number
	startScale: number
	startAngle: number
	[key: string]: unknown
}

interface PanZoomPanOnlyOptions {
	contain?: 'inside' | 'outside'
	disablePan: boolean
	disableXAxis: boolean
	disableYAxis: boolean
	relative: boolean
	panOnlyWhenZoomed: boolean
	roundPixels: boolean
}

interface PanZoomZoomOnlyOptions {
	disableZoom: boolean
	focal?: Point
	minScale: number
	maxScale: number
	step: number
}

export type PanZoomPanOptions = PanZoomMiscOptions & PanZoomPanOnlyOptions
export type PanZoomZoomOptions = PanZoomMiscOptions & PanZoomZoomOnlyOptions
export type PanZoomOptions = PanZoomPanOptions & PanZoomZoomOptions & PanZoomMiscOptions

export function setTransform(elem: HTMLElement | SVGElement, { x, y, scale }: PanZoomTransformation) {
	elem.style.transform = `scale(${scale}) translate(${x}px, ${y}px)`
}

export const DEFAULT_OPTIONS: PanZoomOptions = {
	disablePan: false,
	disableZoom: false,
	disableXAxis: false,
	disableYAxis: false,
	canExclude: () => false,
	handleStartEvent: (e) => {
		e.preventDefault()
		e.stopPropagation()
	},
	maxScale: 4,
	minScale: 0.125,
	panOnlyWhenZoomed: false,
	relative: false,
	setTransform,
	startX: 0,
	startY: 0,
	startScale: 1,
	startAngle: 0,
	step: 0.3,
	roundPixels: false,
	force: false,
	noBind: false,
	silent: false,
}

export class PanZoom extends EventEmitter {
	private readonly options = DEFAULT_OPTIONS

	private x = 0
	private y = 0
	private scale = 1
	private isPanning = false
	private origX?: number
	private origY?: number
	private startClientX?: number
	private startClientY?: number
	private bound = false

	constructor(
		private readonly element: HTMLElement,
		options?: Omit<Partial<PanZoomOptions>, 'force'>,
		private readonly wrapper: HTMLElement = element.parentElement!,
	) {
		super()

		if (options) {
			for (const key in this.options) {
				if (key in options) {
					this.options[key] = options[key]
				}
			}
		}

		if (!this.options.noBind) {
			this.bind()
		}

		this.zoom(this.options.startScale, { force: true })

		setTimeout(() => {
			this.pan(this.options.startX, this.options.startY, { force: true })
		})
	}

	get transformation(): PanZoomTransformation {
		return { x: this.x, y: this.y, scale: this.scale }
	}

	bind() {
		if (this.bound) {
			return
		}

		this.bound = true

		this.element.addEventListener('pointerdown', this.handleDown)
		document.addEventListener('pointermove', this.handleMove, { passive: true })
		document.addEventListener('pointerup', this.handleUp, { passive: true })
	}

	unbind() {
		if (!this.bound) {
			return
		}

		this.bound = false

		this.element.removeEventListener('pointerdown', this.handleDown)
		document.removeEventListener('pointermove', this.handleMove)
		document.removeEventListener('pointerup', this.handleUp)
	}

	zoom(scale: number, zoomOptions?: Partial<PanZoomZoomOptions>, originalEvent?: PanZoomOriginalEvent) {
		const result = this.constrainScale(scale, zoomOptions)
		const opts = result.opts

		if (!opts.force && opts.disableZoom) {
			return
		}

		if (result.scale === this.scale) {
			return
		}

		// https://github.com/timmywil/panzoom/pull/669
		const focalScale = scale
		scale = result.scale
		let toX = this.x
		let toY = this.y

		// https://github.com/timmywil/panzoom/pull/652
		// Adjust the pointer starting point to ensure that the current pointer panning is accurate.
		if (this.isPanning && this.startClientX && this.startClientY) {
			this.startClientX = (this.startClientX / this.scale) * scale
			this.startClientY = (this.startClientY / this.scale) * scale
		}

		if (opts.focal) {
			// The difference between the point after the scale and the point before the scale
			// plus the current translation after the scale
			// neutralized to no scale (as the transform scale will apply to the translation)
			const focal = opts.focal
			toX = (focal.x / scale - focal.x / this.scale + this.x * focalScale) / scale
			toY = (focal.y / scale - focal.y / this.scale + this.y * focalScale) / scale
		}

		const panResult = this.constrainXY(toX, toY, scale, { relative: false, force: true })

		this.x = panResult.x
		this.y = panResult.y
		this.scale = scale

		this.setTransformWithEvent('panzoomzoom', opts, originalEvent)
	}

	zoomInOut(isIn: boolean, point: { clientX: number; clientY: number }, zoomOptions?: Partial<PanZoomZoomOptions>) {
		const opts = { ...this.options, ...zoomOptions }
		this.zoomToPoint(this.scale * Math.exp((isIn ? 1 : -1) * opts.step), point, opts)
	}

	zoomIn(point: { clientX: number; clientY: number }, zoomOptions?: Partial<PanZoomZoomOptions>) {
		this.zoomInOut(true, point, zoomOptions)
	}

	zoomOut(point: { clientX: number; clientY: number }, zoomOptions?: Partial<PanZoomZoomOptions>) {
		this.zoomInOut(false, point, zoomOptions)
	}

	zoomWithWheel(event: WheelEvent, zoomOptions?: Partial<PanZoomZoomOptions>) {
		// Need to prevent the default here or it conflicts with regular page scroll
		event.preventDefault()

		const opts = { ...this.options, ...zoomOptions, animate: false }

		// Normalize to deltaX in case shift modifier is used on Mac
		const delta = event.deltaY === 0 && event.deltaX ? event.deltaX : event.deltaY
		const wheel = delta < 0 ? 1 : -1
		const scale = this.constrainScale(this.scale * Math.exp((wheel * opts.step) / 3), opts).scale

		this.zoomToPoint(scale, event, opts, event)
	}

	zoomToPoint(scale: number, point: { clientX: number; clientY: number }, zoomOptions?: Partial<PanZoomZoomOptions>, originalEvent?: PanZoomOriginalEvent) {
		const elementRect = this.element.getBoundingClientRect()
		const parentRect = this.wrapper.getBoundingClientRect()

		// Instead of thinking of operating on the panzoom element,
		// think of operating on the area inside the panzoom
		// element's parent subtract padding and border
		const effectiveArea = {
			width: parentRect.width,
			height: parentRect.height,
		}

		// Adjust the clientX/clientY to ignore the area
		// outside the effective area
		let clientX = point.clientX - parentRect.left
		let clientY = point.clientY - parentRect.top

		// Adjust the clientX/clientY for HTML elements,
		// because they have a transform-origin of 50% 50%
		clientX -= elementRect.width / this.scale / 2
		clientY -= elementRect.height / this.scale / 2

		// Convert the mouse point from it's position over the
		// effective area before the scale to the position
		// over the effective area after the scale.
		const focal = {
			x: (clientX / effectiveArea.width) * (effectiveArea.width * scale),
			y: (clientY / effectiveArea.height) * (effectiveArea.height * scale),
		}

		this.zoom(scale, { ...zoomOptions, focal }, originalEvent)
	}

	pan(toX: number, toY: number, panOptions?: Partial<PanZoomPanOptions>, originalEvent?: PanZoomOriginalEvent) {
		const result = this.constrainXY(toX, toY, this.scale, panOptions)

		// Only try to set if the result is somehow different
		if (this.x !== result.x || this.y !== result.y) {
			this.x = result.x
			this.y = result.y
			this.setTransformWithEvent('panzoompan', result.opts, originalEvent)
		}
	}

	// Centers the element at wrapper's coordinates [x] and [y].
	centerAt(x: number, y: number) {
		const { clientWidth: iw, clientHeight: ih } = this.element
		const siw = iw / this.scale // scaled element width
		const sih = ih / this.scale // scaled element height
		const ciw = -siw / 2 // center of element x
		const cih = -sih / 2 // center of element y
		const { offsetLeft, offsetTop } = this.wrapper
		this.pan(ciw + (x - offsetLeft / 2) / this.scale, cih + (y - offsetTop / 2) / this.scale)
	}

	// Places the element coordinates [x] and [y] on center of screen.
	focusAt(x: number, y: number) {
		const { clientWidth: iw, clientHeight: ih } = this.element
		const { clientWidth: cw, clientHeight: ch, offsetLeft, offsetTop } = this.wrapper
		const siw = iw / this.scale // scaled element width
		const sih = ih / this.scale // scaled element height
		const ciw = -siw / 2 // center of element x
		const cih = -sih / 2 // center of element y
		this.pan(ciw + (cw / 2 - offsetLeft / 2) / this.scale + (iw / 2 - x), cih + (ch / 2 - offsetTop / 2) / this.scale + (ih / 2 - y))
	}

	reset(resetOptions?: Partial<PanZoomOptions>) {
		const opts = { ...this.options, ...resetOptions, force: true }
		this.scale = this.constrainScale(opts.startScale, opts).scale
		const { x, y } = this.constrainXY(opts.startX, opts.startY, this.scale, opts)
		this.x = x
		this.y = y
		this.setTransformWithEvent('panzoomreset', opts)
	}

	destroy() {
		this.unbind()
	}

	private constrainScale(scale: number, zoomOptions?: Partial<PanZoomZoomOptions>) {
		const opts = { ...this.options, ...zoomOptions }
		const result = { scale: this.scale, opts }

		if (!opts.force && opts.disableZoom) {
			return result
		}

		let minScale = this.options.minScale
		let maxScale = this.options.maxScale

		if (opts.contain) {
			const elementRect = this.element.getBoundingClientRect()
			const elemWidth = elementRect.width / this.scale
			const elemHeight = elementRect.height / this.scale

			if (elemWidth > 1 && elemHeight > 1) {
				const parentRect = this.wrapper.getBoundingClientRect()
				const parentWidth = parentRect.width
				const parentHeight = parentRect.height
				const elemScaledWidth = parentWidth / elemWidth
				const elemScaledHeight = parentHeight / elemHeight

				if (opts.contain === 'inside') {
					maxScale = Math.min(maxScale, elemScaledWidth, elemScaledHeight)
				} else {
					minScale = Math.max(minScale, elemScaledWidth, elemScaledHeight)
				}
			}
		}

		result.scale = Math.min(Math.max(scale, minScale), maxScale)

		return result
	}

	private constrainXY(toX: number, toY: number, scale: number, panOptions?: Partial<PanZoomPanOptions>) {
		const opts = { ...this.options, ...panOptions }
		const result = { x: this.x, y: this.y, opts }

		if (!opts.force && (opts.disablePan || (opts.panOnlyWhenZoomed && this.scale === opts.startScale))) {
			return result
		}

		if (!opts.disableXAxis) {
			result.x = (opts.relative ? this.x : 0) + toX
		}

		if (!opts.disableYAxis) {
			result.y = (opts.relative ? this.y : 0) + toY
		}

		if (opts.contain) {
			const elementRect = this.element.getBoundingClientRect()
			const parentRect = this.wrapper.getBoundingClientRect()
			const realWidth = elementRect.width / this.scale
			const realHeight = elementRect.height / this.scale
			const scaledWidth = realWidth * scale
			const scaledHeight = realHeight * scale
			const diffHorizontal = (scaledWidth - realWidth) / 2
			const diffVertical = (scaledHeight - realHeight) / 2

			if (opts.contain === 'inside') {
				const minX = diffHorizontal / scale
				const maxX = (parentRect.width - scaledWidth + diffHorizontal) / scale
				result.x = Math.max(Math.min(result.x, maxX), minX)
				const minY = diffVertical / scale
				const maxY = (parentRect.height - scaledHeight + diffVertical) / scale
				result.y = Math.max(Math.min(result.y, maxY), minY)
			} else {
				const minX = (-(scaledWidth - parentRect.width) + diffHorizontal) / scale
				const maxX = diffHorizontal / scale
				result.x = Math.max(Math.min(result.x, maxX), minX)
				const minY = (-(scaledHeight - parentRect.height) + diffVertical) / scale
				const maxY = diffVertical / scale
				result.y = Math.max(Math.min(result.y, maxY), minY)
			}
		}

		if (opts.roundPixels) {
			result.x = Math.round(result.x)
			result.y = Math.round(result.y)
		}

		return result
	}

	private readonly handleDown = (event: MouseEvent) => {
		// Don't handle this event if the target is excluded
		if (event.button !== 0 || isExcluded(event.target as Element, this.options)) {
			return
		}

		this.isPanning = true
		this.options.handleStartEvent(event)
		this.origX = this.x
		this.origY = this.y

		this.triggerEvent('panzoomstart', { transformation: this.transformation, originalEvent: event }, this.options)

		this.startClientX = event.clientX
		this.startClientY = event.clientY
	}

	private readonly handleMove = (event: MouseEvent) => {
		if (!this.isPanning || this.origX === undefined || this.origY === undefined || this.startClientX === undefined || this.startClientY === undefined) {
			return
		}

		this.pan(this.origX + (event.clientX - this.startClientX) / this.scale, this.origY + (event.clientY - this.startClientY) / this.scale, undefined, event)
	}

	private readonly handleUp = (event: MouseEvent) => {
		this.triggerEvent('panzoomend', { transformation: this.transformation, originalEvent: event }, this.options)

		if (!this.isPanning) {
			return
		}

		this.isPanning = false
		this.origX = this.origY = this.startClientX = this.startClientY = undefined
	}

	private setTransformWithEvent(eventName: PanZoomEvent, opts: PanZoomOptions, originalEvent?: PanZoomOriginalEvent) {
		const value: PanZoomEventDetail = { transformation: this.transformation, originalEvent }
		opts.setTransform(this.element, value.transformation)
		this.triggerEvent(eventName, value, opts)
		this.triggerEvent('panzoomchange', value, opts)
	}

	private triggerEvent(eventName: PanZoomEvent, detail: PanZoomEventDetail, opts: PanZoomOptions) {
		if (opts.silent) {
			return
		}

		this.emit(eventName, detail)
	}
}

function isExcluded(elem: Element, options: PanZoomOptions) {
	let cur: Element | null = elem

	while (cur) {
		if (options.canExclude(cur)) {
			return true
		}

		cur = cur.parentNode as Element | null
	}

	return false
}
