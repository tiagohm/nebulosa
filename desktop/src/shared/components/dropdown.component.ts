import { Component, input, model, ViewEncapsulation } from '@angular/core'

export interface DropdownItem<T> {
	label: string
	value: T
}

@Component({
	selector: 'neb-dropdown',
	template: `
		<p-floatLabel class="w-full">
			<p-dropdown
				[options]="options()"
				[(ngModel)]="value"
				[disabled]="disabled() ?? false"
				[optionLabel]="optionLabel()"
				[optionValue]="optionValue()"
				styleClass="w-full p-inputtext-sm border-0"
				[emptyMessage]="emptyMessage()"
				[autoDisplayFirst]="false"
				appendTo="body" />
			<label>{{ label() }}</label>
		</p-floatLabel>
	`,
	styles: `
		neb-dropdown {
			width: 100%;
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class DropdownComponent<T> {
	readonly label = input<string>()
	readonly options = input<T[]>([])
	readonly value = model<T>()
	readonly optionLabel = input<string>('name')
	readonly optionValue = input<string | undefined>()
	readonly disabled = input<boolean | undefined>(false)
	readonly emptyMessage = input('Not available')
}

@Component({
	selector: 'neb-dropdown-item',
	template: `
		<p-floatLabel class="w-full">
			<p-dropdown
				[options]="options()"
				[(ngModel)]="value"
				[disabled]="disabled() ?? false"
				optionLabel="label"
				optionValue="value"
				styleClass="w-full p-inputtext-sm border-0"
				[emptyMessage]="emptyMessage()"
				[autoDisplayFirst]="false"
				appendTo="body" />
			<label>{{ label() }}</label>
		</p-floatLabel>
	`,
	styles: `
		neb-dropdown-item {
			width: 100%;
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class DropdownItemComponent<T> {
	readonly label = input<string>()
	readonly options = input<DropdownItem<T>[]>([])
	readonly value = model<T>()
	readonly disabled = input<boolean | undefined>(false)
	readonly emptyMessage = input('Not available')
}

@Component({
	selector: 'neb-dropdown-enum',
	template: `
		<p-floatLabel class="w-full">
			<p-dropdown
				[options]="options() | enumDropdown"
				[(ngModel)]="value"
				[disabled]="disabled() ?? false"
				optionLabel="label"
				optionValue="value"
				styleClass="w-full p-inputtext-sm border-0"
				[emptyMessage]="emptyMessage()"
				[filter]="filter()"
				[autoDisplayFirst]="false"
				appendTo="body" />
			<label>{{ label() }}</label>
		</p-floatLabel>
	`,
	styles: `
		neb-dropdown-enum {
			width: 100%;
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class DropdownEnumComponent<T extends string> {
	readonly label = input<string>()
	readonly options = input<T[]>([])
	readonly value = model<T>()
	readonly filter = input(false)
	readonly disabled = input<boolean | undefined>(false)
	readonly emptyMessage = input('Not available')
}
