export class BicubicInterpolationBase {

    constructor(protected M: number[], protected cols: number, protected rows: number) {
    }

    protected initXY(x: number, y: number) {
        const i1 = Math.min(Math.max(0, Math.trunc(y)), this.rows - 1)
        const j1 = Math.min(Math.max(0, Math.trunc(x)), this.cols - 1)

        const j0 = j1 - 1
        const i0 = i1 - 1
        const j2 = j1 + 1
        const i2 = i1 + 1
        const j3 = j1 + 2
        const i3 = i1 + 2

        let fp = i0 * this.cols + j0

        if (i0 < 0) fp += this.cols
        const p0 = this.getRow(fp, j0, j2, j3)

        if (i0 >= 0) fp += this.cols
        const p1 = this.getRow(fp, j0, j2, j3)

        if (i2 < this.rows) fp += this.cols
        const p2 = this.getRow(fp, j0, j2, j3)

        if (i3 < this.rows) fp += this.cols
        const p3 = this.getRow(fp, j0, j2, j3)

        return { i1, j1, p0, p1, p2, p3 }
    }

    getRow(fp: number, j0: number, j2: number, j3: number) {
        const p = new Array<number>(4)

        if (j0 < 0) ++fp
        p[0] = this.M[fp]

        if (j0 >= 0) ++fp
        p[1] = this.M[fp]

        if (j2 < this.cols) {
            p[2] = this.M[++fp]
            if (j3 < this.cols) ++fp
            p[3] = this.M[fp]
        } else {
            p[2] = this.M[fp]
            p[3] = this.M[fp - 1]
        }

        return p
    }
}

export class BicubicSplineInterpolation extends BicubicInterpolationBase {

    private readonly clamp: number

    constructor(M: number[], cols: number, rows: number, clamp?: number) {
        super(M, cols, rows)
        this.clamp = (clamp === undefined || clamp < 0 || clamp > 1) ? 0.3 : clamp
    }

    interpolate(x: number, y: number) {
        const { i1, j1, p0, p1, p2, p3 } = this.initXY(x, y)
        const C = this.coefficients(x - j1)
        const c = [this.spline(p0, C), this.spline(p1, C), this.spline(p2, C), this.spline(p3, C)]
        return this.spline(c, this.coefficients(y - i1))
    }

    private coefficients(dx: number) {
        const dx2 = dx * dx
        const dx3 = dx2 * dx
        const dx1_2 = dx / 2
        const dx2_2 = dx2 / 2
        const dx3_2 = dx3 / 2
        const dx22 = dx2 + dx2
        const dx315 = dx3 + dx3_2
        return [dx2 - dx3_2 - dx1_2, dx315 - dx22 - dx2_2 + 1, dx22 - dx315 + dx1_2, dx3_2 - dx2_2]
    }

    private spline(p: number[], C: number[]) {
        const f12 = p[1] * C[1] + p[2] * C[2]
        const f03 = p[0] * C[0] + p[3] * C[3]
        return (-f03 < f12 * this.clamp) ? f12 + f03 : f12 / (C[1] + C[2])
    }
}