import { Pipe, PipeTransform } from '@angular/core'
import { AstronomicalObject } from '../types'

const SKY_OBJECT_PARTS = ['name', 'firstName'] as const

export type SkyObjectPart = (typeof SKY_OBJECT_PARTS)[number]

@Pipe({ name: 'skyObject' })
export class SkyObjectPipe implements PipeTransform {

    transform(value: AstronomicalObject | undefined, what: SkyObjectPart) {
        switch (what) {
            case 'name':
                return value?.name.replaceAll('][', ' · ').replace('[', '').replace(']', '')
            case 'firstName':
                return value?.name.split(/\[([^\]]+)\]/g).filter(Boolean)[0]
        }
    }
}
