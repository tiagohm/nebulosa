import { Pipe, PipeTransform } from '@angular/core'
import { Angle } from '../types'
import { AngleRange, angleStringify } from '../utils/angle-stringify'
import { radiansToDegrees } from '../utils/ephemeris'

@Pipe({ name: 'angle' })
export class AnglePipe implements PipeTransform {

    transform(value: Angle, radians: boolean, range: AngleRange) {
        if (typeof value === 'string') return value
        value = radians ? radiansToDegrees(value) : value
        value = range === 24 ? value / 15.0 : value
        return angleStringify(value, range, range !== 24, 1, true)
    }
}
