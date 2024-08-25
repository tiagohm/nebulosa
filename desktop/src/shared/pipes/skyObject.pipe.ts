import { Pipe, PipeTransform } from '@angular/core'
import { AstronomicalObject } from '../types/atlas.types'

export type SkyObjectPart = 'name' | 'firstName'

@Pipe({ name: 'skyObject' })
export class SkyObjectPipe implements PipeTransform {
	transform(value: AstronomicalObject | undefined, what: SkyObjectPart) {
		switch (what) {
			case 'name':
				return value?.name.replaceAll('|', ' · ')
			case 'firstName':
				return value?.name.split(/\[([^\]]+)\]/g).find(Boolean)
			default:
				return `${value}`
		}
	}
}
