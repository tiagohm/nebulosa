import { angleStringify } from './angle-stringify'
import { BicubicSplineInterpolation } from './bicubic-interpolation'
import { TimeRepresentation, degreesToRadians, longitudeDegreesConstrained, obliquity, rectangularEquatorialToEcliptic, rectangularEquatorialToGalactic, rectangularToSphericalDegreesConstrained, sphericalToRectangular } from './ephemeris'

export interface InterpolateCoordinate<T extends string | number> {
    alpha: T
    delta: T
    l?: T
    b?: T
    lambda?: T
    beta?: T
}

export class CoordinateInterpolation {

    private readonly Ia: BicubicSplineInterpolation
    private readonly Id: BicubicSplineInterpolation
    private se = 0.0
    private ce = 0.0
    private readonly precision

    constructor(Ma: number[], Md: number[],
        private x0: number, private y0: number, private x1: number, private y1: number,
        private delta: number, private date?: TimeRepresentation, scale: number = 1) {

        x0 = Math.min(x0, x1)
        x1 = Math.max(x0, x1)
        y0 = Math.min(y0, y1)
        y1 = Math.max(y0, y1)

        if (this.date) {
            const eps = obliquity(this.date)
            this.se = Math.sin(eps)
            this.ce = Math.cos(eps)
        }

        let width = this.x1 - this.x0
        let height = this.y1 - this.y0
        const rows = 1 + Math.trunc(height / this.delta) + ((height % this.delta != 0) ? 1 : 0)
        const cols = 1 + Math.trunc(width / this.delta) + ((width % this.delta != 0) ? 1 : 0)

        if (rows < 2 || cols < 2)
            throw new Error('CoordinateInterpolation: Insufficient interpolation space.')

        if (Ma.length != rows * cols || Md.length != rows * cols)
            throw new Error('CoordinateInterpolation: Invalid matrix dimensions.')

        this.Ia = new BicubicSplineInterpolation(Ma, cols, rows)
        this.Id = new BicubicSplineInterpolation(Md, cols, rows)

        if (scale <= 0) scale = 1

        const q1 = this.interpolate((this.x0 + this.x1) / 2, (this.y0 + this.y1) / 2)
        const q2 = this.interpolate((this.x0 + this.x1) / 2 + 1 / scale, (this.y0 + this.y1) / 2 + 1 / scale)
        const d = 3600 * Math.min(Math.abs(q2.alpha - q1.alpha), Math.abs(q2.delta - q1.delta))
        this.precision = (d >= 2) ? 0 : ((d >= 1) ? 1 : 2)
    }

    interpolate(x: number, y: number, withGalactic: boolean = false, withEcliptic: boolean = false) {
        const fx = (x - this.x0) / this.delta
        const fy = (y - this.y0) / this.delta
        const alpha = longitudeDegreesConstrained(this.Ia.interpolate(fx, fy))
        const delta = this.Id.interpolate(fx, fy)

        const coordinate: InterpolateCoordinate<number> = {
            alpha: alpha,
            delta: delta,
        }

        if (withGalactic || withEcliptic && this.date) {
            const s = {
                lon: degreesToRadians(alpha),
                lat: degreesToRadians(delta),
            }

            const r = sphericalToRectangular(s)

            if (withGalactic) {
                let g = rectangularToSphericalDegreesConstrained(rectangularEquatorialToGalactic(r))
                coordinate.l = g.lon
                coordinate.b = g.lat
            }

            if (withEcliptic)
                if (this.date) {
                    let e = rectangularToSphericalDegreesConstrained(rectangularEquatorialToEcliptic(r, this.se, this.ce))
                    coordinate.lambda = e.lon
                    coordinate.beta = e.lat
                }
        }

        return coordinate
    }

    interpolateAsText(x: number, y: number, units: boolean = true, withGalactic: boolean = false, withEcliptic: boolean = false) {
        const q = this.interpolate(x, y, withGalactic, withEcliptic)

        const coordinate: InterpolateCoordinate<string> = {
            alpha: angleStringify(q.alpha / 15, 24, false, this.precision + 1, units),
            delta: angleStringify(q.delta, 0, true, this.precision, units)
        }

        if (q.l !== undefined && q.b !== undefined) {
            coordinate.l = angleStringify(q.l, 360, false, this.precision, units)
            coordinate.b = angleStringify(q.b, 0, true, this.precision, units)
        }

        if (q.lambda !== undefined && q.beta !== undefined) {
            coordinate.lambda = angleStringify(q.lambda, 360, false, this.precision, units)
            coordinate.beta = angleStringify(q.beta, 0, true, this.precision, units)
        }

        return coordinate
    }
}