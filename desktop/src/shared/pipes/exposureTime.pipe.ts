import { Pipe, PipeTransform } from '@angular/core'
import { threeDigitsFormatter, twoDigitsFormatter } from '../formatters'

@Pipe({ name: 'exposureTime' })
export class ExposureTimePipe implements PipeTransform {

    transform(value: number) {
        for (const unit of ExposureTimePipe.UNITS) {
            if (value >= unit.factor) {
                return unit.format(value)
            }
        }

        return `${value}`
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
        return `${format.format(value)}${unit}`
    }
}

type UnitFormatter = (value: number) => string

const hourFormatter = formatter(twoDigitsFormatter, 'h')
const minuteFormatter = formatter(twoDigitsFormatter, 'm')
const secondFormatter = formatter(twoDigitsFormatter, 's')
const millisecondFormatter = formatter(threeDigitsFormatter, 'ms')

function format(value: number, factors: [number, number], formatters: [UnitFormatter, UnitFormatter]) {
    const a = value / factors[0]
    const b = (a - Math.trunc(a)) * factors[1]
    return `${formatters[0](a)}${formatters[1](b)}`
}

function hours(value: number) {
    return format(value, [3600000000, 60], [hourFormatter, minuteFormatter])
}

function minutes(value: number) {
    return format(value, [60000000, 60], [minuteFormatter, secondFormatter])
}

function seconds(value: number) {
    return `${twoDigitsFormatter.format(value / 1000000)}s`
}

function milliseconds(value: number) {
    return `${threeDigitsFormatter.format(value / 1000)}ms`
}

function microseconds(value: number) {
    return `${threeDigitsFormatter.format(value)}µs`
}
