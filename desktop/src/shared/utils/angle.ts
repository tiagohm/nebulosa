export type AngleRange = 24 | 360 | 0

export function radiansToDegrees(rad: number) {
	return 57.2957795130823208767981548141051700441964 * rad
}

export function degreesToRadians(deg: number) {
	return 0.0174532925199432957692369076848861272222 * deg
}

export function formatAngle(angle: number, range: AngleRange, sign: boolean, precision: number = 0, units: boolean = true) {
	const parts = decimalToSexagesimal(angle)
	let [, dd, mm, ss] = parts
	const [s] = parts

	ss = roundTo(ss, precision)

	if (ss == 60) {
		ss = 0

		if (++mm == 60) {
			mm = 0

			if (++dd == range) dd = 0
		}
	}

	let ff = 0

	if (precision > 0) {
		const si = Math.trunc(ss)
		ff = Math.round((ss - si) * Math.pow(10, precision))
		ss = si
	}

	const dw = range >= 100 ? 3 : 2
	let du = ' '
	let mu = ' '
	let su = ''

	if (units) {
		if (range == 24) {
			du = 'h'
			mu = 'm'
			su = 's'
		} else {
			du = '\u00B0'
			mu = '\u2032'
			su = '\u2033'
		}
	}

	let result = (sign ? s : '') + zeroPadded(dd, dw) + du + zeroPadded(mm, 2) + mu + zeroPadded(ss, 2)
	if (precision > 0) result += '.' + zeroPadded(ff, precision)
	if (units) result += su

	return result
}

function decimalToSexagesimal(d: number): [string, number, number, number] {
	const t1 = Math.abs(d)
	const t2 = (t1 - Math.trunc(t1)) * 60
	return [d < 0 ? '-' : '+', Math.trunc(t1), Math.trunc(t2), (t2 - Math.trunc(t2)) * 60]
}

function roundTo(x: number, n: number) {
	const p = Math.pow(10, n)
	return Math.round(p * x) / p
}

function padded(x: number, n: number, text: string) {
	let s = x.toString()
	while (s.length < n) s = text + s
	return s
}

function zeroPadded(x: number, n: number) {
	return padded(x, n, '0')
}
