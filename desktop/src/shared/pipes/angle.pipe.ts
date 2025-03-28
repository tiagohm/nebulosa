import type { PipeTransform } from '@angular/core'
import { Pipe } from '@angular/core'
import type { Angle } from '../types/atlas.types'
import type { AngleRange } from '../utils/angle'
import { formatAngle, radiansToDegrees } from '../utils/angle'

@Pipe({ standalone: false, name: 'angle' })
export class AnglePipe implements PipeTransform {
	transform(value: Angle, radians: boolean, range: AngleRange) {
		if (typeof value === 'string') return value
		value = radians ? radiansToDegrees(value) : value
		value = range === 24 ? value / 15.0 : value
		return formatAngle(value, range, range !== 24, 1, true)
	}
}
