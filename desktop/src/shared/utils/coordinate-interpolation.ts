import { Angle, EquatorialCoordinateJ2000 } from '../types/atlas.types'
import { degreesToRadians, formatAngle } from './angle'
import { BicubicSplineInterpolation } from './bicubic-interpolation'
import { SphericalRepresentation, TimeRepresentation, longitudeDegreesConstrained, obliquity, rectangularEquatorialToEcliptic, rectangularEquatorialToGalactic, rectangularToSphericalDegreesConstrained, sphericalToRectangular } from './ephemeris'

export interface InterpolatedCoordinate<T extends Angle> extends EquatorialCoordinateJ2000 {
	alpha: T
	delta: T
	l?: T
	b?: T
	lambda?: T
	beta?: T
}

// https://cdn.astrobin.com/static/astrobin_apps_platesolving/js/CoordinateInterpolation.1733091e5e90.js

export class CoordinateInterpolator {
	private readonly Ia: BicubicSplineInterpolation
	private readonly Id: BicubicSplineInterpolation
	private se = 0.0
	private ce = 0.0

	constructor(
		Ma: number[],
		Md: number[],
		private x0: number,
		private y0: number,
		private x1: number,
		private y1: number,
		private delta: number,
		private date?: TimeRepresentation,
		private precision: number = 1,
	) {
		x0 = Math.min(x0, x1)
		x1 = Math.max(x0, x1)
		y0 = Math.min(y0, y1)
		y1 = Math.max(y0, y1)

		if (this.date) {
			const eps = obliquity(this.date)
			this.se = Math.sin(eps)
			this.ce = Math.cos(eps)
		}

		const width = this.x1 - this.x0
		const height = this.y1 - this.y0
		const rows = 1 + Math.trunc(height / this.delta) + (height % this.delta != 0 ? 1 : 0)
		const cols = 1 + Math.trunc(width / this.delta) + (width % this.delta != 0 ? 1 : 0)

		if (rows < 2 || cols < 2) throw new Error('CoordinateInterpolation: Insufficient interpolation space.')

		if (Ma.length != rows * cols || Md.length != Ma.length) throw new Error('CoordinateInterpolation: Invalid matrix dimensions.')

		this.Ia = new BicubicSplineInterpolation(Ma, cols, rows)
		this.Id = new BicubicSplineInterpolation(Md, cols, rows)
	}

	interpolate(x: number, y: number, withGalactic: boolean = false, withEcliptic: boolean = false) {
		const fx = (x - this.x0) / this.delta
		const fy = (y - this.y0) / this.delta
		const alpha = longitudeDegreesConstrained(this.Ia.interpolate(fx, fy))
		const delta = this.Id.interpolate(fx, fy)

		const coordinate: InterpolatedCoordinate<number> = {
			alpha: alpha,
			delta: delta,
			rightAscensionJ2000: formatAngle(alpha / 15, 24, false, this.precision),
			declinationJ2000: formatAngle(delta, 0, true, this.precision),
		}

		if (withGalactic || withEcliptic) {
			const s: SphericalRepresentation = {
				lon: degreesToRadians(alpha),
				lat: degreesToRadians(delta),
			}

			const r = sphericalToRectangular(s)

			if (withGalactic) {
				const g = rectangularToSphericalDegreesConstrained(rectangularEquatorialToGalactic(r))
				coordinate.l = g.lon
				coordinate.b = g.lat
			}

			if (withEcliptic && this.date) {
				const e = rectangularToSphericalDegreesConstrained(rectangularEquatorialToEcliptic(r, this.se, this.ce))
				coordinate.lambda = e.lon
				coordinate.beta = e.lat
			}
		}

		return coordinate
	}

	interpolateAsText(x: number, y: number, units: boolean = true, withGalactic: boolean = false, withEcliptic: boolean = false) {
		const q = this.interpolate(x, y, withGalactic, withEcliptic)

		const coordinate: InterpolatedCoordinate<string> = {
			alpha: q.rightAscensionJ2000 as string,
			delta: q.declinationJ2000 as string,
			rightAscensionJ2000: q.rightAscensionJ2000,
			declinationJ2000: q.declinationJ2000,
		}

		if (q.l !== undefined && q.b !== undefined) {
			coordinate.l = formatAngle(q.l, 360, false, this.precision, units)
			coordinate.b = formatAngle(q.b, 0, true, this.precision, units)
		}

		if (q.lambda !== undefined && q.beta !== undefined) {
			coordinate.lambda = formatAngle(q.lambda, 360, false, this.precision, units)
			coordinate.beta = formatAngle(q.beta, 0, true, this.precision, units)
		}

		return coordinate
	}
}
