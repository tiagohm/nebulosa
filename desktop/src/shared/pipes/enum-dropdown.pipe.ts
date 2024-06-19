import { Pipe, PipeTransform } from '@angular/core'
import { EnumPipe, EnumPipeKey } from './enum.pipe'

export interface EnumDropdownItem {
	label: EnumPipeKey
	value: string
}

@Pipe({ name: 'enumDropdown' })
export class EnumDropdownPipe implements PipeTransform {
	constructor(private enumPipe: EnumPipe) {}

	transform(value: EnumPipeKey[]): EnumDropdownItem[] {
		return value.map((value) => {
			return { label: this.enumPipe.transform(value), value }
		})
	}
}
