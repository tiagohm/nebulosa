import { Pipe, PipeTransform } from '@angular/core'
import { DropdownItem } from '../types/angular.types'
import { EnumPipe, EnumPipeKey } from './enum.pipe'

@Pipe({ name: 'enumDropdown' })
export class EnumDropdownPipe implements PipeTransform {
	constructor(private readonly enumPipe: EnumPipe) {}

	transform(value: EnumPipeKey[]): DropdownItem<EnumPipeKey>[] {
		return value.map((value) => {
			return { label: this.enumPipe.transform(value), value }
		})
	}
}
