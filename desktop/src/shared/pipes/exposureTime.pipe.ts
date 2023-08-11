import { Pipe, PipeTransform } from '@angular/core'

@Pipe({ name: 'exposureTime' })
export class ExposureTimePipe implements PipeTransform {

    transform(value: number) {
        for (const unit of ExposureTimePipe.UNITS) {
            if (value >= unit.factor) {
                const convertedValue = (value / unit.factor).toFixed(1)
                return `${convertedValue} ${unit.unit}`
            }
        }

        return `${value}`
    }

    private static readonly UNITS = [
        { factor: 3600000000, unit: 'h' },
        { factor: 60000000, unit: 'm' },
        { factor: 1000000, unit: 's' },
        { factor: 1000, unit: 'ms' },
        { factor: 1, unit: 'µs' },
    ]
}
