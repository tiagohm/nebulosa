import { Pipe, PipeTransform } from '@angular/core'
import { DropdownItem } from '../types/angular.types'
import { EnumPipe } from './enum.pipe'

@Pipe({ name: 'enumDropdown' })
export class EnumDropdownPipe implements PipeTransform {
	constructor(private readonly enumPipe: EnumPipe) {}

	transform<T extends string>(value: T[]): DropdownItem<T>[] {
		return value.map((value) => {
			return { label: this.enumPipe.transform(value), value }
		})
	}
}
