import { Pipe, PipeTransform, inject } from '@angular/core'
import { DropdownItem } from '../components/dropdown.component'
import { EnumPipe } from './enum.pipe'

@Pipe({ standalone: false, name: 'enumDropdown' })
export class EnumDropdownPipe implements PipeTransform {
	private readonly enumPipe = inject(EnumPipe)

	transform<T extends string>(value: T[]): DropdownItem<T>[] {
		return value.map((value) => {
			return { label: this.enumPipe.transform(value), value }
		})
	}
}
