import type { PipeTransform } from '@angular/core'
import { Pipe } from '@angular/core'
import { THREE_DIGITS_FORMATTER, TWO_DIGITS_FORMATTER } from '../constants'

@Pipe({ standalone: false, name: 'exposureTime' })
export class ExposureTimePipe implements PipeTransform {
	transform(value: number) {
		for (const unit of ExposureTimePipe.UNITS) {
			if (value >= unit.factor) {
				return unit.format(value)
			}
		}

		return `${value}s`
	}

	private static readonly UNITS = [
		{ factor: 3600000000, format: hours },
		{ factor: 60000000, format: minutes },
		{ factor: 1000000, format: seconds },
		{ factor: 1000, format: milliseconds },
		{ factor: 1, format: microseconds },
	]
}

function formatter(format: Intl.NumberFormat, unit: string) {
	return function (value: number) {
		return value ? `${format.format(value)}${unit}` : ''
	}
}

type UnitFormatter = (value: number) => string

const hourFormatter = formatter(TWO_DIGITS_FORMATTER, 'h')
const minuteFormatter = formatter(TWO_DIGITS_FORMATTER, 'm')
const secondFormatter = formatter(TWO_DIGITS_FORMATTER, 's')
// const millisecondFormatter = formatter(THREE_DIGITS_FORMATTER, 'ms')

function format(value: number, factors: [number, number], formatters: [UnitFormatter, UnitFormatter]) {
	const a = value / factors[0]
	const ta = Math.trunc(a)
	const b = Math.trunc((a - ta) * factors[1])
	return `${formatters[0](ta)}${formatters[1](b)}`
}

function hours(value: number) {
	return format(value, [3600000000, 60], [hourFormatter, minuteFormatter])
}

function minutes(value: number) {
	return format(value, [60000000, 60], [minuteFormatter, secondFormatter])
}

function seconds(value: number) {
	return `${TWO_DIGITS_FORMATTER.format(value / 1000000)}s`
	// return format(value, [1000000, 1000], [secondFormatter, millisecondFormatter])
}

function milliseconds(value: number) {
	return `${THREE_DIGITS_FORMATTER.format(value / 1000)}ms`
}

function microseconds(value: number) {
	return `${THREE_DIGITS_FORMATTER.format(value)}µs`
}
