import { Pipe, PipeTransform } from '@angular/core'
import { AstronomicalObject } from '../types/atlas.types'
import { Undefinable } from '../utils/types'

const SKY_OBJECT_PARTS = ['name', 'firstName'] as const

export type SkyObjectPart = (typeof SKY_OBJECT_PARTS)[number]

@Pipe({ name: 'skyObject' })
export class SkyObjectPipe implements PipeTransform {
	transform(value: Undefinable<AstronomicalObject>, what: SkyObjectPart) {
		switch (what) {
			case 'name':
				return value?.name.replaceAll('|', ' · ')
			case 'firstName':
				return value?.name.split(/\[([^\]]+)\]/g).filter(Boolean)[0]
			default:
				return `${value}`
		}
	}
}
